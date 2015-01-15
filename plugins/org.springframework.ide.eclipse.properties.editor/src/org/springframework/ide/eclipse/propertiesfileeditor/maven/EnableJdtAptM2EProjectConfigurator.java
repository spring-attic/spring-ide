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

import static org.springframework.ide.eclipse.propertiesfileeditor.preferences.PreferenceConstants.AUTO_CONFIGURE_APT_M2E_DEFAULT;
import static org.springframework.ide.eclipse.propertiesfileeditor.preferences.PreferenceConstants.AUTO_CONFIGURE_APT_M2E_PREF;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.m2e.core.project.configurator.ProjectConfigurationRequest;
import org.springframework.ide.eclipse.maven.AbstractSpringProjectConfigurator;
import org.springframework.ide.eclipse.propertiesfileeditor.SpringPropertiesEditorPlugin;
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
		if (isPreferenceEnabled(mavenProject) && shouldEnableApt(mavenProject)) {
			AptUtils.enableApt(JavaCore.create(project));
		}
	}

	private boolean isPreferenceEnabled(MavenProject mavenProject) {
		//in future maybe this can be overridden at project level, but for now only a
		// global preference.
		return Platform.getPreferencesService().getBoolean(SpringPropertiesEditorPlugin.PLUGIN_ID, 
				AUTO_CONFIGURE_APT_M2E_PREF, AUTO_CONFIGURE_APT_M2E_DEFAULT, null);
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
