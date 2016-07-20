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

import org.eclipse.cft.server.core.CFApplicationArchive;
import org.eclipse.cft.server.core.internal.CloudFoundryProjectUtil;
import org.eclipse.cft.server.core.internal.CloudFoundryServer;
import org.eclipse.cft.server.core.internal.client.CloudFoundryApplicationModule;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.wst.server.core.model.IModuleResource;
import org.springframework.ide.eclipse.cft.CFTConsole;
import org.springframework.ide.eclipse.cft.Log;

public abstract class AbstractApplicationArchiver implements IntegrationApplicationArchiver {

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

	@Override
	public CFApplicationArchive getApplicationArchive(CloudFoundryApplicationModule appModule,
			CloudFoundryServer cloudServer, IModuleResource[] moduleResources, IProgressMonitor monitor)
			throws CoreException {
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

	protected abstract CFApplicationArchive generateArchive(CloudFoundryApplicationModule appModule,
			CloudFoundryServer cloudServer, IProject project, IProgressMonitor monitor) throws CoreException;

}
