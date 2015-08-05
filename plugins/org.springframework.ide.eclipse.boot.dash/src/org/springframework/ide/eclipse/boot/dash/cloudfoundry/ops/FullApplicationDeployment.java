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
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.osgi.util.NLS;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudApplicationDeploymentProperties;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryBootDashModel;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.ManifestParser;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;

public class FullApplicationDeployment extends CloudApplicationOperation {

	private final static String APP_FOUND_TITLE = "Existing Application Found";

	private final static String APP_FOUND_MESSAGE = "An application with the name - {0} - already exists. Continuing with the deployment will replace all of the existing application's resources with those in the selected project. Are you sure that you want to continue?";

	private final IProject project;
	private final UserInteractions ui;
	private final boolean shouldAutoReplace;

	public FullApplicationDeployment(IProject project, CloudFoundryBootDashModel model,
			UserInteractions ui, boolean shouldAutoReplace) {
		super("Deploying project " + project.getName(), model, project.getName());

		this.project = project;
		this.shouldAutoReplace = shouldAutoReplace;
		this.ui = ui;
	}

	@Override
	protected void doCloudOp(IProgressMonitor monitor)
			throws Exception, OperationCanceledException {

		CloudApplicationDeploymentProperties properties = getDeploymentProperties(project, monitor);

		List<CloudApplicationOperation> deploymentOperations = new ArrayList<CloudApplicationOperation>();
		// Check if the application exists
		CloudApplication app = getCloudApplication();

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
					ui.errorPopup("Existing application already exists",
							"An application [" + el.getName() + "] linked to project [" + project
									.getName()
							+ "] already exists. Only one application can be linked to the same project in the same space. Please delete the existing one and try deploying again.");
					throw new OperationCanceledException();
				}
			}
		}

		// Compose all the different operations that form a full deployment and
		// add them in the order that they should be executed

		CloudApplicationOperation createOp = new ApplicationCreateOperation(properties, model);
		CloudApplicationOperation uploadOp = new ApplicationUploadOperation(properties, model);
		CloudApplicationOperation updateOp = new ApplicationDeploymentUpdateOperation(properties, model);
		CloudApplicationOperation restartOp = new ApplicationStartOperation(properties.getAppName(), model);

		deploymentOperations.add(createOp);
		deploymentOperations.add(updateOp);
		deploymentOperations.add(uploadOp);
		deploymentOperations.add(restartOp);

		CloudApplicationOperation op = new ApplicationOperationWithModelUpdate(getName(), model, appName,
				deploymentOperations);
		op.addApplicationUpdateListener(new FullAppDeploymentListener(properties.getAppName(), model));

		model.getOperationsExecution(ui).runOpAsynch(op);
	}

	protected CloudApplicationDeploymentProperties getDeploymentProperties(IProject project, IProgressMonitor monitor)
			throws Exception {

		SubMonitor subMonitor = SubMonitor.convert(monitor);
		subMonitor.setTaskName("Resolving deployment properties for project: " + project.getName());

		CloudApplicationDeploymentProperties deploymentProperties = null;

		ManifestParser parser = new ManifestParser(project, model.getCloudTarget().getDomains(monitor));

		if (parser.hasManifest()) {
			deploymentProperties = parser.load(subMonitor.newChild(100));
		}

		IStatus status = Status.OK_STATUS;

		if (deploymentProperties == null) {
			status = BootDashActivator.createErrorStatus(null, "No deployment propreties found for " + project.getName()
					+ ". Only projects with valid manifest.yml are currently supported. Please add a manifest.yml to your project and try again.");
		} else {
			// Update the app name
			this.appName = deploymentProperties.getAppName();
			status = deploymentProperties.validate();
		}

		if (!status.isOK()) {
			throw new CoreException(status);
		}

		deploymentProperties.setShoudAutoReplace(shouldAutoReplace);
		return deploymentProperties;
	}

}
