/*******************************************************************************
 * Copyright (c) 2015, 2016 Pivotal, Inc.
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
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryBootDashModel;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.debug.DebugSupport;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.deployment.CloudApplicationDeploymentProperties;
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;

public class ApplicationDeploymentOperations {

	private final static String APP_FOUND_TITLE = "Replace Existing Application";

	private final static String APP_FOUND_MESSAGE = "Replace the existing application - {0} - with project: {1}?";

	private final CloudFoundryBootDashModel model;


	public ApplicationDeploymentOperations(CloudFoundryBootDashModel model) {
		this.model = model;
	}

	public CloudApplicationOperation restartAndPush(String opName, String appName, DebugSupport debugSupport,
			RunState runState, UserInteractions ui)  {
		return new RestartExistingApplicationOperation(opName, model, appName, debugSupport, runState, this, ui);
	}

	public CloudApplicationOperation restartOnly(IProject project, String appName,
			RunState preferredState) {
		return new ApplicationRestartOnlyOp(appName, this.model, preferredState);
	}

	public CloudApplicationOperation createRestartPush(IProject project,
			CloudApplicationDeploymentProperties properties, RunState runOrDebug, UserInteractions ui,
			IProgressMonitor monitor) throws Exception {
		List<CloudApplicationOperation> deploymentOperations = new ArrayList<CloudApplicationOperation>();

		CloudApplication existingApp = model.getRunTarget().getClient().getApplication(properties.getAppName());

		if (existingApp != null && !ui.confirmOperation(APP_FOUND_TITLE,
				NLS.bind(APP_FOUND_MESSAGE, properties.getAppName(), properties.getProject().getName()))) {
			throw new OperationCanceledException();
		}

		RunState initialRunstate = RunState.STARTING;

		CloudApplicationOperation addElementOp = new AddElementOperation(properties, model, existingApp,
				initialRunstate);
		deploymentOperations.add(addElementOp);

		List<CloudApplicationOperation> uploadAndRestartOps = getPushAndRestartOperations(properties, ui);
		deploymentOperations.addAll(uploadAndRestartOps);

		CloudApplicationOperation op = new CompositeApplicationOperation(
				"Deploying and starting project: " + project.getName(), model, properties.getAppName(),
				deploymentOperations);
		return op;
	}

	/**
	 * Convenience method to return a list of ops, in case the caller wants to
	 * build their own ops.
	 *
	 * @return non-null list of ops that perform upload, update and restart
	 */
	public List<CloudApplicationOperation> getPushAndRestartOperations(CloudApplicationDeploymentProperties properties,
			UserInteractions ui) {
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
		deploymentOperations.add(new ApplicationPushOperation(properties, model, ui));
		RunState preferredState = null;
		deploymentOperations.add(restartOnly(properties.getProject(), properties.getAppName(), preferredState));

		return deploymentOperations;
	}

}
