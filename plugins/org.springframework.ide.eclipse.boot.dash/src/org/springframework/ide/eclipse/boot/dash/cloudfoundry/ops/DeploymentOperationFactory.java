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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.osgi.util.NLS;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudApplicationDeploymentProperties;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryBootDashModel;
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


		// First see if an app exists with the given project name
		CloudApplication existingApp = requests.getApplication(getAppName(project));
		CloudApplicationDeploymentProperties properties = model.getDeploymentProperties(existingApp, project, runOrDebug, ui, requests,
				monitor);

		// Get the existing app again in case deployment properties changed and a different app name is now present in the properties
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

	protected String getAppName(IProject project) {
		// check if there is a project -> app mapping:
		CloudApplication app = model.getAppCache().getApp(project);
		return app != null ? app.getName() : project.getName();
	}
}
