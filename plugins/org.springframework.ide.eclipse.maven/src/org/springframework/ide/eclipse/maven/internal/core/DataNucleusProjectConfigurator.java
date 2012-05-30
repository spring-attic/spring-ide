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
package org.springframework.ide.eclipse.maven.internal.core;

import org.apache.maven.project.MavenProject;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.m2e.core.project.configurator.ProjectConfigurationRequest;
import org.eclipse.m2e.jdt.IJavaProjectConfigurator;
import org.springframework.ide.eclipse.core.SpringCore;
import org.springframework.ide.eclipse.core.SpringCorePreferences;
import org.springframework.ide.eclipse.core.project.ProjectBuilderDefinition;
import org.springframework.ide.eclipse.core.project.ProjectBuilderDefinitionFactory;
import org.springframework.ide.eclipse.maven.AbstractSpringProjectConfigurator;


/**
 * M2Eclipse project configuration extension that configures a project to get the proper DN enhancement.
 * @author Christian Dupuis
 * @since 2.5.0
 */
public class DataNucleusProjectConfigurator extends AbstractSpringProjectConfigurator implements IJavaProjectConfigurator {

	// App Engine Nature
	private static final String GAE_NATURE_ID = "com.google.appengine.eclipse.core.gaeNature";

	// DataNucleus Builder
	private static final String DN_BUILDER_ID = "com.springsource.sts.maven.core.dataNucleusEnhancerProjectBuilder";

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void doConfigure(final MavenProject mavenProject, IProject project, ProjectConfigurationRequest request,
			final IProgressMonitor monitor) throws CoreException {

		if (configureNature(project, mavenProject, GAE_NATURE_ID, false, monitor)) {

			// Remove DN support if we use GAE
			if (mavenProject.getPlugin("org.datanucleus:maven-datanucleus-plugin") != null) {
				for (ProjectBuilderDefinition builderDefinition : ProjectBuilderDefinitionFactory
						.getProjectBuilderDefinitions()) {
					if (DN_BUILDER_ID.equals(builderDefinition.getId())) {
						builderDefinition.setEnabled(false, project);
						break;
					}
				}
			}
		}
		else {
			// Make sure that we add DN support if DN is being used in the pom.xml
			if (mavenProject.getPlugin("org.datanucleus:maven-datanucleus-plugin") != null) {
				for (ProjectBuilderDefinition builderDefinition : ProjectBuilderDefinitionFactory
						.getProjectBuilderDefinitions()) {
					if (DN_BUILDER_ID.equals(builderDefinition.getId())) {
						SpringCorePreferences.getProjectPreferences(project).putBoolean(SpringCore.PROJECT_PROPERTY_ID,
								true);
						builderDefinition.setEnabled(true, project);
						break;
					}
				}
			}
		}

	}
}
