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

import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudAppInstances;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryBootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.RunState;

/**
 * Notifier that notifies the model of element change that starts with:
 * <p/>
 * 1. Application creation (the "starting" stage): model is notified that the
 * application is "Starting"
 * <p/>
 * 2. Application upload (updates to the model are ignored at this stage to
 * avoid incorrect app run state. Cloud Foundry may indicate the application is
 * "started" during upload, so during this stage, updates to the app run state
 * in the model are ignored
 * <p/>
 * 3. Application restart: final stage where the model is updated with the
 * current run state of the application in Cloud Foundry
 *
 */
public class FullAppDeploymentListener extends ApplicationUpdateListener {

	public FullAppDeploymentListener(String appName, CloudFoundryBootDashModel model) {
		super(appName, model);
	}

	@Override
	public void applicationCreated(CloudAppInstances appInstances) {
		// This is the "starting" point of a full deployment
		// Update the model to indicate the application is starting
		// Run state is set to STARTING after the application is created
		// as before, the element in the model may not yet exist
		updateModel(appInstances, RunState.STARTING);
	}

	@Override
	public void applicationStarting(CloudAppInstances appInstances) {
		// Ignore since the actual starting operation in a full deployment is
		// not the "starting" point
	}

	@Override
	public void applicationUpdated(CloudAppInstances appInstances) {
		updateModel(appInstances, RunState.STARTING);
	}
}
