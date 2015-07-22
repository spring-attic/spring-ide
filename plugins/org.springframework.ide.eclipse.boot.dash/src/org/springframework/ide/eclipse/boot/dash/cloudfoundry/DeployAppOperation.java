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
package org.springframework.ide.eclipse.boot.dash.cloudfoundry;

import org.cloudfoundry.client.lib.CloudFoundryOperations;
import org.cloudfoundry.client.lib.domain.CloudApplication;
import org.cloudfoundry.client.lib.domain.Staging;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.osgi.util.NLS;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;

public class DeployAppOperation extends CloudOperation<CloudApplication> {

	private final CloudFoundryBootDashModel model;

	private final CloudDeploymentProperties deploymentProperties;

	private final static String APP_FOUND_TITLE = "Existing Application Found";

	private final static String APP_FOUND_MESSAGE = "An application with the name - {0} - already exists. Continuing with the deployment will replace all of the existing application's resources with those in the selected project. Are you sure that you want to continue?";

	public DeployAppOperation(CloudFoundryOperations client, CloudDeploymentProperties deploymentProperties,
			CloudFoundryBootDashModel model, UserInteractions ui) {
		super("Deploying project: " + deploymentProperties.getProject().getName(), client, ui);
		this.model = model;
		this.deploymentProperties = deploymentProperties;
	}

	@Override
	protected CloudApplication doCloudOp(CloudFoundryOperations client, IProgressMonitor monitor) throws Exception {

		SubMonitor subMonitor = SubMonitor.convert(monitor, 100);

		IStatus status = deploymentProperties.validate();
		if (!status.isOK()) {
			throw new CoreException(status);
		}

		String appName = deploymentProperties.getAppName();

		CloudApplication app = null;

		// See if the application already exists
		try {
			subMonitor.setTaskName("Verifying application exists: " + appName);

			app = client.getApplication(appName);
			subMonitor.worked(10);

		} catch (Throwable t) {
			// Ignore. Apps that dont exist throw exception. If any
			// other error occurs (network I/O)
			// it will be thrown in further operations below
		}

		if (app == null) {
			subMonitor.setTaskName("Creating application: " + appName);

			client.createApplication(deploymentProperties.getAppName(),
					new Staging(null, deploymentProperties.getBuildpackUrl()), deploymentProperties.getMemory(),
					deploymentProperties.getUrls(), deploymentProperties.getServices());

			subMonitor.setTaskName("Application created: " + appName);

		} else if (!ui.confirmOperation(APP_FOUND_TITLE, NLS.bind(APP_FOUND_MESSAGE, appName))) {
			return app;
		}

		// get the created app to verify it exists as well as fetch
		// the updated version
		app = client.getApplication(appName);

		// Upload the application
		CloudZipApplicationArchive archive = null;

		try {

			if (deploymentProperties.getProject() != null) {

				ManifestParser parser = new ManifestParser(deploymentProperties.getProject());

				CloudApplicationArchiver archiver = new CloudApplicationArchiver(
						JavaCore.create(deploymentProperties.getProject()), app.getName(), parser);

				subMonitor.setTaskName("Generating archive for application: " + appName);

				archive = archiver.getApplicationArchive(subMonitor.newChild(50));
			}

			if (archive != null) {

				subMonitor.setTaskName("Uploading archive to Cloud Foundry for application: " + appName);

				client.uploadApplication(appName, archive);

				subMonitor.setTaskName("Archive uploaded to Cloud Foundry for application: " + appName);

			} else {
				throw BootDashActivator.asCoreException("Failed to generate application archive for " + appName
						+ ". Make sure it is a Java project and it is accessible.");

			}
		} finally {
			// IMPORTANT: MUST close the archive to avoid resource
			// leakage and
			// potential bug were the same archive file keeps being
			// pushes even if the
			// archive file changes
			if (archive != null) {
				archive.close();
			}
		}

		// Add the element to the model LAST to ensure the uploading occurred
		this.model.addElement(app, deploymentProperties.getProject());

		return app;
	}

}