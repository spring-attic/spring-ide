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
package org.springframework.ide.eclipse.roo.ui.internal.maven;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.maven.ide.eclipse.project.IMavenProjectFacade;
import org.maven.ide.eclipse.project.configurator.ProjectConfigurationRequest;
import org.springframework.ide.eclipse.core.SpringCore;
import org.springframework.ide.eclipse.core.SpringCoreUtils;
import org.springframework.ide.eclipse.maven.legacy.AbstractSpringProjectConfigurator;
import org.springframework.ide.eclipse.roo.core.RooCoreActivator;
import org.springframework.ide.eclipse.roo.ui.internal.actions.OpenShellJob;


/**
 * M2Eclipse project configuration extension that configures a project to get
 * the Roo project nature.
 * @author Christian Dupuis
 * @author Leo Dos Santos
 * @since 2.5.0
 */
public class LegacyRooProjectConfigurator extends AbstractSpringProjectConfigurator {

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void doConfigure(MavenProject mavenProject, IProject project, ProjectConfigurationRequest request,
			IProgressMonitor monitor) throws CoreException {
		// first apply Roo project nature
		boolean hasNature = false;
		for (Artifact art : mavenProject.getArtifacts()) {
			if (art.getArtifactId().startsWith("org.springframework.roo")
					&& art.getGroupId().equals("org.springframework.roo")) {
				SpringCoreUtils.addProjectNature(project, SpringCore.NATURE_ID, monitor);
				SpringCoreUtils.addProjectNature(project, RooCoreActivator.NATURE_ID, monitor);
				hasNature = true;
			}
		}
		if (!hasNature) {
			hasNature = (configureNature(project, mavenProject, SpringCore.NATURE_ID, true, monitor) && configureNature(
					project, mavenProject, RooCoreActivator.NATURE_ID, true, monitor));
		}

		if (hasNature) {
			Artifact parent = mavenProject.getParentArtifact();
			if (parent != null) {
				// traverse the parent chain
				IMavenProjectFacade facade = projectManager.getMavenProject(parent.getGroupId(),
						parent.getArtifactId(), parent.getVersion());
				doConfigure(facade.getMavenProject(), facade.getProject(), request, monitor);
			}
			else {
				// open the Roo Shell for the project
				new OpenShellJob(project).schedule();
			}
		}
	}

}
