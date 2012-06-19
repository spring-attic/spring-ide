/*******************************************************************************
 *  Copyright (c) 2012 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.maven.legacy.internal.core;

import java.util.Arrays;
import java.util.List;

import org.apache.maven.execution.DefaultMavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionResult;
import org.apache.maven.model.Dependency;
import org.apache.maven.project.MavenProject;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.maven.ide.eclipse.MavenPlugin;
import org.maven.ide.eclipse.embedder.IMaven;
import org.maven.ide.eclipse.jdt.IJavaProjectConfigurator;
import org.maven.ide.eclipse.project.configurator.ProjectConfigurationRequest;
import org.osgi.service.prefs.BackingStoreException;
import org.springframework.ide.eclipse.core.SpringCorePreferences;
import org.springframework.ide.eclipse.core.SpringCoreUtils;
import org.springframework.ide.eclipse.maven.legacy.AbstractSpringProjectConfigurator;
import org.springframework.ide.eclipse.maven.legacy.MavenCorePlugin;
import org.springframework.util.StringUtils;


/**
 * M2Eclipse project configuration extension that configures a project to get the Google GWT/GAE project nature.
 * @author Christian Dupuis
 * @since 2.5.0
 */
@Deprecated
public class GoogleProjectConfigurator extends AbstractSpringProjectConfigurator implements IJavaProjectConfigurator {

	private static final String GWT_GROUP_ID = "com.google.gwt";

	// App Engine Nature
	private static final String GAE_NATURE_ID = "com.google.appengine.eclipse.core.gaeNature";

	// GWT Nature
	private static final String GWT_NATURE_ID = "com.google.gwt.eclipse.core.gwtNature";

	private static final List<String> GAE_UNPACK_GOAL = Arrays
			.asList(new String[] { "net.kindleit:maven-gae-plugin:unpack" });

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void doConfigure(final MavenProject mavenProject, IProject project, ProjectConfigurationRequest request,
			final IProgressMonitor monitor) throws CoreException {

		final IMaven maven = MavenPlugin.getDefault().getMaven();

		configureNature(project, mavenProject, GAE_NATURE_ID, true, new NatureCallbackAdapter() {

			@Override
			public void beforeAddingNature() {
				try {
					DefaultMavenExecutionRequest executionRequest = new DefaultMavenExecutionRequest();
					executionRequest.setBaseDirectory(mavenProject.getBasedir());
					executionRequest.setLocalRepository(maven.getLocalRepository());
					executionRequest.setRemoteRepositories(mavenProject.getRemoteArtifactRepositories());
					executionRequest.setPluginArtifactRepositories(mavenProject.getPluginArtifactRepositories());
					executionRequest.setPom(mavenProject.getFile());
					executionRequest.setGoals(GAE_UNPACK_GOAL);

					MavenExecutionResult result = maven.execute(executionRequest, monitor);
					if (result.hasExceptions()) {
						MavenCorePlugin.getDefault().getLog().log(
								new Status(IStatus.ERROR, MavenCorePlugin.NON_LEGACY_PLUGIN_ID, "Error configuring project",
										result.getExceptions().get(0)));
					}
				}
				catch (CoreException e) {
					MavenCorePlugin.getDefault().getLog().log(
							new Status(IStatus.ERROR, MavenCorePlugin.NON_LEGACY_PLUGIN_ID, "Error configuring project", e));
				}
			}
		}, monitor);

		if (configureNature(project, mavenProject, GWT_NATURE_ID, true, new NatureCallbackAdapter() {

			@Override
			public void beforeAddingNature() {

				// Get the GWT version from the project pom
				String gwtVersion = null;
				List<Dependency> dependencies = mavenProject.getDependencies();
				for (Dependency dependency : dependencies) {
					if (GWT_GROUP_ID.equals(dependency.getGroupId())
							&& ("gwt-user".equals(dependency.getArtifactId()) || "gwt-servlet".equals(dependency
									.getArtifactId()))) {
						gwtVersion = dependency.getVersion();
						break;
					}
				}

				// Check that the pom.xml has GWT dependencies
				if (StringUtils.hasLength(gwtVersion)) {
					try {
						// Download and install the gwt-dev.jar into the local repository
						maven.resolve(GWT_GROUP_ID, "gwt-dev", gwtVersion, "jar", null,
								mavenProject.getRemoteArtifactRepositories(), monitor);
					}
					catch (CoreException e) {
						MavenCorePlugin.getDefault().getLog().log(
								new Status(IStatus.ERROR, MavenCorePlugin.NON_LEGACY_PLUGIN_ID, "Error configuring project", e));
					}
				}
			}
		}, monitor)) {

			try {
				// Add GWT Web Application configuration parameters
				IEclipsePreferences prefs = SpringCorePreferences.getProjectPreferences(project,
						"com.google.gdt.eclipse.core").getProjectPreferences();
				prefs.put("warSrcDir", "src/main/webapp");
				prefs.putBoolean("warSrcDirIsOutput", false);

				String artifactId = mavenProject.getArtifactId();
				String version = mavenProject.getVersion();
				IPath location = SpringCoreUtils.getProjectLocation(project);
				if (location != null && artifactId != null && version != null) {
					prefs.put("lastWarOutDir", location.append("target").append(artifactId + "-" + version).toFile()
							.getAbsolutePath());
				}

				prefs.flush();
			}
			catch (BackingStoreException e) {
				MavenCorePlugin.getDefault().getLog()
						.log(new Status(IStatus.ERROR, MavenCorePlugin.NON_LEGACY_PLUGIN_ID, "Error configuring project", e));
			}
		}
	}
}
