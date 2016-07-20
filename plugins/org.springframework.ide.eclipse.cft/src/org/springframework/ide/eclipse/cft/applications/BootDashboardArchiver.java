/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.cft.applications;

import java.io.File;
import java.util.zip.ZipFile;

import org.eclipse.cft.server.core.CFApplicationArchive;
import org.eclipse.cft.server.core.internal.CloudFoundryServer;
import org.eclipse.cft.server.core.internal.application.JavaWebApplicationDelegate;
import org.eclipse.cft.server.core.internal.application.ZipArchive;
import org.eclipse.cft.server.core.internal.client.CloudFoundryApplicationModule;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.wst.server.core.IModule;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.packaging.CloudApplicationArchiverStrategies;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.packaging.CloudApplicationArchiverStrategy;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.packaging.ICloudApplicationArchiver;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springframework.ide.eclipse.cft.CFTIntegrationUserInteractions;
import org.springframework.ide.eclipse.cft.Log;
import org.springframework.ide.eclipse.cft.ProjectUtils;

public class BootDashboardArchiver extends AbstractApplicationArchiver {

	private CFTIntegrationUserInteractions userInteractions;

	protected synchronized UserInteractions getUserInteractions() {
		if (userInteractions == null) {
			userInteractions = new CFTIntegrationUserInteractions();
		}
		return userInteractions;
	}

	protected CFApplicationArchive getFromBootDashboardIntegration(CloudFoundryApplicationModule appModule,
			CloudFoundryServer cloudServer, IProject project, UserInteractions ui, IProgressMonitor monitor)
			throws Exception {

		// Bugzilla (CFT) 495814 fix
		refreshProject(project, appModule, cloudServer, monitor);
		
		CFApplicationArchive archive = JavaWebApplicationDelegate.getArchiveFromManifest(appModule, cloudServer);

		if (archive != null) {
			printToConsole(appModule, cloudServer,
					"Found application archive file in manifest file: " + archive.getName());
			return archive;
		} else {
			CloudApplicationArchiverStrategy strategy = CloudApplicationArchiverStrategies.packageAsJar(project, ui);
			ICloudApplicationArchiver archiver = strategy.getArchiver(monitor);

			if (archiver == null) {
				throw asCoreException(
						"Unable to archive the application project. No boot dashboard archiver resolved for: "
								+ appModule.getDeployedApplicationName()
								+ ". Check that the application is linked to an accessible workspace project and the project is a Java project.");
			}
			printToConsole(appModule, cloudServer,
					"Using Boot Dashboard archiver to package application project: " + project.getName());

			File file = archiver.getApplicationArchive(monitor);
			if (file != null) {
				printToConsole(appModule, cloudServer,
						"Succeeded in generating archive file using Boot Dashboard archiver:  "
								+ file.getAbsolutePath());
				return new ZipArchive(new ZipFile(file));
			} else {
				throw asCoreException("Unable to archive the application project. No archive file generated for: "
						+ appModule.getDeployedApplicationName()
						+ ". Check that the application is linked to an accessible workspace project and the project is a Java project.");
			}
		}
	}

	@Override
	protected CFApplicationArchive generateArchive(CloudFoundryApplicationModule appModule,
			CloudFoundryServer cloudServer, IProject project, IProgressMonitor monitor) throws CoreException {
		if (ProjectUtils.isSpringBootProject(appModule)) {
			try {
				return getFromBootDashboardIntegration(appModule, cloudServer, project, getUserInteractions(), monitor);
			} catch (Exception e) {
				// If error, don't propagate error to allow fallback to other
				// archiving mechanisms
				Log.log(e);
			}
		}
		return null;
	}

	@Override
	public boolean supports(IModule module) {
		return ProjectUtils.isSpringBootProject(module);
	}

	@Override
	public boolean shouldSetDefaultUrl(IModule module) {
		return ProjectUtils.isSpringBootProject(module);
	}

}
