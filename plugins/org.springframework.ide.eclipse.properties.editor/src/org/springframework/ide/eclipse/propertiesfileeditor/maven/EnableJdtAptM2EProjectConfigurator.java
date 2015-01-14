/*******************************************************************************
 * Copyright (c) 2015 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.propertiesfileeditor.maven;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.m2e.core.project.configurator.ProjectConfigurationRequest;
import org.springframework.ide.eclipse.maven.AbstractSpringProjectConfigurator;
import org.springframework.ide.eclipse.propertiesfileeditor.util.AptUtils;

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
		if (shouldEnableApt(mavenProject)) {
			AptUtils.enableApt(JavaCore.create(project));
		}
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

}
