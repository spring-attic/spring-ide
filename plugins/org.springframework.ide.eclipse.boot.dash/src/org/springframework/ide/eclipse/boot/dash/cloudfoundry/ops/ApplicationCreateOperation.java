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
package org.springframework.ide.eclipse.boot.dash.cloudfoundry.ops;

import org.cloudfoundry.client.lib.domain.CloudApplication;
import org.cloudfoundry.client.lib.domain.Staging;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudAppInstances;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudApplicationDeploymentProperties;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryBootDashModel;

public class ApplicationCreateOperation extends CloudApplicationOperation {

	private final CloudApplicationDeploymentProperties deploymentProperties;

	public ApplicationCreateOperation(CloudApplicationDeploymentProperties deploymentProperties,
			CloudFoundryBootDashModel model) {
		super("Deploying application: " + deploymentProperties.getAppName(), model, deploymentProperties.getAppName());
		this.deploymentProperties = deploymentProperties;
	}

	@Override
	protected void doCloudOp(IProgressMonitor monitor) throws Exception, OperationCanceledException {

		SubMonitor subMonitor = SubMonitor.convert(monitor, 100);

		IStatus status = deploymentProperties.validate();
		if (!status.isOK()) {
			throw new CoreException(status);
		}

		String appName = deploymentProperties.getAppName();

		// See if the application already exists
		subMonitor.setTaskName("Verifying that the application exists: " + appName);

		subMonitor.worked(10);

		CloudAppInstances appInstances = getCloudApplicationInstances();
		if (appInstances == null) {
			appInstances = createApplication(subMonitor);
		}

		if (appInstances == null) {
			throw BootDashActivator.asCoreException("Failed to create or resolve a Cloud application for : "
					+ deploymentProperties.getAppName() + ". Please try to redeploy again or check your connection.");
		}

		this.model.addElement(appInstances, deploymentProperties.getProject());

		getAppUpdateListener().applicationCreated(appInstances);
	}

	protected CloudAppInstances createApplication(SubMonitor subMonitor) throws Exception {
		try {
			subMonitor.setTaskName("Creating application: " + deploymentProperties.getAppName());

			getClient().createApplication(deploymentProperties.getAppName(),
					new Staging(null, deploymentProperties.getBuildpackUrl()), deploymentProperties.getMemory(),
					deploymentProperties.getUrls(), deploymentProperties.getServices());

		} catch (Exception e) {
			// If app creation failed, check if the app was created anyway
			// and delete it to allow users to redeploy
			CloudApplication toCleanUp = getCloudApplication();
			if (toCleanUp != null) {
				getClient().deleteApplication(toCleanUp.getName());
			}
			throw e;
		}
		// Fetch the created Cloud Application
		try {
			subMonitor.setTaskName(
					"Verifying that the application was created successfully: " + deploymentProperties.getAppName());
			return getCloudApplicationInstances();
		} catch (Exception e) {
			throw getApplicationException(e);
		}
	}

}