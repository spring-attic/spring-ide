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
import org.eclipse.osgi.util.NLS;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.ApplicationManifestHandler;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudApplicationDeploymentProperties;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryBootDashModel;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.DevtoolsUtil;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.UserDefinedDeploymentProperties;
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;

public class DeploymentOperationFactory {

	private final static String APP_FOUND_TITLE = "Replace Existing Application";

	private final static String APP_FOUND_MESSAGE = "Replace the existing application - {0} - with project: {1}?";

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
		List<CloudApplicationOperation> uploadAndRestartOps = getUploadUpdateRestartOps(properties);

		CloudApplicationOperation op = new CompositeApplicationOperation(
				"Re-deploying and re-starting project: " + project.getName(), model, properties.getAppName(),
				uploadAndRestartOps, RunState.STARTING);
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

		// Get the existing app again in case deployment properties changed
		// (i.e. it points to another existing app)
		existingApp = requests.getApplication(properties.getAppName());

		if (existingApp != null && !ui.confirmOperation(APP_FOUND_TITLE,
				NLS.bind(APP_FOUND_MESSAGE, properties.getAppName(), properties.getProject().getName()))) {
			throw new OperationCanceledException();
		}

		RunState initialRunstate = RunState.STARTING;

		CloudApplicationOperation addElementOp = new AddElementOperation(properties, model, existingApp,
				initialRunstate);
		deploymentOperations.add(addElementOp);

		List<CloudApplicationOperation> uploadAndRestartOps = getUploadUpdateRestartOps(properties);
		deploymentOperations.addAll(uploadAndRestartOps);

		CloudApplicationOperation op = new CompositeApplicationOperation(
				"Deploying and starting project: " + project.getName(), model, properties.getAppName(),
				deploymentOperations);
		return op;
	}

	/**
	 *
	 * @return non-null list of ops that perform upload, update and restart
	 */
	protected List<CloudApplicationOperation> getUploadUpdateRestartOps(
			CloudApplicationDeploymentProperties properties) {
		List<CloudApplicationOperation> deploymentOperations = new ArrayList<CloudApplicationOperation>();
		// set the preferred runstate in the first op that gets executed. Not
		// necessary to add it to any of the following ops
		// as the app will remain in the preferred run state unless any one of
		// the ops, or any other op running in parallel, changes the state to
		// something else

		// Stop application first to avoid issues when updating or restarting
		// the app in case
		// the app is in a "failed" state in CF. This is a work around to handle
		// 503 errors that
		// may result when the underlying client indirectly fetches app instance
		// stats that may not
		// be available (and thus throw 503)
		deploymentOperations.add(new ApplicationStopOperation(properties.getAppName(), model, false));
		deploymentOperations.add(new ApplicationPropertiesUpdateOperation(properties, model));
		deploymentOperations.add(new ApplicationUploadOperation(properties, model, ui));
		deploymentOperations.add(new ApplicationStartOperation(properties.getAppName(), model));

		return deploymentOperations;
	}

	/**
	 *
	 * @param existingApp
	 *            optional. If it does not exist, pass null.
	 * @param domains
	 *            in the Cloud target
	 * @param runOrDebug
	 * @param monitor
	 * @return non-null deployment properties for the application.
	 * @throws Exception
	 *             if error occurred while resolving the deployment properties
	 * @throws OperationCanceledException
	 *             if user canceled operation while resolving deployment
	 *             properties
	 */
	protected CloudApplicationDeploymentProperties resolveDeploymentProperties(CloudApplication existingApp,
			List<CloudDomain> domains, RunState runOrDebug, IProgressMonitor monitor) throws Exception {

		ApplicationManifestHandler manifestHandler = new ApplicationManifestHandler(project, domains);

		monitor.setTaskName("Resolving deployment properties for project: " + project.getName());

		CloudApplicationDeploymentProperties deploymentProperties = null;

		if (manifestHandler.hasManifest()) {
			List<CloudApplicationDeploymentProperties> appProperties = manifestHandler.load(monitor);
			if (appProperties == null || appProperties.isEmpty()) {
				throw BootDashActivator.asCoreException("No deployment properties found for " + project.getName()
						+ ". Please ensure that your project can be packaged as an executable application or contains a manifest.yml that points to an archive file that exists.");
			} else {
				deploymentProperties = appProperties.get(0);
			}
		} else if (existingApp != null) {
			deploymentProperties = CloudApplicationDeploymentProperties.getFor(existingApp, project);
		} else {
			// Prompt user for properties
			UserDefinedDeploymentProperties userDefinedProps = ui.promptApplicationDeploymentProperties(project,
					domains);
			if (userDefinedProps != null) {
				deploymentProperties = userDefinedProps.asCloudAppDeploymentProperties();
				if (userDefinedProps.writeManifest()) {
					manifestHandler.create(monitor, deploymentProperties);
				}
			} else {
				// if no deployment properties specified, cancel
				throw new OperationCanceledException();
			}

			// Get the app AGAIN in case the app name was changed in the UI
			// (e.g. a user wants to "link" the project to another
			// existing application with a different name)
			existingApp = requests.getApplication(userDefinedProps.getAppName());
			if (existingApp != null) {

				CloudApplicationDeploymentProperties existingProps = CloudApplicationDeploymentProperties
						.getFor(existingApp, project);

				deploymentProperties = userDefinedProps.mergeInto(existingProps);
			}
		}

		monitor.worked(10);

		// For now just support one application deployment from a manifest.yml
		IStatus status = deploymentProperties.validate();
		monitor.worked(10);

		if (!status.isOK()) {
			throw new CoreException(status);
		}

		// Update JAVA_OPTS env variable with Remote DevTools Client secret
		if (project != null) {
			DevtoolsUtil.setupEnvVarsForRemoteClient(deploymentProperties.getEnvironmentVariables(),
					DevtoolsUtil.getSecret(project));
		}

		return deploymentProperties;
	}

	protected String getAppName(IProject project) {
		// check if there is a project -> app mapping:
		CloudApplication app = model.getAppCache().getApp(project);
		return app != null ? app.getName() : project.getName();
	}
}
