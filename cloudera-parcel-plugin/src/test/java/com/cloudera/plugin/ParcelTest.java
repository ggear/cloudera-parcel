package com.cloudera.plugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ParcelTest {

  private static final String PARCEL_TYPE = "parcel";
  private static final String PARCEL_CLASSIFIER = "el6";
  private static final String PARCEL_VERSION = "1.4c5";
  private static final String PARCEL_VERSION_SHORT = "1.4";
  private static final String PARCEL_VERSION_LONG = "1.4-1.sqoop1.4.0.p0.88";
  private static final String PARCEL_ARTIFACT_ID = "SQOOP_TERADATA_CONNECTOR";
  private static final String PARCEL_GROUP_ID = "com.cloudera.parcel";
  private static final String PARCEL_REPO_URL = "http://archive.cloudera.com/sqoop-connectors/parcels";

  private static final String PATH_EXPLODE = new File(".").getAbsolutePath() + "/target/test-sqoop-runtime";
  private static final String PATH_EXPLODE_LINK = new File(".").getAbsolutePath() + "/target/test-sqoop-runtime-link";
  private static final String PATH_MAVEN_REPO = new File(".").getAbsolutePath() + "/target/local-repo-unit-test";

  private String osName;
  private String osVersion;

  @Before
  public void pushOsSystemProperties() {
    osName = System.getProperty("os.name");
    osVersion = System.getProperty("os.version");
  }

  @After
  public void popOsSystemProperties() {
    System.setProperty("os.name", osName);
    System.setProperty("os.version", osVersion);
  }

  @Test
  public void testGetOsDescriptor() {
    System.setProperty("os.name", "Mac OS X");
    System.setProperty("os.version", "10.11.3");
    Assert.assertEquals("elcapitan", Parcel.getOsDescriptor());
    System.setProperty("os.version", "10.11");
    Assert.assertEquals("elcapitan", Parcel.getOsDescriptor());
    System.setProperty("os.version", "10.11.SOME_OTHER_MINOR_VERSION");
    Assert.assertEquals("elcapitan", Parcel.getOsDescriptor());
    System.setProperty("os.name", "Linux");
    System.setProperty("os.version", "2.6.32-573.el6.x86_64");
    Assert.assertEquals("el6", Parcel.getOsDescriptor());
    System.setProperty("os.version", "SOME_VERSION.el6.SOME_VERSION");
    Assert.assertEquals("el6", Parcel.getOsDescriptor());
    System.setProperty("os.name", "Linux");
    System.setProperty("os.version", "3.10.0-327.10.1.el7.x86_6");
    Assert.assertEquals("el7", Parcel.getOsDescriptor());
    System.setProperty("os.version", "SOME_VERSION.el7.SOME_VERSION");
    Assert.assertEquals("el7", Parcel.getOsDescriptor());
    System.setProperty("os.name", "Linux");
    System.setProperty("os.version", "4.2.0-27-generic");
    Assert.assertEquals("trusty", Parcel.getOsDescriptor());
    System.setProperty("os.version", "4.2.0-SOME_OTHER_VERSION-generic");
    Assert.assertEquals("trusty", Parcel.getOsDescriptor());
  }

  @Test(expected = RuntimeException.class)
  public void testGetOsDescriptorBadName() {
    System.setProperty("os.name", "SOME GARBAGE");
    System.setProperty("os.version", "SOME GARBAGE");
    Assert.assertNull(Parcel.getOsDescriptor());
  }

  @Test(expected = RuntimeException.class)
  public void testGetOsDescriptorBadVersion() {
    System.setProperty("os.name", "Linux");
    System.setProperty("os.version", "SOME GARBAGE");
    Assert.assertNull(Parcel.getOsDescriptor());
  }

  @Test(expected = MojoExecutionException.class)
  public void testIsValidDefaults() throws MojoExecutionException {
    Assert.assertFalse(new Parcel().isValid());
  }

  @Test(expected = MojoExecutionException.class)
  public void testIsValidEmpty() throws MojoExecutionException {
    Assert.assertTrue(new Parcel("", "", "", "", "", "", "", true, "").isValid());
  }

  @Test()
  public void testIsValid() throws MojoExecutionException {
    Assert.assertTrue(new Parcel("a", "a", "a", "a", "a", "a", "a", true, "a").isValid());
  }

  @Test
  public void testGetArtifactName() throws MojoExecutionException {
    Assert.assertEquals(PARCEL_ARTIFACT_ID + "-" + PARCEL_VERSION + "-" + PARCEL_CLASSIFIER + "." + PARCEL_TYPE,
        new Parcel(PARCEL_REPO_URL, PARCEL_GROUP_ID, PARCEL_ARTIFACT_ID, PARCEL_VERSION, PARCEL_CLASSIFIER,
            PATH_EXPLODE, PATH_EXPLODE_LINK, true, PARCEL_TYPE).getArtifactName());
  }

  @Test
  public void testGetArtifactNameSansClassifierType() throws MojoExecutionException {
    Assert.assertEquals(PARCEL_ARTIFACT_ID + "-" + PARCEL_VERSION,
        new Parcel(PARCEL_REPO_URL, PARCEL_GROUP_ID, PARCEL_ARTIFACT_ID, PARCEL_VERSION, PARCEL_CLASSIFIER,
            PATH_EXPLODE, PATH_EXPLODE_LINK, true, PARCEL_TYPE).getArtifactNameSansClassifierType());
  }

  @Test
  public void testGetArtifactNamespace() throws MojoExecutionException {
    Assert.assertEquals(
        PARCEL_GROUP_ID + ":" + PARCEL_ARTIFACT_ID + ":" + PARCEL_TYPE + ":" + PARCEL_CLASSIFIER + ":" + PARCEL_VERSION,
        new Parcel(PARCEL_REPO_URL, PARCEL_GROUP_ID, PARCEL_ARTIFACT_ID, PARCEL_VERSION, PARCEL_CLASSIFIER,
            PATH_EXPLODE, PATH_EXPLODE_LINK, true, PARCEL_TYPE).getArtifactNamespace());
  }

  @Test
  public void testGetVersionShort() throws MojoExecutionException {
    Assert.assertEquals(PARCEL_VERSION_SHORT, new Parcel(PARCEL_REPO_URL, PARCEL_GROUP_ID, PARCEL_ARTIFACT_ID,
        PARCEL_VERSION_SHORT, PARCEL_CLASSIFIER, PATH_EXPLODE, PATH_EXPLODE_LINK, true, PARCEL_TYPE).getVersionShort());
    Assert.assertEquals(PARCEL_VERSION_SHORT, new Parcel(PARCEL_REPO_URL, PARCEL_GROUP_ID, PARCEL_ARTIFACT_ID,
        PARCEL_VERSION, PARCEL_CLASSIFIER, PATH_EXPLODE, PATH_EXPLODE_LINK, true, PARCEL_TYPE).getVersionShort());
    Assert.assertEquals(PARCEL_VERSION_SHORT, new Parcel(PARCEL_REPO_URL, PARCEL_GROUP_ID, PARCEL_ARTIFACT_ID,
        PARCEL_VERSION_LONG, PARCEL_CLASSIFIER, PATH_EXPLODE, PATH_EXPLODE_LINK, true, PARCEL_TYPE).getVersionShort());
  }

  @Test
  public void testGetRemoteUrl() throws MojoExecutionException {
    Assert.assertEquals(
        PARCEL_REPO_URL + "/" + PARCEL_VERSION_SHORT + "/" + PARCEL_ARTIFACT_ID + "-" + PARCEL_VERSION + "-"
            + PARCEL_CLASSIFIER + "." + PARCEL_TYPE,
        new Parcel(PARCEL_REPO_URL, PARCEL_GROUP_ID, PARCEL_ARTIFACT_ID, PARCEL_VERSION, PARCEL_CLASSIFIER,
            PATH_EXPLODE, PATH_EXPLODE_LINK, true, PARCEL_TYPE).getRemoteUrl());
  }

  @Test
  public void testGetLocalPath() throws MojoExecutionException {
    Assert.assertEquals(
        "/" + PARCEL_GROUP_ID.replaceAll("\\.", "/") + "/" + PARCEL_ARTIFACT_ID + "/" + PARCEL_VERSION + "/"
            + PARCEL_ARTIFACT_ID + "-" + PARCEL_VERSION + "-" + PARCEL_CLASSIFIER + "." + PARCEL_TYPE,
        new Parcel(PARCEL_REPO_URL, PARCEL_GROUP_ID, PARCEL_ARTIFACT_ID, PARCEL_VERSION, PARCEL_CLASSIFIER,
            PATH_EXPLODE, PATH_EXPLODE_LINK, true, PARCEL_TYPE).getLocalPath());
  }

  @Test(expected = MojoExecutionException.class)
  public void testDownloadSquattingHost() throws MojoExecutionException, IOException {
    Assert.assertFalse(new Parcel("http://some.non.existant.host.com/sqoop-connectors/parcels", PARCEL_GROUP_ID,
        PARCEL_ARTIFACT_ID, PARCEL_VERSION, PARCEL_CLASSIFIER, PATH_EXPLODE, PATH_EXPLODE_LINK, true, PARCEL_TYPE)
            .download(new SystemStreamLog(), PATH_MAVEN_REPO));
  }

  @Test(expected = MojoExecutionException.class)
  public void testDownloadBadHost() throws MojoExecutionException, IOException {
    Assert.assertFalse(new Parcel("http://KHAsdalj123lljasd/sqoop-connectors/parcels", PARCEL_GROUP_ID,
        PARCEL_ARTIFACT_ID, PARCEL_VERSION, PARCEL_CLASSIFIER, PATH_EXPLODE, PATH_EXPLODE_LINK, true, PARCEL_TYPE)
            .download(new SystemStreamLog(), PATH_MAVEN_REPO));
  }

  @Test(expected = MojoExecutionException.class)
  public void testDownloadBadPath() throws MojoExecutionException, IOException {
    Assert.assertFalse(new Parcel(PARCEL_REPO_URL, PARCEL_GROUP_ID, PARCEL_ARTIFACT_ID,
        "1.4c5.some.nonexistant.version", PARCEL_CLASSIFIER, PATH_EXPLODE, PATH_EXPLODE_LINK, true, PARCEL_TYPE)
            .download(new SystemStreamLog(), PATH_MAVEN_REPO));
  }

  @Test
  @SuppressWarnings("resource")
  public void testDownloadHash() throws MojoExecutionException, IOException {
    Assert
        .assertTrue(new Parcel(PARCEL_REPO_URL, PARCEL_GROUP_ID, PARCEL_ARTIFACT_ID, PARCEL_VERSION, PARCEL_CLASSIFIER,
            PATH_EXPLODE, PATH_EXPLODE_LINK, true, PARCEL_TYPE).download(new SystemStreamLog(), PATH_MAVEN_REPO));
    Assert
        .assertFalse(new Parcel(PARCEL_REPO_URL, PARCEL_GROUP_ID, PARCEL_ARTIFACT_ID, PARCEL_VERSION, PARCEL_CLASSIFIER,
            PATH_EXPLODE, PATH_EXPLODE_LINK, true, PARCEL_TYPE).download(new SystemStreamLog(), PATH_MAVEN_REPO));
    FileUtils.listFiles(new File(PATH_MAVEN_REPO), null, true).iterator().next().delete();
    Assert
        .assertTrue(new Parcel(PARCEL_REPO_URL, PARCEL_GROUP_ID, PARCEL_ARTIFACT_ID, PARCEL_VERSION, PARCEL_CLASSIFIER,
            PATH_EXPLODE, PATH_EXPLODE_LINK, true, PARCEL_TYPE).download(new SystemStreamLog(), PATH_MAVEN_REPO));
    Assert
        .assertFalse(new Parcel(PARCEL_REPO_URL, PARCEL_GROUP_ID, PARCEL_ARTIFACT_ID, PARCEL_VERSION, PARCEL_CLASSIFIER,
            PATH_EXPLODE, PATH_EXPLODE_LINK, true, PARCEL_TYPE).download(new SystemStreamLog(), PATH_MAVEN_REPO));
    FileUtils.listFiles(new File(PATH_MAVEN_REPO), null, true).toArray(new File[2])[1].delete();
    Assert
        .assertTrue(new Parcel(PARCEL_REPO_URL, PARCEL_GROUP_ID, PARCEL_ARTIFACT_ID, PARCEL_VERSION, PARCEL_CLASSIFIER,
            PATH_EXPLODE, PATH_EXPLODE_LINK, true, PARCEL_TYPE).download(new SystemStreamLog(), PATH_MAVEN_REPO));
    Assert
        .assertFalse(new Parcel(PARCEL_REPO_URL, PARCEL_GROUP_ID, PARCEL_ARTIFACT_ID, PARCEL_VERSION, PARCEL_CLASSIFIER,
            PATH_EXPLODE, PATH_EXPLODE_LINK, true, PARCEL_TYPE).download(new SystemStreamLog(), PATH_MAVEN_REPO));
    new FileOutputStream(FileUtils.listFiles(new File(PATH_MAVEN_REPO), null, true).iterator().next(), true)
        .getChannel().truncate(0).close();
    Assert
        .assertTrue(new Parcel(PARCEL_REPO_URL, PARCEL_GROUP_ID, PARCEL_ARTIFACT_ID, PARCEL_VERSION, PARCEL_CLASSIFIER,
            PATH_EXPLODE, PATH_EXPLODE_LINK, true, PARCEL_TYPE).download(new SystemStreamLog(), PATH_MAVEN_REPO));
    Assert
        .assertFalse(new Parcel(PARCEL_REPO_URL, PARCEL_GROUP_ID, PARCEL_ARTIFACT_ID, PARCEL_VERSION, PARCEL_CLASSIFIER,
            PATH_EXPLODE, PATH_EXPLODE_LINK, true, PARCEL_TYPE).download(new SystemStreamLog(), PATH_MAVEN_REPO));
    new FileOutputStream(FileUtils.listFiles(new File(PATH_MAVEN_REPO), null, true).toArray(new File[2])[1], true)
        .getChannel().truncate(0).close();
    Assert
        .assertTrue(new Parcel(PARCEL_REPO_URL, PARCEL_GROUP_ID, PARCEL_ARTIFACT_ID, PARCEL_VERSION, PARCEL_CLASSIFIER,
            PATH_EXPLODE, PATH_EXPLODE_LINK, true, PARCEL_TYPE).download(new SystemStreamLog(), PATH_MAVEN_REPO));
    Assert
        .assertFalse(new Parcel(PARCEL_REPO_URL, PARCEL_GROUP_ID, PARCEL_ARTIFACT_ID, PARCEL_VERSION, PARCEL_CLASSIFIER,
            PATH_EXPLODE, PATH_EXPLODE_LINK, true, PARCEL_TYPE).download(new SystemStreamLog(), PATH_MAVEN_REPO));
  }

  @Test
  @SuppressWarnings("resource")
  public void testDownloadNoHash() throws MojoExecutionException, IOException {
    Assert
        .assertTrue(new Parcel(PARCEL_REPO_URL, PARCEL_GROUP_ID, PARCEL_ARTIFACT_ID, PARCEL_VERSION, PARCEL_CLASSIFIER,
            PATH_EXPLODE, PATH_EXPLODE_LINK, false, PARCEL_TYPE).download(new SystemStreamLog(), PATH_MAVEN_REPO));
    Assert
        .assertFalse(new Parcel(PARCEL_REPO_URL, PARCEL_GROUP_ID, PARCEL_ARTIFACT_ID, PARCEL_VERSION, PARCEL_CLASSIFIER,
            PATH_EXPLODE, PATH_EXPLODE_LINK, false, PARCEL_TYPE).download(new SystemStreamLog(), PATH_MAVEN_REPO));
    FileUtils.listFiles(new File(PATH_MAVEN_REPO), null, true).iterator().next().delete();
    Assert
        .assertTrue(new Parcel(PARCEL_REPO_URL, PARCEL_GROUP_ID, PARCEL_ARTIFACT_ID, PARCEL_VERSION, PARCEL_CLASSIFIER,
            PATH_EXPLODE, PATH_EXPLODE_LINK, false, PARCEL_TYPE).download(new SystemStreamLog(), PATH_MAVEN_REPO));
    Assert
        .assertFalse(new Parcel(PARCEL_REPO_URL, PARCEL_GROUP_ID, PARCEL_ARTIFACT_ID, PARCEL_VERSION, PARCEL_CLASSIFIER,
            PATH_EXPLODE, PATH_EXPLODE_LINK, false, PARCEL_TYPE).download(new SystemStreamLog(), PATH_MAVEN_REPO));
    Assert
        .assertTrue(new Parcel(PARCEL_REPO_URL, PARCEL_GROUP_ID, PARCEL_ARTIFACT_ID, PARCEL_VERSION, PARCEL_CLASSIFIER,
            PATH_EXPLODE, PATH_EXPLODE_LINK, true, PARCEL_TYPE).download(new SystemStreamLog(), PATH_MAVEN_REPO));
    Assert
        .assertFalse(new Parcel(PARCEL_REPO_URL, PARCEL_GROUP_ID, PARCEL_ARTIFACT_ID, PARCEL_VERSION, PARCEL_CLASSIFIER,
            PATH_EXPLODE, PATH_EXPLODE_LINK, false, PARCEL_TYPE).download(new SystemStreamLog(), PATH_MAVEN_REPO));
    Assert
        .assertFalse(new Parcel(PARCEL_REPO_URL, PARCEL_GROUP_ID, PARCEL_ARTIFACT_ID, PARCEL_VERSION, PARCEL_CLASSIFIER,
            PATH_EXPLODE, PATH_EXPLODE_LINK, false, PARCEL_TYPE).download(new SystemStreamLog(), PATH_MAVEN_REPO));
    new FileOutputStream(FileUtils.listFiles(new File(PATH_MAVEN_REPO), null, true).iterator().next(), true)
        .getChannel().truncate(0).close();
    Assert
        .assertTrue(new Parcel(PARCEL_REPO_URL, PARCEL_GROUP_ID, PARCEL_ARTIFACT_ID, PARCEL_VERSION, PARCEL_CLASSIFIER,
            PATH_EXPLODE, PATH_EXPLODE_LINK, false, PARCEL_TYPE).download(new SystemStreamLog(), PATH_MAVEN_REPO));
    Assert
        .assertFalse(new Parcel(PARCEL_REPO_URL, PARCEL_GROUP_ID, PARCEL_ARTIFACT_ID, PARCEL_VERSION, PARCEL_CLASSIFIER,
            PATH_EXPLODE, PATH_EXPLODE_LINK, false, PARCEL_TYPE).download(new SystemStreamLog(), PATH_MAVEN_REPO));
    new File(FileUtils.listFiles(new File(PATH_MAVEN_REPO), null, true).iterator().next().getAbsolutePath() + ".sha1")
        .createNewFile();
    Assert
        .assertTrue(new Parcel(PARCEL_REPO_URL, PARCEL_GROUP_ID, PARCEL_ARTIFACT_ID, PARCEL_VERSION, PARCEL_CLASSIFIER,
            PATH_EXPLODE, PATH_EXPLODE_LINK, false, PARCEL_TYPE).download(new SystemStreamLog(), PATH_MAVEN_REPO));
    Assert
        .assertFalse(new Parcel(PARCEL_REPO_URL, PARCEL_GROUP_ID, PARCEL_ARTIFACT_ID, PARCEL_VERSION, PARCEL_CLASSIFIER,
            PATH_EXPLODE, PATH_EXPLODE_LINK, false, PARCEL_TYPE).download(new SystemStreamLog(), PATH_MAVEN_REPO));
  }

  @Test(expected = MojoExecutionException.class)
  public void testExplodeSquattingHost() throws MojoExecutionException, IOException {
    Assert.assertFalse(new Parcel("http://some.non.existant.host.com/sqoop-connectors/parcels", PARCEL_GROUP_ID,
        PARCEL_ARTIFACT_ID, PARCEL_VERSION, PARCEL_CLASSIFIER, PATH_EXPLODE, PATH_EXPLODE_LINK, true, PARCEL_TYPE)
            .explode(new SystemStreamLog(), PATH_MAVEN_REPO));
  }

  @Test(expected = MojoExecutionException.class)
  public void testExplodeBadHost() throws MojoExecutionException, IOException {
    Assert.assertFalse(new Parcel("http://KHAsdalj123lljasd/sqoop-connectors/parcels", PARCEL_GROUP_ID,
        PARCEL_ARTIFACT_ID, PARCEL_VERSION, PARCEL_CLASSIFIER, PATH_EXPLODE, PATH_EXPLODE_LINK, true, PARCEL_TYPE)
            .explode(new SystemStreamLog(), PATH_MAVEN_REPO));
  }

  @Test(expected = MojoExecutionException.class)
  public void testExplodeBadPath() throws MojoExecutionException, IOException {
    Assert.assertFalse(new Parcel(PARCEL_REPO_URL, PARCEL_GROUP_ID, PARCEL_ARTIFACT_ID,
        "1.4c5.some.nonexistant.version", PARCEL_CLASSIFIER, PATH_EXPLODE, PATH_EXPLODE_LINK, true, PARCEL_TYPE)
            .explode(new SystemStreamLog(), PATH_MAVEN_REPO));
  }

  @Test
  @SuppressWarnings("resource")
  public void testExplode() throws MojoExecutionException, IOException {
    Assert
        .assertTrue(new Parcel(PARCEL_REPO_URL, PARCEL_GROUP_ID, PARCEL_ARTIFACT_ID, PARCEL_VERSION, PARCEL_CLASSIFIER,
            PATH_EXPLODE, PATH_EXPLODE_LINK, true, PARCEL_TYPE).explode(new SystemStreamLog(), PATH_MAVEN_REPO));
    Assert
        .assertTrue(new Parcel(PARCEL_REPO_URL, PARCEL_GROUP_ID, PARCEL_ARTIFACT_ID, PARCEL_VERSION, PARCEL_CLASSIFIER,
            PATH_EXPLODE, PATH_EXPLODE_LINK, true, PARCEL_TYPE).explode(new SystemStreamLog(), PATH_MAVEN_REPO));
    FileUtils.listFiles(new File(PATH_MAVEN_REPO), null, true).iterator().next().delete();
    Assert
        .assertTrue(new Parcel(PARCEL_REPO_URL, PARCEL_GROUP_ID, PARCEL_ARTIFACT_ID, PARCEL_VERSION, PARCEL_CLASSIFIER,
            PATH_EXPLODE, PATH_EXPLODE_LINK, true, PARCEL_TYPE).explode(new SystemStreamLog(), PATH_MAVEN_REPO));
    Assert
        .assertTrue(new Parcel(PARCEL_REPO_URL, PARCEL_GROUP_ID, PARCEL_ARTIFACT_ID, PARCEL_VERSION, PARCEL_CLASSIFIER,
            PATH_EXPLODE, PATH_EXPLODE_LINK, true, PARCEL_TYPE).explode(new SystemStreamLog(), PATH_MAVEN_REPO));
    FileUtils.listFiles(new File(PATH_MAVEN_REPO), null, true).toArray(new File[2])[1].delete();
    Assert
        .assertTrue(new Parcel(PARCEL_REPO_URL, PARCEL_GROUP_ID, PARCEL_ARTIFACT_ID, PARCEL_VERSION, PARCEL_CLASSIFIER,
            PATH_EXPLODE, PATH_EXPLODE_LINK, true, PARCEL_TYPE).explode(new SystemStreamLog(), PATH_MAVEN_REPO));
    Assert
        .assertTrue(new Parcel(PARCEL_REPO_URL, PARCEL_GROUP_ID, PARCEL_ARTIFACT_ID, PARCEL_VERSION, PARCEL_CLASSIFIER,
            PATH_EXPLODE, PATH_EXPLODE_LINK, true, PARCEL_TYPE).explode(new SystemStreamLog(), PATH_MAVEN_REPO));
    new FileOutputStream(FileUtils.listFiles(new File(PATH_MAVEN_REPO), null, true).iterator().next(), true)
        .getChannel().truncate(0).close();
    Assert
        .assertTrue(new Parcel(PARCEL_REPO_URL, PARCEL_GROUP_ID, PARCEL_ARTIFACT_ID, PARCEL_VERSION, PARCEL_CLASSIFIER,
            PATH_EXPLODE, PATH_EXPLODE_LINK, true, PARCEL_TYPE).explode(new SystemStreamLog(), PATH_MAVEN_REPO));
    Assert
        .assertTrue(new Parcel(PARCEL_REPO_URL, PARCEL_GROUP_ID, PARCEL_ARTIFACT_ID, PARCEL_VERSION, PARCEL_CLASSIFIER,
            PATH_EXPLODE, PATH_EXPLODE_LINK, true, PARCEL_TYPE).explode(new SystemStreamLog(), PATH_MAVEN_REPO));
    new FileOutputStream(FileUtils.listFiles(new File(PATH_MAVEN_REPO), null, true).toArray(new File[2])[1], true)
        .getChannel().truncate(0).close();
    Assert
        .assertTrue(new Parcel(PARCEL_REPO_URL, PARCEL_GROUP_ID, PARCEL_ARTIFACT_ID, PARCEL_VERSION, PARCEL_CLASSIFIER,
            PATH_EXPLODE, PATH_EXPLODE_LINK, true, PARCEL_TYPE).explode(new SystemStreamLog(), PATH_MAVEN_REPO));
    Assert
        .assertTrue(new Parcel(PARCEL_REPO_URL, PARCEL_GROUP_ID, PARCEL_ARTIFACT_ID, PARCEL_VERSION, PARCEL_CLASSIFIER,
            PATH_EXPLODE, PATH_EXPLODE_LINK, true, PARCEL_TYPE).explode(new SystemStreamLog(), PATH_MAVEN_REPO));
  }

  @Before
  public void setUpTestMavenRepo() throws IOException {
    File pathMavenRepo = new File(PATH_MAVEN_REPO);
    FileUtils.deleteDirectory(pathMavenRepo);
    pathMavenRepo.mkdirs();
    File pathBuild = new File(PATH_EXPLODE);
    FileUtils.deleteDirectory(pathBuild);
    pathBuild.mkdirs();
  }

}
