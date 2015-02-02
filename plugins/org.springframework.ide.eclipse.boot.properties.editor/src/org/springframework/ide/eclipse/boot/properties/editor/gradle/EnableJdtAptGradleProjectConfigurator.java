/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.properties.editor.gradle;

import static org.springframework.ide.eclipse.boot.properties.editor.preferences.PreferenceConstants.AUTO_CONFIGURE_APT_GRADLE_DEFAULT;
import static org.springframework.ide.eclipse.boot.properties.editor.preferences.PreferenceConstants.AUTO_CONFIGURE_APT_GRADLE_PREF;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.JavaCore;
import org.gradle.tooling.model.ExternalDependency;
import org.gradle.tooling.model.GradleModuleVersion;
import org.gradle.tooling.model.eclipse.EclipseProject;
import org.springframework.ide.eclipse.boot.properties.editor.SpringPropertiesEditorPlugin;
import org.springframework.ide.eclipse.boot.properties.editor.util.AptUtils;
import org.springsource.ide.eclipse.gradle.core.api.IProjectConfigurationRequest;
import org.springsource.ide.eclipse.gradle.core.api.IProjectConfigurator;

/**
 * Configures JDT APT for a Gradle project that has 'spring-boot-configuration-processor' on its classpath.
 *
 * @author Alex Boyko
 * @author Kris De Volder
 */
public class EnableJdtAptGradleProjectConfigurator implements IProjectConfigurator {

	public void configure(IProjectConfigurationRequest request, IProgressMonitor monitor) throws Exception {
		if (
			isJavaProject(request.getProject()) &&
			isPreferenceEnabled(request.getProject()) &&
			shouldEnableApt(request.getGradleModel())
		) {
			AptUtils.enableApt(JavaCore.create(request.getProject()));
		}
	}

	private boolean isJavaProject(IProject project) {
		try {
			return project.isAccessible() && project.hasNature(JavaCore.NATURE_ID);
		} catch (CoreException e) {
			SpringPropertiesEditorPlugin.log(e);
		}
		return false;
	}

	private boolean isPreferenceEnabled(IProject p) {
		return Platform.getPreferencesService().getBoolean(SpringPropertiesEditorPlugin.PLUGIN_ID,
				AUTO_CONFIGURE_APT_GRADLE_PREF, AUTO_CONFIGURE_APT_GRADLE_DEFAULT, null);
	}

	protected boolean shouldEnableApt(EclipseProject eclipseProject) {
		for (ExternalDependency d : eclipseProject.getClasspath()) {
			GradleModuleVersion a = d.getGradleModuleVersion();
			if (
				a!=null &&
				"org.springframework.boot".equals(a.getGroup()) &&
				"spring-boot-configuration-processor".equals(a.getName())
			) {
				return true;
			}
		}
		return false;
	}


}