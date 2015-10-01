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
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudApplicationDeploymentProperties;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryBootDashModel;

/**
 * Updates deployment properties (e.g. memory, URL, instances) of an existing
 * Cloud Application.
 * <p/>
 * This does NOT upload application sources (i.e the archive). This is meant to
 * be a separate operation so that users can scale their application or change
 * URLs mappings without also pushing the application archive which is a slow
 * process.
 */
public class ApplicationPropertiesUpdateOperation extends CloudApplicationOperation {

	private final CloudApplicationDeploymentProperties deploymentProperties;

	public ApplicationPropertiesUpdateOperation(CloudApplicationDeploymentProperties deploymentProperties,
			CloudFoundryBootDashModel model) {
		super("Updating application deployment properties - " + deploymentProperties.getAppName(), model,
				deploymentProperties.getAppName());
		this.deploymentProperties = deploymentProperties;
	}

	@Override
	protected void doCloudOp(IProgressMonitor monitor) throws Exception, OperationCanceledException {
		if (deploymentProperties == null) {
			throw BootDashActivator.asCoreException("No deployment properties for application - " + appName
					+ " found. Unable to update the application");
		}
		IStatus status = deploymentProperties.validate();
		if (!status.isOK()) {
			throw new CoreException(status);
		}

		if (updateExistingApplicationInCloud(deploymentProperties, monitor)) {
			boolean checkTermination = true;
			this.eventHandler.fireEvent(
					eventFactory.updateRunState(requests.getExistingAppInstances(appName), getDashElement(), null),
					checkTermination);
		}
	}

	protected boolean updateExistingApplicationInCloud(CloudApplicationDeploymentProperties properties,
			IProgressMonitor monitor) throws Exception {

		CloudApplication app = requests.getApplication(appName);
		SubMonitor subMonitor = SubMonitor.convert(monitor, 5);
		boolean updated = false;

		if (app != null) {
			String appName = properties.getAppName();
			if (properties.getEnvironmentVariables() != null
					&& !properties.getEnvironmentVariables().equals(app.getEnvAsMap())) {
				subMonitor.setTaskName("Updating " + appName + " environment variables.");

				requests.updateApplicationEnvironment(appName, properties.getEnvironmentVariables());
				updated = true;

				subMonitor.worked(1);
			}

			if (properties.getBuildpackUrl() != null && app.getStaging() != null
					&& !properties.getBuildpackUrl().equals(app.getStaging().getDetectedBuildpack())) {
				subMonitor.setTaskName("Updating " + appName + " buildpack.");

				requests.updateApplicationStaging(appName, new Staging(null, properties.getBuildpackUrl()));
				updated = true;

				subMonitor.worked(1);
			}

			if (properties.getServices() != null && !properties.getServices().equals(app.getServices())) {
				subMonitor.setTaskName("Updating " + appName + " bound services.");

				requests.updateApplicationServices(appName, properties.getServices());
				updated = true;

				subMonitor.worked(1);
			}

			if (properties.getMemory() > 0 && properties.getMemory() != app.getMemory()) {
				subMonitor.setTaskName("Updating " + appName + " memory.");

				requests.updateApplicationMemory(appName, properties.getMemory());
				updated = true;

				subMonitor.worked(1);
			}

			if (properties.getInstances() > 0 && properties.getInstances() != app.getInstances()) {
				subMonitor.setTaskName("Updating " + appName + " instances.");

				requests.updateApplicationInstances(appName, properties.getInstances());
				updated = true;

				subMonitor.worked(1);
			}

			if (properties.getUrls() != null && !properties.getUrls().equals(app.getUris())) {

				subMonitor.setTaskName("Updating " + appName + " mapped URLs.");

				requests.updateApplicationUris(appName, properties.getUrls());
				updated = true;

				subMonitor.worked(1);
			}
		}
		return updated;
	}

}
