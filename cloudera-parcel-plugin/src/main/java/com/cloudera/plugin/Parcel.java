package com.cloudera.plugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.archiver.tar.TarArchiver;
import org.codehaus.plexus.archiver.tar.TarArchiver.TarCompressionMethod;
import org.codehaus.plexus.archiver.tar.TarGZipUnArchiver;
import org.codehaus.plexus.archiver.util.DefaultFileSet;
import org.codehaus.plexus.components.io.fileselectors.FileInfo;
import org.codehaus.plexus.components.io.fileselectors.FileSelector;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.console.ConsoleLogger;
import org.codehaus.plexus.util.StringUtils;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.common.collect.ImmutableMap;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

public class Parcel {

  public boolean isValid() throws MojoExecutionException {
    List<String> paramatersMissing = new ArrayList<>();
    if (StringUtils.isEmpty(groupId)) {
      paramatersMissing.add("groupId");
    }
    if (StringUtils.isEmpty(artifactId)) {
      paramatersMissing.add("artifactId");
    }
    if (StringUtils.isEmpty(version)) {
      paramatersMissing.add("version");
    }
    if (StringUtils.isEmpty(classifier)) {
      classifier = getOsDescriptor();
    }
    if (StringUtils.isEmpty(baseDirectory)) {
      paramatersMissing.add("baseDirectory");
    }
    if (StringUtils.isEmpty(type)) {
      paramatersMissing.add("type");
    }
    if (!paramatersMissing.isEmpty()) {
      throw new MojoExecutionException("The required parameters " + paramatersMissing
          + " were missing from the POM as <properties><parcel.MISSING-PARAMATER> or "
          + "<execution><configuration><parcels><parcel><MISSING-PARAMATER> definitions");
    }
    return true;
  }

  @SuppressWarnings("rawtypes")
  public boolean isValid(Map paramaters) throws MojoExecutionException {
    List<Object> paramatersMissing = new ArrayList<>();
    for (Object paramater : paramaters.keySet()) {
      if (paramaters.get(paramater) == null
          || paramaters.get(paramater) instanceof String && StringUtils.isEmpty((String) paramaters.get(paramater))) {
        paramatersMissing.add(paramater);
      }
    }
    if (!paramatersMissing.isEmpty()) {
      throw new MojoExecutionException("The required parameters " + paramatersMissing
          + " were missing from the POM as <properties><parcel.MISSING-PARAMATER> or "
          + "<execution><configuration><parcels><parcel><MISSING-PARAMATER> definitions");
    }
    return isValid();
  }

  public String getArtifactName() throws MojoExecutionException {
    return isValid()
        ? artifactId + "-" + version + (StringUtils.isEmpty(classifier) ? "" : "-" + classifier) + "." + type : null;
  }

  public String getArtifactNameSansClassifierType() throws MojoExecutionException {
    return isValid() ? artifactId + "-" + version : null;
  }

  public String getArtifactNamespace() throws MojoExecutionException {
    return isValid() ? groupId + ":" + artifactId + ":" + type
        + (StringUtils.isEmpty(classifier) ? "" : ":" + classifier) + ":" + version : null;
  }

  public String getVersionShort() {
    int index = 0;
    for (index = 0; index < version.length(); index++) {
      if (!Character.isDigit(version.charAt(index)) && version.charAt(index) != '.') {
        break;
      }
    }
    return version.substring(0, index);
  }

  public String getRemoteUrl(String repositoryUrl) throws MojoExecutionException {
    return isValid() ? repositoryUrl + "/" + getVersionShort() + "/" + artifactId + "-" + version
        + (StringUtils.isEmpty(classifier) ? "" : "-" + classifier) + "." + type : null;
  }

  public String getLocalPath() throws MojoExecutionException {
    return isValid() ? "/" + groupId.replaceAll("\\.", "/") + "/" + artifactId + "/" + version + "/" + getArtifactName()
        : null;
  }

  public boolean download(Log log) throws MojoExecutionException {
    isValid(ImmutableMap.of("localRepositoryDirectory", localRepositoryDirectory, "repositoryUrl", repositoryUrl));
    boolean downloaded = false;
    File localPath = new File(getFile(localRepositoryDirectory).getAbsolutePath(), getLocalPath());
    File localPathSha1 = new File(getFile(localRepositoryDirectory).getAbsolutePath(), getLocalPath() + SUFFIX_SHA1);
    if (localPath.exists() && localPathSha1.exists()) {
      if (!assertSha1(log, localPath, localPathSha1, true)) {
        localPath.delete();
        localPathSha1.delete();
      }
    } else if (localPath.exists() && !localPathSha1.exists()) {
      localPath.delete();
    } else if (!localPath.exists() && localPathSha1.exists()) {
      localPathSha1.delete();
    }
    if (!localPath.exists() && !localPathSha1.exists()) {
      System.out.println("Downloading: " + getRemoteUrl(repositoryUrl));
      try {
        GenericUrl remoteUrl = new GenericUrl(getRemoteUrl(repositoryUrl));
        GenericUrl remoteUrlSha1 = new GenericUrl(getRemoteUrl(repositoryUrl) + SUFFIX_SHA1);
        long time = System.currentTimeMillis();
        if (downloaded = downloadHttpResource(log, remoteUrl, localPath)
            && downloadHttpResource(log, remoteUrlSha1, localPathSha1)) {
          if (!(downloaded = assertSha1(log, localPath, localPathSha1, true))) {
            localPath.delete();
            localPathSha1.delete();
            throw new MojoExecutionException(
                "Downloaded file from [" + remoteUrl + "] failed to match checksum [" + remoteUrlSha1 + "]");
          }
          System.out.println("Downloaded: " + remoteUrl + " ("
              + FileUtils.byteCountToDisplaySize(localPath.length() + localPathSha1.length()) + " at "
              + String.format("%.2f",
                  (localPath.length() + localPathSha1.length()) / ((System.currentTimeMillis() - time) * 1000D))
              + " MB/sec)");
        }
      } catch (Exception exception) {
        if (log.isDebugEnabled()) {
          log.debug("Error encountered downlaoding parcel [" + getArtifactName() + "]", exception);
        }
      }
      if (!downloaded) {
        throw new MojoExecutionException("Could not find parcel [" + getArtifactName() + "] in remote repositories, "
            + "see above for download attemps and try a mvn -X invocation for DEBUG logs showing transport exceptions");
      }
    }
    return downloaded;
  }

  public boolean explode(Log log) throws MojoExecutionException {
    isValid(ImmutableMap.of("localRepositoryDirectory", localRepositoryDirectory));
    download(log);
    File explodedPath = StringUtils.isEmpty(outputDirectory)
        ? new File(getFile(localRepositoryDirectory).getAbsolutePath(), getLocalPath()).getParentFile()
        : getFile(outputDirectory);
    String explodedPathRoot = explodedPath.toString() + File.separator + getArtifactNameSansClassifierType();
    boolean exploded = new File(explodedPath, getArtifactNameSansClassifierType()).exists();
    if (!exploded) {
      File localPath = new File(getFile(localRepositoryDirectory).getAbsolutePath(), getLocalPath());
      log.info("Exploding " + localPath.getAbsolutePath());
      try {
        explodedPath.mkdirs();
        TarGZipUnArchiver unarchiver = new TarGZipUnArchiver();
        unarchiver.enableLogging(new ConsoleLogger(Logger.LEVEL_DISABLED, "Logger"));
        unarchiver.setSourceFile(localPath);
        unarchiver.setDestDirectory(explodedPath);
        unarchiver.extract();
        exploded = true;
      } catch (Exception exception) {
        throw new MojoExecutionException("Failed to explode artifact [" + getArtifactNamespace() + "] from ["
            + localPath + "] to [" + explodedPath + "]", exception);
      }
    }
    try {
      if (StringUtils.isNotEmpty(linkDirectory)
          && Files.notExists(Paths.get(linkDirectory), LinkOption.NOFOLLOW_LINKS)) {
        getFile(linkDirectory).getParentFile().mkdirs();
        Files.createSymbolicLink(Paths.get(linkDirectory), Paths.get(explodedPathRoot));
      }
    } catch (Exception exception) {
      throw new MojoExecutionException("Failed to sym link to exploded artifact [" + getArtifactNamespace() + "] from ["
          + explodedPathRoot + "] to [" + linkDirectory + "]", exception);
    }
    return exploded;
  }

  public boolean prepare(Log log) throws MojoExecutionException {
    isValid(ImmutableMap.of("parcelResourcesDirectory", parcelResourcesDirectory, "parcelBuildDirectory",
        parcelBuildDirectory));
    final Path sourcePath = Paths.get(getFile(parcelResourcesDirectory).getAbsolutePath());
    final Path ouputPath = Paths.get(getFile(parcelBuildDirectory).getAbsolutePath(),
        getArtifactNameSansClassifierType());
    try {
      if (Files.exists(sourcePath)) {
        Files.walkFileTree(sourcePath, new SimpleFileVisitor<Path>() {
          @Override
          public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException {
            Files.createDirectories(ouputPath.resolve(sourcePath.relativize(dir)));
            return FileVisitResult.CONTINUE;
          }

          @Override
          public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
            Files.copy(file, ouputPath.resolve(sourcePath.relativize(file)), StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.COPY_ATTRIBUTES);
            return FileVisitResult.CONTINUE;
          }
        });
      }
    } catch (Exception exception) {
      throw new MojoExecutionException("Failed to prepare artifact [" + getArtifactNamespace() + "] from [" + sourcePath
          + "] to [" + ouputPath + "]", exception);
    }
    return true;
  }

  public boolean build(Log log) throws MojoExecutionException {
    isValid(ImmutableMap.of("buildDirectory", buildDirectory, "parcelBuildDirectory", parcelBuildDirectory));
    File buildPath = new File(getFile(buildDirectory).getAbsolutePath(), getArtifactName());
    File buildPathSha1 = new File(getFile(buildDirectory).getAbsolutePath(), getArtifactName() + SUFFIX_SHA1);
    File sourcePath = getFile(parcelBuildDirectory);
    buildPath.delete();
    buildPathSha1.delete();
    try {
      TarArchiver archiver = new TarArchiver();
      archiver.setCompression(TarCompressionMethod.gzip);
      DefaultFileSet fileSet = new DefaultFileSet();
      fileSet.setDirectory(sourcePath);
      fileSet.setFileSelectors(new FileSelector[] { new FileSelector() {
        @Override
        public boolean isSelected(FileInfo fileInfo) throws IOException {
          return !fileInfo.isFile()
              || !new File(getFile(buildDirectory).getAbsolutePath(), fileInfo.getName()).canExecute();
        }
      } });
      archiver.addFileSet(fileSet);
      archiver.setFileMode(0755);
      fileSet = new DefaultFileSet();
      fileSet.setDirectory(sourcePath);
      fileSet.setFileSelectors(new FileSelector[] { new FileSelector() {
        @Override
        public boolean isSelected(FileInfo fileInfo) throws IOException {
          return fileInfo.isFile()
              && new File(getFile(buildDirectory).getAbsolutePath(), fileInfo.getName()).canExecute();
        }
      } });
      archiver.addFileSet(fileSet);
      archiver.setDestFile(buildPath);
      archiver.createArchive();
      FileUtils.writeStringToFile(buildPathSha1, calculateSha1(buildPath) + "\n");
    } catch (Exception exception) {
      throw new MojoExecutionException(
          "Failed to build artifact [" + getArtifactNamespace() + "] from [" + sourcePath + "] to [" + buildPath + "]",
          exception);
    }
    return assertSha1(log, buildPath, buildPathSha1, false);
  }

  public boolean install(Log log) throws MojoExecutionException {
    isValid(ImmutableMap.of("buildDirectory", buildDirectory, "localRepositoryDirectory", localRepositoryDirectory));
    File buildPath = new File(getFile(buildDirectory).getAbsolutePath(), getArtifactName());
    File buildPathSha1 = new File(getFile(buildDirectory).getAbsolutePath(), getArtifactName() + SUFFIX_SHA1);
    File repositoryRootPath = new File(getFile(localRepositoryDirectory).getAbsolutePath(), getLocalPath())
        .getParentFile();
    File repositoryPath = new File(repositoryRootPath, getArtifactName());
    File repositoryPathSha1 = new File(repositoryRootPath, getArtifactName() + SUFFIX_SHA1);
    try {
      if (assertSha1(log, buildPath, buildPathSha1, false)) {
        log.info("Installing " + buildPath + " to " + repositoryPath);

        FileUtils.copyFileToDirectory(buildPath, repositoryRootPath);
        FileUtils.copyFileToDirectory(buildPathSha1, repositoryRootPath);
      }
    } catch (Exception exception) {
      throw new MojoExecutionException("Failed to install artifact [" + getArtifactNamespace() + "] from [" + buildPath
          + "] to [" + repositoryRootPath + "]", exception);
    }
    return assertSha1(log, repositoryPath, repositoryPathSha1, false);
  }

  public boolean deploy(Log log) throws MojoExecutionException {
    isValid(ImmutableMap.of("buildDirectory", buildDirectory, "distributionRepositoryUrl", distributionRepositoryUrl));
    boolean deployed = false;
    File buildPath = new File(getFile(buildDirectory).getAbsolutePath(), getArtifactName());
    File buildPathSha1 = new File(getFile(buildDirectory).getAbsolutePath(), getArtifactName() + SUFFIX_SHA1);
    Matcher sshConnectMatcher = REGEXP_SCP_CONNECT
        .matcher(distributionRepositoryUrl + (distributionRepositoryUrl.endsWith("/") ? "" : "/") + getVersionShort());
    if (!sshConnectMatcher.matches()) {
      throw new MojoExecutionException("Could not match [" + distributionRepositoryUrl + "] with regexp ["
          + REGEXP_SCP_CONNECT + "], please check your ssh connect string and provide all values.");
    }
    Session session = null;
    ChannelExec channelSsh = null;
    ChannelSftp channelScp = null;
    try {
      if (assertSha1(log, buildPath, buildPathSha1, false)) {
        System.out.println("Deploying: " + buildPath + " to " + sshConnectMatcher.group(0));
        long time = System.currentTimeMillis();
        JSch jsch = new JSch();
        jsch.addIdentity(sshConnectMatcher.group(2));
        session = jsch.getSession(sshConnectMatcher.group(1), sshConnectMatcher.group(3),
            Integer.parseInt(sshConnectMatcher.group(4)));
        Properties config = new java.util.Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.connect();
        channelSsh = (ChannelExec) session.openChannel("exec");
        BufferedReader readerSsh = new BufferedReader(new InputStreamReader(channelSsh.getInputStream()));
        channelSsh.setCommand("mkdir -p " + sshConnectMatcher.group(5));
        channelSsh.connect();
        String output = IOUtils.toString(readerSsh);
        if (!output.isEmpty()) {
          throw new MojoExecutionException(
              "Failed to create deploy directory [" + sshConnectMatcher.group(5) + "] for artifact ["
                  + getArtifactNamespace() + "] on [" + sshConnectMatcher.group(0) + "] with error [" + output + "]");
        }
        channelScp = (ChannelSftp) session.openChannel("sftp");
        channelScp.connect();
        channelScp.cd(sshConnectMatcher.group(5));
        channelScp.put(buildPath.getAbsolutePath(), buildPath.getName());
        channelScp.put(buildPathSha1.getAbsolutePath(), buildPathSha1.getName());
        System.out.println("Deployed: " + buildPath + " ("
            + FileUtils.byteCountToDisplaySize(buildPath.length() + buildPathSha1.length()) + " at "
            + String.format("%.2f",
                (buildPath.length() + buildPathSha1.length()) / ((System.currentTimeMillis() - time) * 1000D))
            + " MB/sec)");
        deployed = true;
      }
    } catch (Exception exception) {
      throw new MojoExecutionException("Failed to deploy artifact [" + getArtifactNamespace() + "] from [" + buildPath
          + "] to [" + sshConnectMatcher.group(0) + "]", exception);
    } finally {
      if (channelSsh != null) {
        channelSsh.disconnect();
      }
      if (channelScp != null) {
        channelScp.disconnect();
      }
      if (session != null) {
        session.disconnect();
      }
    }
    return deployed;
  }

  private boolean downloadHttpResource(Log log, GenericUrl remote, File local) throws MojoExecutionException {
    local.getParentFile().mkdirs();
    FileOutputStream localStream = null;
    try {
      HttpResponse httpResponse = new NetHttpTransport().createRequestFactory().buildGetRequest(remote)
          .setContentLoggingLimit(0).setConnectTimeout(5000).setReadTimeout(0).execute();
      if (httpResponse.getStatusCode() == 200) {
        httpResponse.download(localStream = new FileOutputStream(local));
        return true;
      }
    } catch (Exception exception) {
      if (log.isDebugEnabled()) {
        log.debug("Error downloading resource [" + remote + "]", exception);
      }
    } finally {
      IOUtils.closeQuietly(localStream);
    }
    return false;
  }

  private String calculateSha1(File file) throws MojoExecutionException {
    InputStream input = null;
    try {
      MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
      input = new FileInputStream(file);
      byte[] buffer = new byte[8192];
      int len = 0;
      while ((len = input.read(buffer)) != -1) {
        messageDigest.update(buffer, 0, len);
      }
      return new HexBinaryAdapter().marshal(messageDigest.digest());
    } catch (Exception exception) {
      throw new MojoExecutionException("Could not create SHA1 of file [" + file + "]");
    } finally {
      IOUtils.closeQuietly(input);
    }
  }

  private boolean assertSha1(Log log, File file, File fileSha1, boolean quiet) throws MojoExecutionException {
    InputStream input = null;
    try {
      if (!IOUtils.toString(input = new FileInputStream(fileSha1)).trim().toUpperCase()
          .equals(calculateSha1(file).toUpperCase())) {
        if (quiet) {
          return false;
        } else {
          throw new MojoExecutionException("File [" + file + "] is not consistent with hash file [" + fileSha1 + "]");
        }
      }
      return true;
    } catch (Exception exception) {
      if (quiet) {
        if (log.isDebugEnabled()) {
          log.debug("Could not verify file [" + file + "] is consistent with hash file [" + fileSha1 + "]", exception);
        }
        return false;
      } else {
        throw new MojoExecutionException(
            "Could not verify file [" + file + "] is consistent with hash file [" + fileSha1 + "]");
      }
    } finally {
      IOUtils.closeQuietly(input);
    }
  }

  private static final String SUFFIX_SHA1 = ".sha1";

  private static final Pattern REGEXP_SCP_CONNECT = Pattern.compile("^scp://(.*):(.*)@(.*):([0-9]*)(.*)");

  private static final Map<String, ImmutableMap<String, String>> OS_NAME_VERSION_DESCRIPTOR = ImmutableMap.of(//
      "Mac OS X", //
      ImmutableMap.of(//
          "10\\.11.*", "elcapitan"//
      ), //
      "Linux", //
      ImmutableMap.of(//
          ".*\\.el6\\..*", "el6", //
          ".*\\.el7\\..*", "el7", //
          "4\\.2\\.0.*-generic", "trusty"//
      )//
  );

  private File getFile(String path) {
    return path.startsWith("/") ? new File(path) : new File(baseDirectory, path);
  }

  public static String getOsDescriptor() {
    Map<String, String> osVersionDescriptor = OS_NAME_VERSION_DESCRIPTOR.get(System.getProperty("os.name"));
    if (osVersionDescriptor != null) {
      for (String versionRegEx : osVersionDescriptor.keySet())
        if (System.getProperty("os.version").matches(versionRegEx)) {
          return osVersionDescriptor.get(versionRegEx);
        }
    }
    throw new RuntimeException("Could not determine OS descritor from system property os.name ["
        + System.getProperty("os.name") + "] and os.version [" + System.getProperty("os.version")
        + "] from the regexp mapping " + OS_NAME_VERSION_DESCRIPTOR
        + ". If your OS looks like a supported platform, you can overide this parcels [classifier]"
        + " on the command line.");
  }

  @Parameter(required = false, defaultValue = "com.cloudera.parcel")
  private String groupId = "com.cloudera.parcel";

  @Parameter(required = true)
  private String artifactId;

  @Parameter(required = true)
  private String version;

  @Parameter(required = false, defaultValue = "")
  private String classifier = "";

  @Parameter(required = false, defaultValue = "")
  private String baseDirectory = "";

  @Parameter(required = false, defaultValue = "src/main/parcel")
  private String parcelResourcesDirectory = "src/main/parcel";

  @Parameter(required = false, defaultValue = "target")
  private String buildDirectory = "target";

  @Parameter(required = false, defaultValue = "target/parcel")
  private String parcelBuildDirectory = "target/parcel";

  @Parameter(required = false, defaultValue = "")
  private String outputDirectory = "";

  @Parameter(required = false, defaultValue = "")
  private String linkDirectory = "";

  @Parameter(required = false, defaultValue = "")
  private String repositoryUrl = "";

  @Parameter(required = false, defaultValue = "")
  private String distributionRepositoryUrl = "";

  @Parameter(required = false, defaultValue = "")
  private String localRepositoryDirectory = "";

  @Parameter(required = false, defaultValue = "parcel")
  private String type = "parcel";

  public Parcel() {
  }

  public String getGroupId() {
    return groupId;
  }

  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }

  public String getArtifactId() {
    return artifactId;
  }

  public void setArtifactId(String artifactId) {
    this.artifactId = artifactId;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getClassifier() {
    return classifier;
  }

  public void setClassifier(String classifier) {
    this.classifier = classifier;
  }

  public String getBaseDirectory() {
    return baseDirectory;
  }

  public void setBaseDirectory(String baseDirectory) {
    this.baseDirectory = baseDirectory;
  }

  public String getParcelResourcesDirectory() {
    return parcelResourcesDirectory;
  }

  public void setParcelResourcesDirectory(String parcelResourcesDirectory) {
    this.parcelResourcesDirectory = parcelResourcesDirectory;
  }

  public String getBuildDirectory() {
    return buildDirectory;
  }

  public void setBuildDirectory(String buildDirectory) {
    this.buildDirectory = buildDirectory;
  }

  public String getParcelBuildDirectory() {
    return parcelBuildDirectory;
  }

  public void setParcelBuildDirectory(String parcelBuildDirectory) {
    this.parcelBuildDirectory = parcelBuildDirectory;
  }

  public String getOutputDirectory() {
    return outputDirectory;
  }

  public void setOutputDirectory(String outputDirectory) {
    this.outputDirectory = outputDirectory;
  }

  public String getLocalRepositoryDirectory() {
    return localRepositoryDirectory;
  }

  public void setLocalRepositoryDirectory(String localRepositoryDirectory) {
    this.localRepositoryDirectory = localRepositoryDirectory;
  }

  public String getLinkDirectory() {
    return linkDirectory;
  }

  public void setLinkDirectory(String linkDirectory) {
    this.linkDirectory = linkDirectory;
  }

  public String getRepositoryUrl() {
    return repositoryUrl;
  }

  public void setRepositoryUrl(String repositoryUrl) {
    this.repositoryUrl = repositoryUrl;
  }

  public String getDistributionRepositoryUrl() {
    return distributionRepositoryUrl;
  }

  public void setDistributionRepositoryUrl(String distributionRepositoryUrl) {
    this.distributionRepositoryUrl = distributionRepositoryUrl;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public static class ParcelBuilder {

    public static ParcelBuilder get() {
      return new ParcelBuilder();
    }

    private Parcel parcel;

    private ParcelBuilder() {
      parcel = new Parcel();
    }

    public ParcelBuilder groupId(String groupId) {
      parcel.groupId = groupId;
      return this;
    }

    public ParcelBuilder artifactId(String artifactId) {
      parcel.artifactId = artifactId;
      return this;
    }

    public ParcelBuilder version(String version) {
      parcel.version = version;
      return this;
    }

    public ParcelBuilder classifier(String classifier) {
      parcel.classifier = classifier;
      return this;
    }

    public ParcelBuilder baseDirectory(String baseDirectory) {
      parcel.baseDirectory = baseDirectory;
      return this;
    }

    public ParcelBuilder parcelResourcesDirectory(String parcelResourcesDirectory) {
      parcel.parcelResourcesDirectory = parcelResourcesDirectory;
      return this;
    }

    public ParcelBuilder buildDirectory(String buildDirectory) {
      parcel.buildDirectory = buildDirectory;
      return this;
    }

    public ParcelBuilder parcelBuildDirectory(String parcelBuildDirectory) {
      parcel.parcelBuildDirectory = parcelBuildDirectory;
      return this;
    }

    public ParcelBuilder outputDirectory(String outputDirectory) {
      parcel.outputDirectory = outputDirectory;
      return this;
    }

    public ParcelBuilder linkDirectory(String linkDirectory) {
      parcel.linkDirectory = linkDirectory;
      return this;
    }

    public ParcelBuilder repositoryUrl(String repositoryUrl) {
      parcel.repositoryUrl = repositoryUrl;
      return this;
    }

    public ParcelBuilder distributionRepositoryUrl(String distributionRepositoryUrl) {
      parcel.distributionRepositoryUrl = distributionRepositoryUrl;
      return this;
    }

    public ParcelBuilder localRepositoryDirectory(String localRepositoryDirectory) {
      parcel.localRepositoryDirectory = localRepositoryDirectory;
      return this;
    }

    public ParcelBuilder type(String type) {
      parcel.type = type;
      return this;
    }

    public Parcel build() throws MojoExecutionException {
      parcel.isValid();
      return parcel;
    }

  }

}
