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

import org.cloudfoundry.client.lib.CloudFoundryOperations;
import org.cloudfoundry.client.lib.domain.CloudApplication;
import org.cloudfoundry.client.lib.domain.Staging;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.osgi.util.NLS;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudApplicationDeploymentProperties;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryBootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;

public class CreateAndUpdateCloudAppOp extends CloudApplicationOperation {

	private final static String APP_FOUND_TITLE = "Existing Application Found";

	private final static String APP_FOUND_MESSAGE = "An application with the name - {0} - already exists. Continuing with the deployment will replace all of the existing application's resources with those in the selected project. Are you sure that you want to continue?";

	private final CloudApplicationDeploymentProperties deploymentProperties;

	public CreateAndUpdateCloudAppOp(CloudFoundryOperations client, CloudApplicationDeploymentProperties deploymentProperties,
			CloudFoundryBootDashModel model, UserInteractions ui) {
		super("Deploying application: " + deploymentProperties.getAppName(), client, deploymentProperties.getAppName(),
				model, ui);
		this.deploymentProperties = deploymentProperties;
	}

	@Override
	protected CloudApplication doCloudOp(CloudFoundryOperations client, IProgressMonitor monitor)
			throws Exception, OperationCanceledException {

		SubMonitor subMonitor = SubMonitor.convert(monitor, 100);

		IStatus status = deploymentProperties.validate();
		if (!status.isOK()) {
			throw new CoreException(status);
		}

		String appName = deploymentProperties.getAppName();

		CloudApplication app = null;

		// See if the application already exists
		subMonitor.setTaskName("Verifying application exists: " + appName);
		app = getCloudApplication();

		subMonitor.worked(10);

		if (app == null) {
			app = createApplication(subMonitor);
		} else if (!deploymentProperties.shouldAutoReplace()
				&& !ui.confirmOperation(APP_FOUND_TITLE, NLS.bind(APP_FOUND_MESSAGE, appName))) {
			throw new OperationCanceledException();
		}

		if (app == null) {
			throw BootDashActivator.asCoreException("Failed to create or resolve a Cloud application for : "
					+ deploymentProperties.getAppName() + ". Please try to redeploy again or check your connection.");
		}

		updateExistingApplicationInCloud(deploymentProperties, subMonitor);

		// Notify FIRST before adding the element in the model
		// That way the app state tracker is updated first before
		// any model element listeners ask for the app state when the
		// element is added
		getAppUpdateListener().applicationCreated(app);

		this.model.addElement(app, deploymentProperties.getProject());

		return app;
	}

	protected CloudApplication createApplication(SubMonitor subMonitor) throws Exception {
		CloudApplication app = null;
		try {
			subMonitor.setTaskName("Creating application: " + deploymentProperties.getAppName());

			client.createApplication(deploymentProperties.getAppName(),
					new Staging(null, deploymentProperties.getBuildpackUrl()), deploymentProperties.getMemory(),
					deploymentProperties.getUrls(), deploymentProperties.getServices());

			subMonitor.setTaskName("Application created: " + deploymentProperties.getAppName());
		} catch (Exception e) {
			// If app creation failed, check if the app was created anyway
			// and delete it to allow users to redeploy
			app = getCloudApplication();
			if (app != null) {
				client.deleteApplication(app.getName());
				app = null;
			}
			throw e;
		}
		// Fetch the created Cloud Application
		try {
			app = client.getApplication(deploymentProperties.getAppName());
		} catch (Exception e) {
			throw getApplicationException(e);
		}
		return app;
	}

	protected void updateExistingApplicationInCloud(CloudApplicationDeploymentProperties properties, IProgressMonitor monitor)
			throws Exception {

		CloudApplication app = getCloudApplication();
		SubMonitor subMonitor = SubMonitor.convert(monitor, 5);
		if (app != null) {
			String appName = properties.getAppName();
			if (properties.getEnvironmentVariables() != null
					&& !properties.getEnvironmentVariables().equals(app.getEnvAsMap())) {
				subMonitor.setTaskName("Updating " + appName + " environment variables.");
				client.updateApplicationEnv(appName, properties.getEnvironmentVariables());
				subMonitor.worked(1);

			}

			if (properties.getBuildpackUrl() != null && app.getStaging() != null
					&& !properties.getBuildpackUrl().equals(app.getStaging().getDetectedBuildpack())) {
				subMonitor.setTaskName("Updating " + appName + " buildpack.");

				client.updateApplicationStaging(appName, new Staging(null, properties.getBuildpackUrl()));
				subMonitor.worked(1);

			}

			if (properties.getServices() != null && !properties.getServices().equals(app.getServices())) {
				subMonitor.setTaskName("Updating " + appName + " bound services.");

				client.updateApplicationServices(appName, properties.getServices());
				subMonitor.worked(1);

			}

			if (properties.getMemory() > 0 && properties.getMemory() != app.getMemory()) {
				subMonitor.setTaskName("Updating " + appName + " memory.");

				client.updateApplicationMemory(appName, properties.getMemory());
				subMonitor.worked(1);

			}

			if (properties.getInstances() > 1 && properties.getInstances() != app.getInstances()) {
				subMonitor.setTaskName("Updating " + appName + " instances.");

				client.updateApplicationInstances(appName, properties.getInstances());
				subMonitor.worked(1);

			}

			// if (properties.getUrls() != null &&
			// !properties.getUrls().equals(app.getUris())) {
			//
			// subMonitor.setTaskName("Updating " + appName + " mapped URLs.");
			//
			// client.updateApplicationUris(appName, properties.getUrls());
			//
			// subMonitor.worked(1);
			//
			// }
		}

	}

}