package com.cloudera.plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.console.ConsoleLogger;
import org.codehaus.plexus.util.StringUtils;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.common.collect.ImmutableMap;

public class Parcel {

  public boolean isValid() throws MojoExecutionException {
    List<String> paramaters = new ArrayList<>();
    if (StringUtils.isEmpty(repositoryUrl)) {
      paramaters.add("repositoryUrl");
    }
    if (StringUtils.isEmpty(groupId)) {
      paramaters.add("groupId");
    }
    if (StringUtils.isEmpty(artifactId)) {
      paramaters.add("artifactId");
    }
    if (StringUtils.isEmpty(version)) {
      paramaters.add("version");
    }
    if (StringUtils.isEmpty(type)) {
      paramaters.add("type");
    }
    if (!paramaters.isEmpty()) {
      throw new MojoExecutionException("The required parameters " + paramaters + " are missing or invalid");
    }
    return true;
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

  public String getRemoteUrl() throws MojoExecutionException {
    return isValid() ? repositoryUrl + "/" + getVersionShort() + "/" + artifactId + "-" + version
        + (StringUtils.isEmpty(classifier) ? "" : "-" + classifier) + "." + type : null;
  }

  public String getLocalPath() throws MojoExecutionException {
    return isValid() ? "/" + groupId.replaceAll("\\.", "/") + "/" + artifactId + "/" + version + "/" + getArtifactName()
        : null;
  }

  public boolean download(Log log, String dirRepository) throws MojoExecutionException {
    boolean downloaded = false;
    GenericUrl remoteUrl = new GenericUrl(getRemoteUrl());
    GenericUrl remoteUrlSha1 = new GenericUrl(getRemoteUrl() + SUFFIX_SHA1);
    File localPath = new File(dirRepository, getLocalPath());
    File localPathSha1 = new File(dirRepository, getLocalPath() + SUFFIX_SHA1);
    if (localPath.exists() && localPathSha1.exists()) {
      if (!assertSha1(localPath, localPathSha1)) {
        localPath.delete();
        localPathSha1.delete();
      }
    } else if (localPath.exists() && !localPathSha1.exists() && assertHash) {
      localPath.delete();
    } else if (!localPath.exists() && localPathSha1.exists()) {
      localPathSha1.delete();
    }
    if (!localPath.exists()) {
      log.info("Downloding: " + remoteUrl);
      if ((downloaded = downloadHttpResource(remoteUrl, localPath)) && assertHash) {
        if ((downloaded = downloadHttpResource(remoteUrlSha1, localPathSha1))
            && !assertSha1(localPath, localPathSha1)) {
          localPath.delete();
          localPathSha1.delete();
          throw new MojoExecutionException(
              "Downloaded file from (" + remoteUrl + ") failed to match checksum (" + remoteUrlSha1 + ")");
        }
      }
    }
    if (downloaded) {
      log.info("Downloded: " + remoteUrl);
    }
    return downloaded;
  }

  public boolean explode(Log log, String dirRepository) throws MojoExecutionException {
    download(log, dirRepository);
    File explodedPath = StringUtils.isEmpty(outputDirectory) ? new File(dirRepository, getLocalPath()).getParentFile()
        : new File(outputDirectory);
    String explodedPathRoot = explodedPath.toString() + File.separator + getArtifactNameSansClassifierType();
    boolean exploded = new File(explodedPath, getArtifactNameSansClassifierType()).exists();
    if (!exploded) {
      File localPath = new File(dirRepository, getLocalPath());
      log.info("Exploding: " + getArtifactNamespace());
      try {
        explodedPath.mkdirs();
        TarGZipUnArchiver unarchiver = new TarGZipUnArchiver();
        unarchiver.enableLogging(new ConsoleLogger(Logger.LEVEL_DISABLED, "Logger"));
        unarchiver.setSourceFile(localPath);
        unarchiver.setDestDirectory(explodedPath);
        unarchiver.extract();
        exploded = true;
        log.info("Exploded: " + getArtifactNamespace());
      } catch (Exception exception) {
        throw new MojoExecutionException("Failed to explode artifact [" + getArtifactNamespace() + "] from ["
            + localPath + "] to [" + explodedPath + "]", exception);
      }
    }
    try {
      if (StringUtils.isNotEmpty(linkDirectory)
          && Files.notExists(Paths.get(linkDirectory), LinkOption.NOFOLLOW_LINKS)) {
        new File(linkDirectory).getParentFile().mkdirs();
        Files.createSymbolicLink(Paths.get(linkDirectory), Paths.get(explodedPathRoot));
      }
    } catch (Exception exception) {
      throw new MojoExecutionException("Failed to sym link to exploded artifact [" + getArtifactNamespace() + "] from ["
          + explodedPathRoot + "] to [" + linkDirectory + "]", exception);
    }
    return exploded;
  }

  public boolean build(Log log, String dirBuild, String dirOutput, String dirExecutable) throws MojoExecutionException {
    File buildPath = new File(dirBuild, getArtifactName());
    File buildPathSha1 = new File(dirBuild, getArtifactName() + SUFFIX_SHA1);
    File ouputPath = new File(dirOutput);
    buildPath.delete();
    buildPathSha1.delete();
    File parcelRepoRootPath = new File(dirBuild, DIR_PARCEL_REPO + File.separator + artifactId.toLowerCase()
        + File.separator + DIR_PARCEL_REPO_TYPE + File.separator + getVersionShort());
    File parcelRepoPath = new File(parcelRepoRootPath, buildPath.getName());
    File parcelRepoPathSha1 = new File(parcelRepoRootPath, buildPathSha1.getName());
    parcelRepoRootPath.mkdirs();
    parcelRepoPath.delete();
    parcelRepoPathSha1.delete();
    try {
      TarArchiver archiver = new TarArchiver();
      archiver.setCompression(TarCompressionMethod.gzip);
      DefaultFileSet fileSet = new DefaultFileSet();
      fileSet.setDirectory(ouputPath);
      if (dirExecutable != null) {
        fileSet.setExcludes(new String[] { dirExecutable, });
      }
      archiver.addFileSet(fileSet);
      if (dirExecutable != null) {
        archiver.setFileMode(0755);
        fileSet = new DefaultFileSet();
        fileSet.setDirectory(ouputPath);
        fileSet.setIncludes(new String[] { dirExecutable, });
        archiver.addFileSet(fileSet);
      }
      archiver.setDestFile(buildPath);
      archiver.createArchive();
      FileUtils.writeStringToFile(buildPathSha1, calculateSha1(buildPath) + "\n");
      FileUtils.copyFile(buildPath, parcelRepoPath);
      FileUtils.copyFile(buildPathSha1, parcelRepoPathSha1);
      return true;
    } catch (Exception exception) {
      throw new MojoExecutionException(
          "Failed to build artifact " + getArtifactNamespace() + " from (" + ouputPath + ") to (" + buildPath + ")",
          exception);
    }
  }

  public boolean install(Log log, String dirBuild, String dirRepository) throws MojoExecutionException {
    File buildPath = new File(dirBuild, getArtifactName());
    File buildPathSha1 = new File(dirBuild, getArtifactName() + SUFFIX_SHA1);
    File repositoryPath = new File(dirRepository, getLocalPath()).getParentFile();
    try {
      FileUtils.copyFileToDirectory(buildPath, repositoryPath);
      FileUtils.copyFileToDirectory(buildPathSha1, repositoryPath);
    } catch (Exception exception) {
      throw new MojoExecutionException("Failed to install artifact " + getArtifactNamespace() + " from (" + buildPath
          + ") to (" + repositoryPath + ")", exception);
    }
    return true;
  }

  private boolean downloadHttpResource(GenericUrl remote, File local) throws MojoExecutionException {
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
      throw new MojoExecutionException("Failed to download artifact " + getArtifactNamespace() + " from repo ("
          + repositoryUrl + ") to (" + local + ")", exception);
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
      throw new MojoExecutionException("Could not create SHA1 of file (" + file + ")");
    } finally {
      IOUtils.closeQuietly(input);
    }
  }

  private boolean assertSha1(File file, File fileSha1) throws MojoExecutionException {
    InputStream input = null;
    try {
      return IOUtils.toString(input = new FileInputStream(fileSha1)).trim().toUpperCase()
          .equals(calculateSha1(file).toUpperCase());
    } catch (Exception exception) {
      throw new MojoExecutionException("Could not load file (" + fileSha1 + ")");
    } finally {
      IOUtils.closeQuietly(input);
    }
  }

  private static final String SUFFIX_SHA1 = ".sha1";

  private static final String DIR_PARCEL_REPO = "parcel-repo";
  private static final String DIR_PARCEL_REPO_TYPE = "parcels";

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

  public static String getOsDescriptor() {
    Map<String, String> osVersionDescriptor = OS_NAME_VERSION_DESCRIPTOR.get(System.getProperty("os.name"));
    if (osVersionDescriptor != null) {
      for (String versionRegEx : osVersionDescriptor.keySet())
        if (System.getProperty("os.version").matches(versionRegEx)) {
          return osVersionDescriptor.get(versionRegEx);
        }
    }
    throw new RuntimeException("Could not determine OS Descritor from system property os.name ["
        + System.getProperty("os.name") + "] and os.version [" + System.getProperty("os.version")
        + "] from OS name, version regular expression and descriptor mapping " + OS_NAME_VERSION_DESCRIPTOR
        + ", it is suggested you overirde this parcels [calssifier] on the command line.");
  }

  @Parameter(required = false, defaultValue = "http://archive.cloudera.com/cdh5/parcels/latest")
  private String repositoryUrl = "http://archive.cloudera.com/cdh5/parcels/latest";

  @Parameter(required = false, defaultValue = "com.cloudera.parcel")
  private String groupId = "com.cloudera.parcel";

  @Parameter(required = true)
  private String artifactId;

  @Parameter(required = true)
  private String version;

  @Parameter(required = false, defaultValue = "")
  private String classifier = getOsDescriptor();

  @Parameter(required = false, defaultValue = "")
  private String outputDirectory = "";

  @Parameter(required = false, defaultValue = "")
  private String linkDirectory = "";

  @Parameter(required = false, defaultValue = "true")
  private Boolean assertHash = Boolean.TRUE;

  @Parameter(required = false, defaultValue = "parcel")
  private String type = "parcel";

  public Parcel() {
  }

  public Parcel(String repositoryUrl, String groupId, String artifactId, String version, String classifier,
      String outputDirectory, String linkDirectory, Boolean assertHash, String type) {
    this.repositoryUrl = repositoryUrl;
    this.groupId = groupId;
    this.artifactId = artifactId;
    this.version = version;
    this.classifier = classifier;
    this.outputDirectory = outputDirectory;
    this.linkDirectory = linkDirectory;
    this.assertHash = assertHash;
    this.type = type;
  }

  public Parcel(String groupId, String artifactId, String version, String classifier, String type) {
    this.groupId = groupId;
    this.artifactId = artifactId;
    this.version = version;
    this.classifier = classifier;
    this.type = type;
  }

  public String getRepositoryUrl() {
    return repositoryUrl;
  }

  public void setRepositoryUrl(String repositoryUrl) {
    this.repositoryUrl = repositoryUrl;
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

  public String getOutputDirectory() {
    return outputDirectory;
  }

  public void setOutputDirectory(String outputDirectory) {
    this.outputDirectory = outputDirectory;
  }

  public String getLinkDirectory() {
    return linkDirectory;
  }

  public void setLinkDirectory(String linkDirectory) {
    this.linkDirectory = linkDirectory;
  }

  public Boolean getAssertHash() {
    return assertHash;
  }

  public void setAssertHash(Boolean assertHash) {
    this.assertHash = assertHash;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

}
