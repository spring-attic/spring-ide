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

import java.util.ArrayList;
import java.util.List;

import org.cloudfoundry.client.lib.domain.CloudApplication;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudApplicationDeploymentProperties;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryBootDashModel;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.ManifestParser;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;

public class FullApplicationDeployment extends CloudApplicationOperation {

	private final static String APP_FOUND_TITLE = "Existing Application Found";

	private final static String APP_FOUND_MESSAGE = "An application with the name - {0} - already exists. Continuing with the deployment will replace all of the existing application's resources with those in the selected project. Are you sure that you want to continue?";

	private final IProject project;
	private final UserInteractions ui;
	private final boolean shouldAutoReplace;
	private final RunState runOrDebug;

	public FullApplicationDeployment(IProject project, CloudFoundryBootDashModel model, UserInteractions ui,
			boolean shouldAutoReplace, RunState runOrDebug) {
		super("Deploying project " + project.getName(), model, project.getName());

		this.project = project;
		this.shouldAutoReplace = shouldAutoReplace;
		this.ui = ui;
		this.runOrDebug = runOrDebug;
	}

	@Override
	protected void doCloudOp(IProgressMonitor monitor) throws Exception, OperationCanceledException {

		CloudApplicationDeploymentProperties properties = getDeploymentProperties(project, monitor);

		List<CloudApplicationOperation> deploymentOperations = new ArrayList<CloudApplicationOperation>();

		monitor.beginTask("Checking deployment properties and existing application", 10);
		// Check if the application exists

		CloudApplication app = getCloudApplication();
		monitor.worked(5);

		if (app != null && !properties.shouldAutoReplace()
				&& !ui.confirmOperation(APP_FOUND_TITLE, NLS.bind(APP_FOUND_MESSAGE, appName))) {
			throw new OperationCanceledException();
		}

		// Check if another application with the same project mapping already
		// exists.
		// Only ONE application with the same project mapping can exist in the
		// same space
		List<BootDashElement> existingElements = this.model.getElements().getValues();
		if (existingElements != null) {
			for (BootDashElement el : existingElements) {
				if (!properties.getAppName().equals(el.getName()) && project.equals(el.getProject())) {
					ui.errorPopup("Application already exists",
							"An application [" + el.getName() + "] linked to project [" + project
									.getName()
							+ "] already exists. Only one application can be linked to the same project in the same space. Please delete the existing one and try deploying again.");
					throw new OperationCanceledException();
				}
			}
		}
		monitor.worked(5);

		// Compose all the different operations that form a full deployment and
		// add them in the order that they should be executed

		CloudApplicationOperation createOp = new ApplicationCreateOperation(properties, model);
		CloudApplicationOperation uploadOp = new ApplicationUploadOperation(properties, model);
		CloudApplicationOperation updateOp = new ApplicationDeploymentUpdateOperation(properties, model);
		CloudApplicationOperation restartOp = new ApplicationStartOperation(properties.getAppName(), model, runOrDebug);

		deploymentOperations.add(createOp);
		deploymentOperations.add(updateOp);
		deploymentOperations.add(uploadOp);
		deploymentOperations.add(restartOp);

		CloudApplicationOperation op = new ApplicationOperationWithModelUpdate(getName(), model, appName,
				deploymentOperations, true);
		op.addApplicationUpdateListener(new FullAppDeploymentListener(properties.getAppName(), model));

		model.getOperationsExecution(ui).runOpAsynch(op);
	}

	protected CloudApplicationDeploymentProperties getDeploymentProperties(IProject project, IProgressMonitor monitor)
			throws Exception {

		monitor.setTaskName("Resolving deployment properties for project: " + project.getName());

		ManifestParser parser = new ManifestParser(project, model.getCloudTarget().getDomains(monitor));

		List<CloudApplicationDeploymentProperties> appProperties = null;

		if (parser.hasManifest()) {
			appProperties = parser.load(monitor);
		}
		monitor.worked(10);
		IStatus status = Status.OK_STATUS;

		// For now just support one application deployment from a manifest.yml
		CloudApplicationDeploymentProperties deploymentProperties = null;
		if (appProperties == null || appProperties.isEmpty()) {
			status = BootDashActivator.createErrorStatus(null, "No deployment properties found for " + project.getName()
					+ ". Only projects with valid manifest.yml are currently supported. Please add a manifest.yml to your project and try again.");
		} else {
			// Update the app name
			deploymentProperties = appProperties.get(0);
			this.appName = deploymentProperties.getAppName();
			deploymentProperties.setShoudAutoReplace(shouldAutoReplace);

			status = deploymentProperties.validate();
			monitor.worked(10);
		}

		if (!status.isOK()) {
			throw new CoreException(status);
		}

		return deploymentProperties;
	}

}
