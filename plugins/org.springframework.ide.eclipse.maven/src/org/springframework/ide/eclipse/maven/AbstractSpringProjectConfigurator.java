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
package org.springframework.ide.eclipse.maven;

import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.configurator.AbstractProjectConfigurator;
import org.eclipse.m2e.core.project.configurator.ProjectConfigurationRequest;
import org.eclipse.m2e.jdt.IClasspathDescriptor;
import org.eclipse.m2e.jdt.IJavaProjectConfigurator;
import org.springframework.ide.eclipse.core.SpringCoreUtils;

/**
 * @author Christian Dupuis
 * @since 2.5.0
 */
public abstract class AbstractSpringProjectConfigurator extends AbstractProjectConfigurator implements
		IJavaProjectConfigurator {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void configure(ProjectConfigurationRequest request, IProgressMonitor monitor) throws CoreException {
		// Sometimes M2Eclipse calls this method with request == null. Why?
		if (request != null) {
			MavenProject mavenProject = request.getMavenProject();
			IProject project = request.getProject();

			doConfigure(mavenProject, project, request, monitor);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void configureClasspath(IMavenProjectFacade arg0, IClasspathDescriptor arg1, IProgressMonitor arg2)
			throws CoreException {
		// intentionally left empty
	}

	/**
	 * {@inheritDoc}
	 */
	public void configureRawClasspath(ProjectConfigurationRequest arg0, IClasspathDescriptor arg1, IProgressMonitor arg2)
			throws CoreException {
		// intentionally left empty
	}

	private Plugin getEclipsePlugin(MavenProject mavenProject) {
		return mavenProject.getPlugin("org.apache.maven.plugins:maven-eclipse-plugin");
	}

	protected boolean configureNature(IProject project, MavenProject mavenProject, String natureId, boolean addNature,
			final INatureCallback callback, IProgressMonitor monitor) throws CoreException {
		if (hasProjectNature(mavenProject, project, natureId)) {
			if (!SpringCoreUtils.hasNature(project, natureId) && addNature) {
				if (callback != null) {
					callback.beforeAddingNature();
				}

				// Apply nature
				SpringCoreUtils.addProjectNature(project, natureId, monitor);

				if (callback != null) {
					callback.afterAddingNature();
				}
			}
			return true;
		}
		SpringCoreUtils.removeProjectNature(project, natureId, monitor);
		return false;
	}

	protected boolean configureNature(IProject project, MavenProject mavenProject, String natureId, boolean addNature,
			IProgressMonitor monitor) throws CoreException {
		return configureNature(project, mavenProject, natureId, addNature, null, monitor);
	}

	protected abstract void doConfigure(MavenProject mavenProject, IProject project,
			ProjectConfigurationRequest request, IProgressMonitor monitor) throws CoreException;

	/**
	 * Searches the Maven pom.xml for the given project nature.
	 */
	protected boolean hasProjectNature(MavenProject mavenProject, IProject project, String natureId) {
		Plugin plugin = getEclipsePlugin(mavenProject);
		if (plugin != null) {
			Xpp3Dom configuration = (Xpp3Dom) plugin.getConfiguration();
			if (configuration != null) {
				Xpp3Dom additionalBuildCommands = configuration.getChild("additionalProjectnatures");
				if (additionalBuildCommands != null) {
					for (Xpp3Dom projectNature : additionalBuildCommands.getChildren("projectnature")) {
						if (projectNature != null && natureId.equals(projectNature.getValue())) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	/**
	 * Optional callback interface for callers to get notifications before or after adding natures to a project.
	 * <p>
	 * Useful in cases where callers want to execute code before applying the nature.
	 */
	public interface INatureCallback {

		void beforeAddingNature();

		void afterAddingNature();

	}

	/**
	 * Simple no-op adapter for {@link INatureCallback}.
	 */
	public class NatureCallbackAdapter implements INatureCallback {

		public void beforeAddingNature() {
		}

		public void afterAddingNature() {
		}
	}

}
