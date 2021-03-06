package com.cloudera.parcel.plugin.mojo;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.cloudera.parcel.library.ParcelUtil;
import com.cloudera.parcel.plugin.Parcel;
import com.cloudera.parcel.plugin.Parcel.ParcelBuilder;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.StringUtils;

@Mojo(name = "install", requiresProject = true, defaultPhase = LifecyclePhase.INSTALL)
public class Install extends AbstractMojo {

  @Parameter(defaultValue = "${project}", required = true, readonly = true)
  private MavenProject project;

  @Parameter(defaultValue = "${parcel.classifier}", required = false, readonly = true)
  private String parcelClassifier;

  @Parameter(defaultValue = "${localRepository}", required = true, readonly = true)
  private ArtifactRepository localRepository;

  @Parameter(defaultValue = "${project.build.directory}", required = false, readonly = true)
  private String buildDirectory;

  @Parameter(defaultValue = "${parcel.buildMetaData}", required = false, readonly = true)
  private boolean buildMetaData = true;

  @Parameter(defaultValue = "${parcel.validateMetaData}", required = false, readonly = true)
  private boolean validateMetaData = true;

  @Parameter(defaultValue = "${parcel.skip}", required = false, readonly = true)
  private boolean skip = false;

  @Parameter(required = false)
  private List<Parcel> parcels;

  @Override
  public void execute() throws MojoExecutionException {
    if (!skip) {
      if (parcels == null) {
        parcels = Collections.singletonList(ParcelBuilder.get().groupId(project.getGroupId()).artifactId(project.getArtifactId())
          .version(project.getVersion()).classifier(StringUtils.isEmpty(parcelClassifier) ? ParcelUtil.getOsDescriptor() : parcelClassifier)
          .baseDirectory(project.getBasedir().getAbsolutePath()).buildDirectory(buildDirectory).type(project.getPackaging())
          .buildMetaData(buildMetaData).validateMetaData(validateMetaData).build());

      }
      for (Parcel parcel : parcels) {
        parcel.setLocalRepositoryDirectory(localRepository.getBasedir());
        parcel.setBaseDirectory(project.getBasedir().getAbsolutePath());
        parcel.install(getLog());
      }
    }
  }

}
