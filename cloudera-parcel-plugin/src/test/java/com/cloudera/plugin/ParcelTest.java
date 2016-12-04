package com.cloudera.plugin;

import java.io.File;

import com.cloudera.plugin.Parcel.ParcelBuilder;
import org.apache.maven.plugin.MojoExecutionException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ParcelTest {

  private static final String PARCEL_TYPE = "parcel";
  private static final String PARCEL_CLASSIFIER = "el6";
  private static final String PARCEL_VERSION = "1.4c5";
  private static final String PARCEL_VERSION_SHORT = "1.4";
  private static final String PARCEL_VERSION_BASE = "sqoop_teradata_connector1.4";
  private static final String PARCEL_NAME_SHORT = "stc";
  private static final String PARCEL_NAMESPACE = "stc_1_4c5";
  private static final String PARCEL_VERSION_BASE_LONG = "sqoop1.4.0";
  private static final String PARCEL_VERSION_LONG = "1.4-1.sqoop1.4.0.p0.88";
  private static final String PARCEL_VERSION_LONG_ALTERNATE = "1.4-sqoop1.4.0";
  private static final String PARCEL_ARTIFACT_ID = "SQOOP_TERADATA_CONNECTOR";
  private static final String PARCEL_GROUP_ID = "com.cloudera.parcel";
  private static final String PARCEL_REPO_URL = "http://archive.cloudera.com/sqoop-connectors/parcels";

  private static final String PATH_EXPLODE = new File(".").getAbsolutePath() + "/target/test-sqoop-runtime";
  private static final String PATH_EXPLODE_LINK = new File(".").getAbsolutePath() + "/target/test-sqoop-runtime-link";
  private static final String PATH_WORKING = new File(".").getAbsolutePath().substring(0, new File(".").getAbsolutePath().length() - 2);

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
    Assert.assertFalse(ParcelBuilder.get().build().isValid());
  }

  @Test(expected = MojoExecutionException.class)
  public void testIsValidEmpty() throws MojoExecutionException {
    Assert.assertTrue(ParcelBuilder.get().groupId("").artifactId("").version("").type("").build().isValid());
  }

  @Test()
  public void testIsValid() throws MojoExecutionException {
    Assert.assertTrue(ParcelBuilder.get().groupId("a").artifactId("a").version("a").type("a").baseDirectory("a").build().isValid());
  }

  @Test
  public void testGetArtifactName() throws MojoExecutionException {
    Assert.assertEquals(PARCEL_ARTIFACT_ID + "-" + PARCEL_VERSION + "-" + PARCEL_CLASSIFIER + "." + PARCEL_TYPE,
      ParcelBuilder.get().groupId(PARCEL_GROUP_ID).artifactId(PARCEL_ARTIFACT_ID).version(PARCEL_VERSION).classifier(PARCEL_CLASSIFIER)
        .baseDirectory(PATH_WORKING).outputDirectory(PATH_EXPLODE).linkDirectory(PATH_EXPLODE_LINK).type(PARCEL_TYPE).build()
        .getArtifactName());
  }

  @Test
  public void testGetNameShort() throws MojoExecutionException {
    Assert.assertEquals(PARCEL_NAME_SHORT,
      ParcelBuilder.get().groupId(PARCEL_GROUP_ID).artifactId(PARCEL_ARTIFACT_ID).version(PARCEL_VERSION).classifier(PARCEL_CLASSIFIER)
        .baseDirectory(PATH_WORKING).outputDirectory(PATH_EXPLODE).linkDirectory(PATH_EXPLODE_LINK).type(PARCEL_TYPE).build()
        .getNameShort());
    Assert.assertEquals(PARCEL_NAME_SHORT,
      ParcelBuilder.get().groupId(PARCEL_GROUP_ID).artifactId(PARCEL_ARTIFACT_ID).version(PARCEL_VERSION + "-SNAPSHOT")
        .classifier(PARCEL_CLASSIFIER).baseDirectory(PATH_WORKING).outputDirectory(PATH_EXPLODE).linkDirectory(PATH_EXPLODE_LINK)
        .type(PARCEL_TYPE).build().getNameShort());
  }

  @Test
  public void testGetNameSpace() throws MojoExecutionException {
    Assert.assertEquals(PARCEL_NAMESPACE,
      ParcelBuilder.get().groupId(PARCEL_GROUP_ID).artifactId(PARCEL_ARTIFACT_ID).version(PARCEL_VERSION).classifier(PARCEL_CLASSIFIER)
        .baseDirectory(PATH_WORKING).outputDirectory(PATH_EXPLODE).linkDirectory(PATH_EXPLODE_LINK).type(PARCEL_TYPE).build()
        .getNamespace());
    Assert.assertEquals(PARCEL_NAMESPACE + "_snapshot",
      ParcelBuilder.get().groupId(PARCEL_GROUP_ID).artifactId(PARCEL_ARTIFACT_ID).version(PARCEL_VERSION + "-SNAPSHOT")
        .classifier(PARCEL_CLASSIFIER).baseDirectory(PATH_WORKING).outputDirectory(PATH_EXPLODE).linkDirectory(PATH_EXPLODE_LINK)
        .type(PARCEL_TYPE).build().getNamespace());
  }

  @Test
  public void testGetArtifactNameSansClassifierType() throws MojoExecutionException {
    Assert.assertEquals(PARCEL_ARTIFACT_ID + "-" + PARCEL_VERSION,
      ParcelBuilder.get().groupId(PARCEL_GROUP_ID).artifactId(PARCEL_ARTIFACT_ID).version(PARCEL_VERSION).classifier(PARCEL_CLASSIFIER)
        .baseDirectory(PATH_WORKING).outputDirectory(PATH_EXPLODE).linkDirectory(PATH_EXPLODE_LINK).type(PARCEL_TYPE).build()
        .getArtifactNameSansClassifierType());
  }

  @Test
  public void testGetArtifactNamespace() throws MojoExecutionException {
    Assert.assertEquals(PARCEL_GROUP_ID + ":" + PARCEL_ARTIFACT_ID + ":" + PARCEL_TYPE + ":" + PARCEL_CLASSIFIER + ":" + PARCEL_VERSION,
      ParcelBuilder.get().groupId(PARCEL_GROUP_ID).artifactId(PARCEL_ARTIFACT_ID).version(PARCEL_VERSION).classifier(PARCEL_CLASSIFIER)
        .baseDirectory(PATH_WORKING).outputDirectory(PATH_EXPLODE).linkDirectory(PATH_EXPLODE_LINK).type(PARCEL_TYPE).build()
        .getArtifactNamespace());
  }

  @Test
  public void testGetVersionEscaped() throws MojoExecutionException {
    Assert.assertEquals(PARCEL_VERSION_SHORT.replace(".", "_").replace("-", "_"),
      ParcelBuilder.get().groupId(PARCEL_GROUP_ID).artifactId(PARCEL_ARTIFACT_ID).version(PARCEL_VERSION_SHORT)
        .classifier(PARCEL_CLASSIFIER).baseDirectory(PATH_WORKING).outputDirectory(PATH_EXPLODE).linkDirectory(PATH_EXPLODE_LINK)
        .type(PARCEL_TYPE).build().getVersionEscaped());
    Assert.assertEquals(PARCEL_VERSION.replace(".", "_").replace("-", "_"),
      ParcelBuilder.get().groupId(PARCEL_GROUP_ID).artifactId(PARCEL_ARTIFACT_ID).version(PARCEL_VERSION).classifier(PARCEL_CLASSIFIER)
        .baseDirectory(PATH_WORKING).outputDirectory(PATH_EXPLODE).linkDirectory(PATH_EXPLODE_LINK).type(PARCEL_TYPE).build()
        .getVersionEscaped());
    Assert.assertEquals(PARCEL_VERSION_LONG.replace(".", "_").replace("-", "_"),
      ParcelBuilder.get().groupId(PARCEL_GROUP_ID).artifactId(PARCEL_ARTIFACT_ID).version(PARCEL_VERSION_LONG)
        .classifier(PARCEL_CLASSIFIER).baseDirectory(PATH_WORKING).outputDirectory(PATH_EXPLODE).linkDirectory(PATH_EXPLODE_LINK)
        .type(PARCEL_TYPE).build().getVersionEscaped());
  }

  @Test
  public void testGetVersionShort() throws MojoExecutionException {
    Assert.assertEquals(PARCEL_VERSION_SHORT,
      ParcelBuilder.get().groupId(PARCEL_GROUP_ID).artifactId(PARCEL_ARTIFACT_ID).version(PARCEL_VERSION_SHORT)
        .classifier(PARCEL_CLASSIFIER).baseDirectory(PATH_WORKING).outputDirectory(PATH_EXPLODE).linkDirectory(PATH_EXPLODE_LINK)
        .type(PARCEL_TYPE).build().getVersionShort());
    Assert.assertEquals(PARCEL_VERSION_SHORT,
      ParcelBuilder.get().groupId(PARCEL_GROUP_ID).artifactId(PARCEL_ARTIFACT_ID).version(PARCEL_VERSION).classifier(PARCEL_CLASSIFIER)
        .baseDirectory(PATH_WORKING).outputDirectory(PATH_EXPLODE).linkDirectory(PATH_EXPLODE_LINK).type(PARCEL_TYPE).build()
        .getVersionShort());
    Assert.assertEquals(PARCEL_VERSION_SHORT,
      ParcelBuilder.get().groupId(PARCEL_GROUP_ID).artifactId(PARCEL_ARTIFACT_ID).version(PARCEL_VERSION_LONG)
        .classifier(PARCEL_CLASSIFIER).baseDirectory(PATH_WORKING).outputDirectory(PATH_EXPLODE).linkDirectory(PATH_EXPLODE_LINK)
        .type(PARCEL_TYPE).build().getVersionShort());
  }

  @Test
  public void testGetVersionBase() throws MojoExecutionException {
    Assert.assertEquals(PARCEL_VERSION_BASE,
      ParcelBuilder.get().groupId(PARCEL_GROUP_ID).artifactId(PARCEL_ARTIFACT_ID).version(PARCEL_VERSION_SHORT)
        .classifier(PARCEL_CLASSIFIER).baseDirectory(PATH_WORKING).outputDirectory(PATH_EXPLODE).linkDirectory(PATH_EXPLODE_LINK)
        .type(PARCEL_TYPE).build().getVersionBase());
    Assert.assertEquals(PARCEL_VERSION_BASE,
      ParcelBuilder.get().groupId(PARCEL_GROUP_ID).artifactId(PARCEL_ARTIFACT_ID).version(PARCEL_VERSION).classifier(PARCEL_CLASSIFIER)
        .baseDirectory(PATH_WORKING).outputDirectory(PATH_EXPLODE).linkDirectory(PATH_EXPLODE_LINK).type(PARCEL_TYPE).build()
        .getVersionBase());
    Assert.assertEquals(PARCEL_VERSION_BASE,
      ParcelBuilder.get().groupId(PARCEL_GROUP_ID).artifactId(PARCEL_ARTIFACT_ID).version(PARCEL_VERSION_LONG)
        .classifier(PARCEL_CLASSIFIER).baseDirectory(PATH_WORKING).outputDirectory(PATH_EXPLODE).linkDirectory(PATH_EXPLODE_LINK)
        .type(PARCEL_TYPE).build().getVersionBase());
    Assert.assertEquals(PARCEL_VERSION_BASE_LONG,
      ParcelBuilder.get().groupId(PARCEL_GROUP_ID).artifactId(PARCEL_ARTIFACT_ID).version(PARCEL_VERSION_LONG_ALTERNATE)
        .classifier(PARCEL_CLASSIFIER).baseDirectory(PATH_WORKING).outputDirectory(PATH_EXPLODE).linkDirectory(PATH_EXPLODE_LINK)
        .type(PARCEL_TYPE).build().getVersionBase());
  }

  @Test
  public void testGetRemoteUrl() throws MojoExecutionException {
    Assert.assertEquals(
      PARCEL_REPO_URL + "/" + PARCEL_VERSION_SHORT + "/" + PARCEL_ARTIFACT_ID + "-" + PARCEL_VERSION + "-" + PARCEL_CLASSIFIER + "."
        + PARCEL_TYPE,
      ParcelBuilder.get().groupId(PARCEL_GROUP_ID).artifactId(PARCEL_ARTIFACT_ID).version(PARCEL_VERSION).classifier(PARCEL_CLASSIFIER)
        .baseDirectory(PATH_WORKING).outputDirectory(PATH_EXPLODE).linkDirectory(PATH_EXPLODE_LINK).type(PARCEL_TYPE).build()
        .getRemoteUrl(PARCEL_REPO_URL));
  }

  @Test
  public void testGetLocalPath() throws MojoExecutionException {
    Assert.assertEquals(
      "/" + PARCEL_GROUP_ID.replaceAll("\\.", "/") + "/" + PARCEL_ARTIFACT_ID + "/" + PARCEL_VERSION + "/" + PARCEL_ARTIFACT_ID + "-"
        + PARCEL_VERSION + "-" + PARCEL_CLASSIFIER + "." + PARCEL_TYPE,
      ParcelBuilder.get().groupId(PARCEL_GROUP_ID).artifactId(PARCEL_ARTIFACT_ID).version(PARCEL_VERSION).classifier(PARCEL_CLASSIFIER)
        .baseDirectory(PATH_WORKING).outputDirectory(PATH_EXPLODE).linkDirectory(PATH_EXPLODE_LINK).type(PARCEL_TYPE).build()
        .getLocalPath());
  }

}
