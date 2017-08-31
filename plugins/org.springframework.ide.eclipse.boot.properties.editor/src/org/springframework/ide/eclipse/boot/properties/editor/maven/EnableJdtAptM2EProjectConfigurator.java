/*******************************************************************************
 * Copyright (c) 2015, 2017 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.properties.editor.maven;

import static org.springframework.ide.eclipse.boot.properties.editor.preferences.PreferenceConstants.AUTO_CONFIGURE_APT_M2E_DEFAULT;
import static org.springframework.ide.eclipse.boot.properties.editor.preferences.PreferenceConstants.AUTO_CONFIGURE_APT_M2E_PREF;

import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.project.MavenProject;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.m2e.core.embedder.IMaven;
import org.eclipse.m2e.core.project.configurator.ProjectConfigurationRequest;
import org.springframework.ide.eclipse.boot.properties.editor.SpringPropertiesEditorPlugin;
import org.springframework.ide.eclipse.boot.properties.editor.util.AptUtils;
import org.springframework.ide.eclipse.maven.AbstractSpringProjectConfigurator;

/**
 * M2E project configuration which enables JDT APT processor if the project has
 * a dependency on "spring-boot-configuration-processor".
 *
 * @author Kris De Volder
 */
public class EnableJdtAptM2EProjectConfigurator extends AbstractSpringProjectConfigurator {

	@Override
	protected void doConfigure(MavenProject mavenProject, IProject project,
			ProjectConfigurationRequest request, IProgressMonitor monitor)
			throws CoreException {
		if (isPreferenceEnabled(mavenProject) && shouldConfigureApt(mavenProject, project, request, monitor)) {
			AptUtils.configureApt(JavaCore.create(project));
		}
	}

	private boolean isPreferenceEnabled(MavenProject mavenProject) {
		//in future maybe this can be overridden at project level, but for now only a
		// global preference.
		return Platform.getPreferencesService().getBoolean(SpringPropertiesEditorPlugin.PLUGIN_ID,
				AUTO_CONFIGURE_APT_M2E_PREF, AUTO_CONFIGURE_APT_M2E_DEFAULT, null);
	}

	protected boolean shouldConfigureApt(MavenProject mavenProject, IProject project,
			ProjectConfigurationRequest request, IProgressMonitor monitor) throws CoreException {
		IJavaProject jp = JavaCore.create(project);
		return !hasAnnotationProcessorPaths(maven, mavenProject, request, monitor)
				&& (AptUtils.isAptEnabled(jp) || shouldEnableApt(mavenProject));
	}

	protected boolean shouldEnableApt(MavenProject mp) {
		for (Artifact a : mp.getArtifacts()) {
			if (
				"org.springframework.boot".equals(a.getGroupId()) &&
				"spring-boot-configuration-processor".equals(a.getArtifactId())
			) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Defining an annotation processor using the "annotationProcessorPaths" support
	 * in m2e is a "newer" way and does not require checking the classpath for the processor like this STS project configurator does.
	 *
	 * However, due to the STS configurator being too
	 * general and handling any project that has APT enabled (instead of just Spring
	 * boot projects), it resulted in this bug that affects other projects:
	 *
	 * https://github.com/jbosstools/m2e-apt/issues/57
	 *
	 * Therefore, if a project uses the annotation processor path, do no perform the STS APT management.
	 *
	 * @param maven
	 * @param mavenProject
	 * @param request
	 * @param monitor
	 * @return
	 * @throws CoreException
	 */
	protected boolean hasAnnotationProcessorPaths(IMaven maven, MavenProject mavenProject, ProjectConfigurationRequest request, IProgressMonitor monitor)
			throws CoreException {

		List<MojoExecution> mojoExecutions = getMojoExecutions(request , monitor);
		if (mojoExecutions != null) {
			for (MojoExecution mojoExecution : mojoExecutions) {

				Dependency[] annotationProcessorPaths = maven.getMojoParameterValue(mavenProject, mojoExecution,
						"annotationProcessorPaths", Dependency[].class, monitor);

				return annotationProcessorPaths != null && annotationProcessorPaths.length > 0;
			}
		}

		return false;
	}

}
