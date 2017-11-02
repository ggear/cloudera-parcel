package com.cloudera.parcel.library;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.io.IOUtils;

public class ParcelUtil {

  private static final ImmutableMap<String, ImmutableMap<String, String>> OS_NAME_VERSION_DESCRIPTOR = ImmutableMap.of(
    "Mac OS X",
    ImmutableMap.of(
      "10\\.11.*", "elcapitan",
      "10\\.12.*", "sierra",
      "10\\.13.*", "sierra" // Hack while awaiting compilation of Kudu for High Sierra
    ), //
    "Linux",
    ImmutableMap.of(
      "6\\..*", "el6",
      "7\\..*", "el7",
      "14\\.04.*", "trusty",
      "16\\.04.*", "xenial"
    )
  );

  public static final Pattern REGEX_LSB_RELEASE = Pattern.compile("^Release:\\s+(.*)");

  /**
   * Get the host OS descriptor
   *
   * @return the OS descriptor
   * @throws RuntimeException if OS descriptor could not be determined
   */
  public static String getOsDescriptor() {
    return getOsDescriptor(null);
  }

  static String getOsDescriptor(String lsbRelease) {
    Exception thrown = null;
    try {
      List<String> versions = new ArrayList<>();
      if (System.getProperty("os.name").equals("Linux")) {
        if (lsbRelease == null) {
          lsbRelease = IOUtils.toString(new ProcessBuilder().command("lsb_release", "-a").start().getInputStream());
        }
        for (String lsbReleaseLines : lsbRelease.split("\n")) {
          Matcher regexMatcher = REGEX_LSB_RELEASE.matcher(lsbReleaseLines);
          if (regexMatcher.find()) {
            versions.add(regexMatcher.group(1));
          }
        }
      } else {
        versions.add(System.getProperty("os.version"));
      }
      Map<String, String> osVersionDescriptor = OS_NAME_VERSION_DESCRIPTOR.get(System.getProperty("os.name"));
      if (osVersionDescriptor != null) {
        for (String versionRegEx : osVersionDescriptor.keySet()) {
          for (String version : versions) {
            if (version.matches(versionRegEx)) {
              return osVersionDescriptor.get(versionRegEx);
            }
          }
        }
      }
    } catch (Exception exception) {
      thrown = exception;
    }
    String message = "Could not determine OS descriptor from system property os.name [" + System.getProperty("os.name")
      + "] and os.version [" + System.getProperty("os.version") + "] from the regexp mapping " + OS_NAME_VERSION_DESCRIPTOR
      + " and output of 'lsb_release -a'. If your OS looks like a supported platform, you can override this parcels [classifier]" +
      " in your POM or via the command line.";
    if (thrown == null) {
      throw new RuntimeException(message);
    } else {
      throw new RuntimeException(message, thrown);
    }
  }

}
