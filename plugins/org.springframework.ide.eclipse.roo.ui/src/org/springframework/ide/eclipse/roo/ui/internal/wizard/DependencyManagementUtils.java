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
package org.springframework.ide.eclipse.roo.ui.internal.wizard;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.project.IProjectConfigurationManager;
import org.eclipse.m2e.core.project.ResolverConfiguration;
import org.springframework.ide.eclipse.core.SpringCorePreferences;
import org.springframework.ide.eclipse.maven.MavenCorePlugin;
import org.springframework.ide.eclipse.roo.core.RooCoreActivator;


/**
 * @author Christian Dupuis
 */
public abstract class DependencyManagementUtils {

	private static final String M2ECLIPSE_CLASS = "org.eclipse.m2e.core.MavenPlugin";

	private static final String MAVEN_STS_CLASS = "com.springsource.sts.maven.MavenCorePlugin";

	public static final boolean IS_M2ECLIPSE_PRESENT = isPresent(M2ECLIPSE_CLASS);

	public static final boolean IS_STS_MAVEN_PRESENT = isPresent(MAVEN_STS_CLASS);

	public static boolean isPresent(String className) {
		try {
			Class.forName(className);
			return true;
		}
		catch (ClassNotFoundException e) {
			return false;
		}
	}

	public static String getMavenStsPluginId() {
		return MavenCorePlugin.PLUGIN_ID;
	}

	public static String getMavenStsPreferenceKey() {
		return MavenCorePlugin.AUTOMATICALLY_UPDATE_DEPENDENCIES_KEY;
	}

	public static void installDependencyManagement(final IProject project, DependencyManagement dependencyManagement) {
		if (dependencyManagement == DependencyManagement.MAVEN_STS) {
			SpringCorePreferences.getProjectPreferences(project, DependencyManagementUtils.getMavenStsPluginId())
					.putBoolean(DependencyManagementUtils.getMavenStsPreferenceKey(), true);
		}
		else if (dependencyManagement == DependencyManagement.M2ECLIPSE) {

			Job mavenJob = new Job("Configuring Maven tools") {

				@Override
				protected IStatus run(IProgressMonitor monitor) {
					try {
						ResolverConfiguration configuration = new ResolverConfiguration();
						configuration.setResolveWorkspaceProjects(true);
						configuration.setActiveProfiles("");

						IProjectConfigurationManager configurationManager = MavenPlugin.getProjectConfigurationManager();
						configurationManager.enableMavenNature(project, configuration, new NullProgressMonitor());

						configurationManager.updateProjectConfiguration(project, new NullProgressMonitor());
					}
					catch (CoreException e) {
						RooCoreActivator.log(e);
					}
					return Status.OK_STATUS;
				}

			};
			mavenJob.setSystem(true);
			mavenJob.schedule();

		}
	}
}
