package com.cloudera.plugin.mojo;

import java.util.Arrays;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.StringUtils;

import com.cloudera.plugin.Parcel;
import com.cloudera.plugin.Parcel.ParcelBuilder;

@Mojo(name = "build", requiresProject = true, defaultPhase = LifecyclePhase.PACKAGE)
public class Build extends AbstractMojo {

  @Parameter(defaultValue = "${project}", required = true, readonly = true)
  private MavenProject project;

  @Parameter(defaultValue = "${parcel.classifier}", required = false, readonly = true)
  private String parcelClassifier;

  @Parameter(defaultValue = "${project.build.directory}", required = true, readonly = true)
  private String buildDirectory;

  @Parameter(defaultValue = "${project.build.directory}/parcel", required = true, readonly = true)
  private String parcelBuildDirectory;

  @Parameter(defaultValue = "${project.basedir}/src/main/parcel", required = true, readonly = true)
  private String parcelResourcesDirectory;

  @Parameter(required = false)
  private List<Parcel> parcels;

  @Override
  public void execute() throws MojoExecutionException {
    if (parcels == null) {
      parcels = Arrays.asList(new Parcel[] {
          ParcelBuilder.get().groupId(project.getGroupId()).artifactId(project.getArtifactId()).version(project.getVersion())
              .classifier(StringUtils.isEmpty(parcelClassifier) ? Parcel.getOsDescriptor() : parcelClassifier)
              .parcelResourcesDirectory(parcelResourcesDirectory).buildDirectory(buildDirectory)
              .parcelBuildDirectory(parcelBuildDirectory).type(project.getPackaging()).build() });
    }
    for (Parcel parcel : parcels) {
      if (parcel.isValid()) {
        parcel.build(getLog());
      }
    }
  }

}
