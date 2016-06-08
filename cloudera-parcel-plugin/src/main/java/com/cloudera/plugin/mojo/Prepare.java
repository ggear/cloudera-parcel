package com.cloudera.plugin.mojo;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.StringUtils;

import com.cloudera.plugin.Parcel;

@Mojo(name = "prepare", requiresProject = true, defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class Prepare extends AbstractMojo {

  @Parameter(defaultValue = "${project}", required = true, readonly = true)
  private MavenProject project;

  @Parameter(defaultValue = "${parcel.classifier}", required = false, readonly = true)
  private String parcelClassifier;

  @Parameter(defaultValue = "${project.build.directory}/parcel", required = true, readonly = true)
  private String parcelBuildDirectory;

  @Parameter(defaultValue = "${basedir}/src/main/resources/parcel", required = true, readonly = true)
  private String parcelResourcesDirectory;

  @Override
  public void execute() throws MojoExecutionException {
    Parcel parcel = new Parcel(project.getGroupId(), project.getArtifactId(), project.getVersion(),
        StringUtils.isEmpty(parcelClassifier) ? Parcel.getOsDescriptor() : parcelClassifier, project.getPackaging());
    if (parcel.isValid()) {
      parcel.prepare(getLog(), parcelResourcesDirectory, parcelBuildDirectory);
    }
  }

}
