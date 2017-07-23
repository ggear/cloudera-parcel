package com.cloudera.plugin.mojo;

import java.util.ArrayList;
import java.util.List;

import com.cloudera.plugin.Parcel;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

@Mojo(name = "download", requiresProject = false, defaultPhase = LifecyclePhase.VALIDATE)
public class Download extends AbstractMojo {

  @Parameter(defaultValue = "${project}", required = true, readonly = true)
  private MavenProject project;

  @Parameter(defaultValue = "${localRepository}", required = true, readonly = true)
  private ArtifactRepository localRepository;

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
        parcels = new ArrayList<>();
      }
      for (Parcel parcel : parcels) {
        parcel.setLocalRepositoryDirectory(localRepository.getBasedir());
        parcel.setBaseDirectory(project.getBasedir().getAbsolutePath());
        parcel.download(getLog());
      }
    }
  }

}
