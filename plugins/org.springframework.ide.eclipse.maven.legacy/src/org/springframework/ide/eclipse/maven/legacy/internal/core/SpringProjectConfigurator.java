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

import org.apache.maven.project.MavenProject;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.maven.ide.eclipse.jdt.IJavaProjectConfigurator;
import org.maven.ide.eclipse.project.configurator.ProjectConfigurationRequest;
import org.springframework.ide.eclipse.core.SpringCore;
import org.springframework.ide.eclipse.maven.legacy.AbstractSpringProjectConfigurator;


/**
 * M2Eclipse project configuration extension that configures a project to get the Spring project
 * nature.
 * @author Christian Dupuis
 * @since 2.5.0
 */
public class SpringProjectConfigurator extends AbstractSpringProjectConfigurator implements IJavaProjectConfigurator {
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void doConfigure(MavenProject mavenProject, IProject project, ProjectConfigurationRequest request,
			IProgressMonitor monitor) throws CoreException {
		configureNature(project, mavenProject, SpringCore.NATURE_ID, true, monitor);
	}
}
