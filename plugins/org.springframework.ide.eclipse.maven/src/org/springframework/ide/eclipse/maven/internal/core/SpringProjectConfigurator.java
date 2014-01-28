/*******************************************************************************
 * Copyright (c) 2012, 2014 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.maven.internal.core;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.m2e.core.project.configurator.ProjectConfigurationRequest;
import org.eclipse.m2e.jdt.IJavaProjectConfigurator;
import org.springframework.ide.eclipse.core.SpringCore;
import org.springframework.ide.eclipse.maven.AbstractSpringProjectConfigurator;
import org.springsource.ide.eclipse.commons.core.SpringCoreUtils;


/**
 * M2Eclipse project configuration extension that configures a project to get the Spring project
 * nature.
 * @author Christian Dupuis
 * @author Andrew Eisenberg
 * @since 2.5.0
 */
public class SpringProjectConfigurator extends AbstractSpringProjectConfigurator implements IJavaProjectConfigurator {
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void doConfigure(MavenProject mavenProject, IProject project, ProjectConfigurationRequest request,
			IProgressMonitor monitor) throws CoreException {
	    // first look for a dependency on spring-core
	    boolean found = false;
        for (Artifact art : mavenProject.getArtifacts()) {
            if (art.getArtifactId().equals("spring-core")
                    && art.getGroupId().equals("org.springframework")) {
                SpringCoreUtils.addProjectNature(project, SpringCore.NATURE_ID,
                        monitor);
                found = true;
            }
        }
        
        // if none, then look for the eclipse maven plugin 
		if (!found) {
		    configureNature(project, mavenProject, SpringCore.NATURE_ID, true, monitor);
		}
	}
}
