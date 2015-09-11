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

import org.cloudfoundry.client.lib.domain.CloudDomain;
import org.eclipse.core.resources.IProject;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudApplicationDeploymentProperties;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryBootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;

public class DeploymentOperationFactory {

	private final CloudFoundryBootDashModel model;
	private final CloudApplicationDeploymentProperties properties;
	private final IProject project;
	private final UserInteractions ui;

	public DeploymentOperationFactory(CloudFoundryBootDashModel model, CloudApplicationDeploymentProperties properties,
			IProject project, List<CloudDomain> domains, UserInteractions ui) {
		this.model = model;
		this.project = project;
		this.properties = properties;
		this.ui = ui;
	}

	public CloudApplicationOperation getDeploymentOperationExistingApp() {
		List<CloudApplicationOperation> uploadAndRestartOps = getUploadUpdateRestartOps(RunState.STARTING);

		CloudApplicationOperation op = new CompositeApplicationOperation(
				"Re-deploying and re-starting project: " + project.getName(), model, properties.getAppName(),
				uploadAndRestartOps);
		return op;
	}

	public CloudApplicationOperation getDeploymentOperation() {
		List<CloudApplicationOperation> deploymentOperations = new ArrayList<CloudApplicationOperation>();

		CloudApplicationOperation createOp = new ApplicationCreateOperation(properties, model, RunState.STARTING);
		deploymentOperations.add(createOp);

		List<CloudApplicationOperation> uploadAndRestartOps = getUploadUpdateRestartOps(null);
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
	protected List<CloudApplicationOperation> getUploadUpdateRestartOps(RunState preferredRunState) {
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
}
