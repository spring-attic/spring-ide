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

import org.cloudfoundry.client.lib.domain.CloudApplication;
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

	public void updateModel(RunState runState) {
		model.getAppCache().update(appName, runState);

	}

	public void updateModel(CloudApplication app) {
		model.getAppCache().update(app);
	}

	public void onError(Exception e) {
		model.getAppCache().update(appName, RunState.INACTIVE);
	}

	abstract public void applicationCreated(CloudApplication app);

	abstract public void applicationUploaded(CloudApplication app);

	abstract public void applicationStarted(CloudApplication app);

	abstract public void applicationStarting(CloudApplication app);

	static class DefaultListener extends ApplicationUpdateListener {

		public DefaultListener(String appName, CloudFoundryBootDashModel model) {
			super(appName, model);
		}

		@Override
		public void applicationCreated(CloudApplication app) {
		}

		@Override
		public void applicationUploaded(CloudApplication app) {
		}

		@Override
		public void applicationStarted(CloudApplication app) {
		}

		@Override
		public void applicationStarting(CloudApplication app) {
		}

	}

}
