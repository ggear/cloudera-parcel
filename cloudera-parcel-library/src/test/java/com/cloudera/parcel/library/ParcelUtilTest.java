package com.cloudera.parcel.library;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ParcelUtilTest {

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
    Assert.assertEquals("elcapitan", ParcelUtil.getOsDescriptor());
    System.setProperty("os.version", "10.11");
    Assert.assertEquals("elcapitan", ParcelUtil.getOsDescriptor());
    System.setProperty("os.version", "10.11.SOME_OTHER_MINOR_VERSION");
    Assert.assertEquals("elcapitan", ParcelUtil.getOsDescriptor());
    System.setProperty("os.name", "Linux");
    System.setProperty("os.version", "2.6.32-573.el6.x86_64");
    Assert.assertEquals("el6", ParcelUtil.getOsDescriptor("LSB Version:    " +
      ":base-4.0-amd64:base-4.0-noarch:core-4.0-amd64:core-4.0-noarch" +
      ":graphics-4.0-amd64:graphics-4.0-noarch:printing-4.0-amd64:printing-4.0-noarch\n" +
      "Distributor ID: CentOS\n" +
      "Description:    CentOS release 6.4 (Final)\n" +
      "Release:        6.4\n" +
      "Codename:       Final"));
    System.setProperty("os.version", "SOME_VERSION.el6.SOME_VERSION");
    Assert.assertEquals("el6", ParcelUtil.getOsDescriptor("LSB Version:    " +
      ":base-4.0-amd64:base-4.0-noarch:core-4.0-amd64:core-4.0-noarch" +
      ":graphics-4.0-amd64:graphics-4.0-noarch:printing-4.0-amd64:printing-4.0-noarch\n" +
      "Distributor ID: CentOS\n" +
      "Description:    CentOS release 6.4 (Final)\n" +
      "Release:   \t\t     6.SOME_VERSION\n" +
      "Codename:       Final"));
    System.setProperty("os.name", "Linux");
    System.setProperty("os.version", "3.10.0-327.10.1.el7.x86_6");
    Assert.assertEquals("el7", ParcelUtil.getOsDescriptor("LSB Version:\t:core-4.1-amd64:core-4.1-noarch\n" +
      "Distributor ID:\tCentOS\n" +
      "Description:\tCentOS Linux release 7.2.1511 (Core) \n" +
      "Release:\t7.2.1511\n" +
      "Codename:\tCore"));
    System.setProperty("os.version", "SOME_VERSION.el7.SOME_VERSION");
    Assert.assertEquals("el7", ParcelUtil.getOsDescriptor("LSB Version:\t:core-4.1-amd64:core-4.1-noarch\n" +
      "Distributor ID:\tCentOS\n" +
      "Description:\tCentOS Linux release 7.2.1511 (Core) \n" +
      "Release:\t 7.SOME_VERSION\n" +
      "Codename:\tCore"));
    System.setProperty("os.name", "Linux");
    System.setProperty("os.version", "4.2.0-27-generic");
    Assert.assertEquals("trusty", ParcelUtil.getOsDescriptor("No LSB modules are available.\n" +
      "Distributor ID: Ubuntu\n" +
      "Description:    Ubuntu 14.04.5 LTS\n" +
      "Release:    14.04\n" +
      "Codename:   trusty"));
    System.setProperty("os.version", "4.2.0-SOME_OTHER_VERSION-generic");
    Assert.assertEquals("trusty", ParcelUtil.getOsDescriptor("No LSB modules are available.\n" +
      "Distributor ID: Ubuntu\n" +
      "Description:    Ubuntu 14.04.5 LTS\n" +
      "Release:    14.04 SOME OTHER CLASSIFIER\n" +
      "Codename:   trusty"));
    System.setProperty("os.name", "Linux");
    System.setProperty("os.version", "4.2.0-27-generic");
    Assert.assertEquals("xenial", ParcelUtil.getOsDescriptor("No LSB modules are available.\n" +
      "Distributor ID: Ubuntu\n" +
      "Description:    Ubuntu 16.04.2 LTS\n" +
      "Release:        16.04\n" +
      "Codename:       xenial"));
    System.setProperty("os.version", "4.2.0-SOME_OTHER_VERSION-generic");
    Assert.assertEquals("xenial", ParcelUtil.getOsDescriptor("No LSB modules are available.\n" +
      "Distributor ID: Ubuntu\n" +
      "Description:    Ubuntu 16.04.2 LTS\n" +
      "Release: \t\t       16.04. SOME OTHER CLASSIFIER\n" +
      "Codename:       xenial"));
    System.setProperty("os.name", "Linux");
    System.setProperty("os.version", "3.10.0-327.10.1.el7.x86_6");
    Assert.assertEquals("xenial", ParcelUtil.getOsDescriptor("No LSB modules are available.\n" +
      "Distributor ID: Ubuntu\n" +
      "Description:    Ubuntu 16.04.2 LTS\n" +
      "Release:        16.04\n" +
      "Codename:       xenial"));
    System.setProperty("os.version", "3.10.0-327.10.1.el7.x86_6");
    Assert.assertEquals("xenial", ParcelUtil.getOsDescriptor("No LSB modules are available.\n" +
      "Distributor ID: Ubuntu\n" +
      "Description:    Ubuntu 16.04.2 LTS\n" +
      "Release: \t\t       16.04. SOME OTHER CLASSIFIER\n" +
      "Codename:       xenial"));
  }

  @Test(expected = RuntimeException.class)
  public void testGetOsDescriptorBadName() {
    System.setProperty("os.name", "SOME GARBAGE");
    System.setProperty("os.version", "SOME GARBAGE");
    Assert.assertNull(ParcelUtil.getOsDescriptor());
  }

  @Test(expected = RuntimeException.class)
  public void testGetOsDescriptorBadVersion() {
    System.setProperty("os.name", "Linux");
    System.setProperty("os.version", "SOME GARBAGE");
    Assert.assertNull(ParcelUtil.getOsDescriptor());
  }

}
