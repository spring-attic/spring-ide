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
 * Listener that is informed when application events have occurred. For example,
 * after application creation, upload or start.
 *
 */
public abstract class ApplicationUpdateListener {

	private final CloudFoundryBootDashModel model;
	private final String appName;

	public ApplicationUpdateListener(String appName, CloudFoundryBootDashModel model) {
		this.model = model;
		this.appName = appName;
	}

	public String getApplicationName() {
		return appName;
	}

	public void updateModel(RunState runState) {
		model.updateApplication(appName, runState);
	}

	public void updateModel(CloudAppInstances appInstances, RunState runState) {
		model.updateApplication(appInstances, runState);
	}

	public void updateModel(CloudAppInstances appInstances) {
		model.updateApplication(appInstances);
	}

	public void onError(Throwable t) {
		updateModel(RunState.UNKNOWN);
	}

	public void onOperationTermination(CloudAppInstances appInstances) {
		if (appInstances != null) {
			updateModel(appInstances);
		} else {
			updateModel(RunState.UNKNOWN);
		}
	}

	abstract public void applicationCreated(CloudAppInstances app);

	abstract public void applicationStarting(CloudAppInstances app);

	abstract public void applicationUpdated(CloudAppInstances app);

	static class DefaultListener extends ApplicationUpdateListener {

		public DefaultListener(String appName, CloudFoundryBootDashModel model) {
			super(appName, model);
		}

		@Override
		public void applicationCreated(CloudAppInstances app) {
			// TODO Auto-generated method stub
		}

		@Override
		public void applicationStarting(CloudAppInstances app) {
			// TODO Auto-generated method stub
		}

		@Override
		public void applicationUpdated(CloudAppInstances app) {
			// TODO Auto-generated method stub
		}
	}
}
