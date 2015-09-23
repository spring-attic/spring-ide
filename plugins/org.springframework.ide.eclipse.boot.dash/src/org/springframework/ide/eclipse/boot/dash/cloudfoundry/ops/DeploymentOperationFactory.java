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
import org.cloudfoundry.client.lib.domain.CloudDomain;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.ApplicationManifestHandler;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudApplicationDeploymentProperties;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryBootDashModel;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.DevtoolsUtil;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;

public class DeploymentOperationFactory {

	private final static String APP_FOUND_TITLE = "Replace Existing Application";

	private final static String APP_FOUND_MESSAGE = "Replace Existing Application: {0} - already exists. Continue replacing the existing application?";

	private final CloudFoundryBootDashModel model;
	private final IProject project;
	private final UserInteractions ui;
	private final ClientRequests requests;

	public DeploymentOperationFactory(CloudFoundryBootDashModel model, IProject project, UserInteractions ui) {
		this.model = model;
		this.project = project;
		this.ui = ui;
		this.requests = new ClientRequests(model);
	}

	public CloudApplicationOperation getRestartAndDeploy(CloudApplicationDeploymentProperties properties) {
		List<CloudApplicationOperation> uploadAndRestartOps = getUploadUpdateRestartOps(properties, RunState.STARTING);

		CloudApplicationOperation op = new CompositeApplicationOperation(
				"Re-deploying and re-starting project: " + project.getName(), model, properties.getAppName(),
				uploadAndRestartOps);
		return op;
	}

	public CloudApplicationOperation getCreateAndDeploy(RunState runOrDebug, IProgressMonitor monitor)
			throws Exception {
		List<CloudApplicationOperation> deploymentOperations = new ArrayList<CloudApplicationOperation>();

		List<CloudDomain> domains = model.getCloudTarget().getDomains(requests, monitor);

		// First see if an app exists with the given project name
		CloudApplication existingApp = requests.getApplication(getAppName(project));
		CloudApplicationDeploymentProperties properties = resolveDeploymentProperties(existingApp, domains, runOrDebug,
				monitor);

		// Check if another application with the same project mapping already
		// exists.
		// Only ONE application with the same project mapping can exist in the
		// same space
		List<BootDashElement> existingElements = this.model.getElements().getValues();
		if (existingElements != null) {
			for (BootDashElement el : existingElements) {
				if (!properties.getAppName().equals(el.getName()) && project.equals(el.getProject())) {
					ui.errorPopup("Project Already Linked",
							"Unable to create application [" + properties.getAppName() + "]. Another application ["
									+ el.getName() + "] is already linked to the same project - "
									+ project
											.getName()
							+ " - in the same Cloud target. Please delete the existing one and try deploying again.");
					throw new OperationCanceledException();
				}
			}
		}

		// Get the app again in case the app name was changed while resolving
		// deployment properties
		existingApp = requests.getApplication(properties.getAppName());

		if (existingApp != null
				&& !ui.confirmOperation(APP_FOUND_TITLE, NLS.bind(APP_FOUND_MESSAGE, properties.getAppName()))) {
			throw new OperationCanceledException();
		}

		if (existingApp == null) {
			CloudApplicationOperation createOp = new ApplicationCreateOperation(properties, model, RunState.STARTING);
			deploymentOperations.add(createOp);
		}

		List<CloudApplicationOperation> uploadAndRestartOps = getUploadUpdateRestartOps(properties, null);
		deploymentOperations.addAll(uploadAndRestartOps);

		CloudApplicationOperation op = new CompositeApplicationOperation(
				"Deploying and starting project: " + project.getName(), model, properties.getAppName(),
				deploymentOperations);
		return op;
	}

	/**
	 *
	 * @param preferredRunState
	 *            Optional: state at which the application should be when the
	 *            upload and restart operation is run. If null, the existing
	 *            runstate of the app will be retained.
	 * @return non-null list of ops that perform upload, update and restart
	 */
	protected List<CloudApplicationOperation> getUploadUpdateRestartOps(CloudApplicationDeploymentProperties properties,
			RunState preferredRunState) {
		List<CloudApplicationOperation> deploymentOperations = new ArrayList<CloudApplicationOperation>();
		// set the preferred runstate in the first op that gets executed. Not
		// necessary to add it to any of the following ops
		// as the app will remain in the preferred run state unless any one of
		// the ops, or any other op running in parallel, changes the state to
		// something else
		deploymentOperations.add(new ApplicationUploadOperation(properties, model, ui, preferredRunState));

		deploymentOperations.add(new ApplicationPropertiesUpdateOperation(properties, model));

		deploymentOperations.add(new ApplicationStartOperation(properties.getAppName(), model));

		return deploymentOperations;
	}

	protected CloudApplicationDeploymentProperties resolveDeploymentProperties(CloudApplication existingApp,
			List<CloudDomain> domains, RunState runOrDebug, IProgressMonitor monitor) throws Exception {

		ApplicationManifestHandler manifestHandler = new ApplicationManifestHandler(project, domains);

		monitor.setTaskName("Resolving deployment properties for project: " + project.getName());

		List<CloudApplicationDeploymentProperties> appProperties = new ArrayList<CloudApplicationDeploymentProperties>();

		if (manifestHandler.hasManifest()) {
			appProperties = manifestHandler.load(monitor);
		} else if (existingApp != null) {
			appProperties.add(CloudApplicationDeploymentProperties.getFor(existingApp, project));
		} else {
			// Prompt user for properties
			CloudApplicationDeploymentProperties prop = ui.promptApplicationDeploymentProperties(project, domains);
			if (prop != null) {
				if (prop.writeManifest()) {
					manifestHandler.create(monitor, prop);
				}
				appProperties.add(prop);
			}
		}
		monitor.worked(10);
		IStatus status = Status.OK_STATUS;

		// For now just support one application deployment from a manifest.yml
		CloudApplicationDeploymentProperties deploymentProperties = null;
		if (appProperties == null || appProperties.isEmpty()) {
			status = BootDashActivator.createErrorStatus(null, "No deployment properties found for " + project.getName()
					+ ". Please ensure that your project can be packaged as an executable application or contains a manifest.yml that points to an archive file that exists.");
		} else {
			// Update the app name
			deploymentProperties = appProperties.get(0);

			status = deploymentProperties.validate();
			monitor.worked(10);
		}

		if (!status.isOK()) {
			throw new CoreException(status);
		}

		// Update JAVA_OPTS env variable with Remote DevTools Client secret
		if (project != null) {
			DevtoolsUtil.setupEnvVarsForRemoteClient(deploymentProperties.getEnvironmentVariables(),
					DevtoolsUtil.getSecret(project), runOrDebug);
		}

		return deploymentProperties;
	}

	protected String getAppName(IProject project) {
		// check if there is a project -> app mapping:
		CloudApplication app = model.getAppCache().getApp(project);
		return app != null ? app.getName() : project.getName();
	}
}
