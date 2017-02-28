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
import org.eclipse.cft.server.core.internal.CloudFoundryProjectUtil;
import org.eclipse.cft.server.core.internal.CloudFoundryServer;
import org.eclipse.cft.server.core.internal.CloudServerUtil;
import org.eclipse.cft.server.core.internal.application.ICloudFoundryArchiver;
import org.eclipse.cft.server.core.internal.application.ZipArchive;
import org.eclipse.cft.server.core.internal.client.CloudFoundryApplicationModule;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.model.IModuleResource;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.packaging.CloudApplicationArchiverStrategies;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.packaging.CloudApplicationArchiverStrategy;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.packaging.ICloudApplicationArchiver;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springframework.ide.eclipse.cft.CFTConsole;
import org.springframework.ide.eclipse.cft.CFTIntegrationUserInteractions;
import org.springframework.ide.eclipse.cft.Log;
import org.springframework.ide.eclipse.cft.ProjectUtils;

public class BootDashboardArchiver implements ICloudFoundryArchiver {

	private CFTIntegrationUserInteractions userInteractions;

	@Override
	public CFApplicationArchive getApplicationArchive(IModule module, IServer server, IModuleResource[] resources,
			IProgressMonitor monitor) throws CoreException {

		CloudFoundryApplicationModule appModule = CloudServerUtil.getCloudFoundryApplicationModule(module, server);
		CloudFoundryServer cloudServer = CloudServerUtil.getCloudServer(server);

		if (appModule == null) {
			throw asCoreException(
					"No cloud application module resolved. Unable to archive the application. Refresh the server instance and retry the operation.");
		}

		if (cloudServer == null) {
			throw asCoreException(
					"No Cloud Foundry server resolved. Verify that the Cloud Foundry server instance is connected.");
		}

		IProject project = getProject(appModule);

		if (project == null) {
			throw asCoreException(
					"Unable to archive the application. No project resolved for the cloud application module "
							+ appModule.getDeployedApplicationName()
							+ ". Verify that the application is linked to an accessible workspace project.");
		}

		return generateArchive(appModule, cloudServer, project, monitor);
	}

	protected CFApplicationArchive getFromBootDashboardIntegration(CloudFoundryApplicationModule appModule,
			CloudFoundryServer cloudServer, IProject project, UserInteractions ui, IProgressMonitor monitor)
			throws Exception {

		// Bugzilla (CFT) 495814 fix
		refreshProject(project, appModule, cloudServer, monitor);

		CloudApplicationArchiverStrategy strategy = CloudApplicationArchiverStrategies.packageAsJar(project, ui);
		ICloudApplicationArchiver archiver = strategy.getArchiver(monitor);

		// Null archiver means operation cancelled.
		if (archiver == null) {
			throw new OperationCanceledException();
		}
		printToConsole(appModule, cloudServer,
				"Using Boot Dashboard archiver to package application project: " + project.getName());

		File file = archiver.getApplicationArchive(monitor);
	    if (file != null) {
			printToConsole(appModule, cloudServer,
					"Succeeded in generating archive file using Boot Dashboard archiver:  " + file.getAbsolutePath());
			return new ZipArchive(new ZipFile(file));
		} else {
			String warning = "Unable to archive the application project. No archive file generated for: "
					+ appModule.getDeployedApplicationName() + ". Falling back to default CFT framework JAR archiver.";
			printToConsole(appModule, cloudServer, warning);
			Log.logWarning(warning);
			return null;
		}
	}

	protected CFApplicationArchive generateArchive(CloudFoundryApplicationModule appModule,
			CloudFoundryServer cloudServer, IProject project, IProgressMonitor monitor) throws CoreException {
		if (ProjectUtils.isSpringBootProject(appModule)) {
			try {
				return getFromBootDashboardIntegration(appModule, cloudServer, project, getUserInteractions(), monitor);
			} catch (Exception e) {
				// If error, don't propagate error to allow fallback to other
				// archiving mechanisms, unless it is operation Cancelled
				if (e instanceof OperationCanceledException) {
					IStatus status = Log.createWarningStatus("Operation canceled.");
					throw new CoreException(status);
				}
				Log.log(e);
			}
		}
		return null;
	}

	protected synchronized UserInteractions getUserInteractions() {
		if (userInteractions == null) {
			userInteractions = new CFTIntegrationUserInteractions();
		}
		return userInteractions;
	}

	protected void refreshProject(IProject project, CloudFoundryApplicationModule appModule,
			CloudFoundryServer cloudServer, IProgressMonitor monitor) throws CoreException {

		if (project != null && project.isAccessible()) {
			printToConsole(appModule, cloudServer, "Refreshing project: " + project.getName());
			project.refreshLocal(IResource.DEPTH_INFINITE, monitor);
		}
	}

	protected void printToConsole(CloudFoundryApplicationModule appModule, CloudFoundryServer cloudServer,
			String message) {
		getConsole().printToConsole(appModule, cloudServer, message);
	}

	protected CoreException asCoreException(String message) {
		return Log.asCoreException(message);
	}

	protected CFTConsole getConsole() {
		return CFTConsole.getDefault();
	}

	protected IProject getProject(CloudFoundryApplicationModule appModule) {
		return CloudFoundryProjectUtil.getProject(appModule);
	}

}
