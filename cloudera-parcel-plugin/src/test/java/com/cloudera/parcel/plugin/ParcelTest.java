package com.cloudera.parcel.plugin;

import java.io.File;

import com.cloudera.parcel.plugin.Parcel.ParcelBuilder;
import org.apache.maven.plugin.MojoExecutionException;
import org.junit.Assert;
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

  private static final String PARCEL_VERSION_SHORT_ALT = "2.2.0";
  private static final String PARCEL_ARTIFACT_ID_ALT = "SPARK2";
  private static final String PARCEL_VERSION_LONG_ALT = "2.2.0.cloudera1-1.cdh5.12.0.p0.142354";

  private static final String PATH_EXPLODE = new File(".").getAbsolutePath() + "/target/test-sqoop-runtime";
  private static final String PATH_EXPLODE_LINK = new File(".").getAbsolutePath() + "/target/test-sqoop-runtime-link";
  private static final String PATH_WORKING = new File(".").getAbsolutePath().substring(0, new File(".").getAbsolutePath().length() - 2);

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
    Assert.assertEquals(PARCEL_VERSION_SHORT_ALT,
      ParcelBuilder.get().groupId(PARCEL_GROUP_ID).artifactId(PARCEL_ARTIFACT_ID_ALT).version(PARCEL_VERSION_LONG_ALT)
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
