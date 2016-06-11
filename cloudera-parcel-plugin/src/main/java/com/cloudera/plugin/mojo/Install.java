package com.cloudera.plugin.mojo;

import java.util.Arrays;
import java.util.List;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.StringUtils;

import com.cloudera.plugin.Parcel;
import com.cloudera.plugin.Parcel.ParcelBuilder;

@Mojo(name = "install", requiresProject = true, defaultPhase = LifecyclePhase.INSTALL)
public class Install extends AbstractMojo {

  @Parameter(defaultValue = "${project}", required = true, readonly = true)
  private MavenProject project;

  @Parameter(defaultValue = "${parcel.classifier}", required = false, readonly = true)
  private String parcelClassifier;

  @Parameter(defaultValue = "${localRepository}", required = true, readonly = true)
  private ArtifactRepository localRepository;

  @Parameter(defaultValue = "${project.build.directory}", required = true, readonly = true)
  private String buildDirectory;

  @Parameter(required = false)
  private List<Parcel> parcels;

  @Override
  public void execute() throws MojoExecutionException {
    if (parcels == null) {
      parcels = Arrays.asList(new Parcel[] { ParcelBuilder.get().groupId(project.getGroupId())
          .artifactId(project.getArtifactId()).version(project.getVersion())
          .classifier(StringUtils.isEmpty(parcelClassifier) ? Parcel.getOsDescriptor() : parcelClassifier)
          .baseDirectory(project.getBasedir().getAbsolutePath()).buildDirectory(buildDirectory)
          .type(project.getPackaging()).build() });

    }
    for (Parcel parcel : parcels) {
      parcel.setLocalRepositoryDirectory(localRepository.getBasedir());
      parcel.setBaseDirectory(project.getBasedir().getAbsolutePath());
      parcel.install(getLog());
    }
  }

}
