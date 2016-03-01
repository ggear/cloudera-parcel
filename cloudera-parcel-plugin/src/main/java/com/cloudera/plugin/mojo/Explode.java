package com.cloudera.plugin.mojo;

import java.util.List;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.cloudera.plugin.Parcel;

@Mojo(name = "explode", requiresProject = false, defaultPhase = LifecyclePhase.VALIDATE)
public class Explode extends AbstractMojo {

  @Parameter(defaultValue = "${localRepository}", required = true, readonly = true)
  private ArtifactRepository localRepository;

  @Parameter(defaultValue = "${project.build.directory}", required = true, readonly = true)
  private String buildDirectory;

  @Parameter(required = true)
  private List<Parcel> parcels;

  @Override
  public void execute() throws MojoExecutionException {
    for (Parcel parcel : parcels) {
      if (parcel.isValid()) {
        parcel.explode(getLog(), localRepository.getBasedir(), buildDirectory);
      }
    }
  }

}
