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
 * Listener to be used only when running a start operation atomically and not
 * part of list of dependent ops (for example, should not be used as part of a
 * deployment operation that also includes application creation and upload)
 *
 */
public class StartOnlyUpdateListener extends ApplicationUpdateListener {

	public StartOnlyUpdateListener(String appName, CloudFoundryBootDashModel model) {
		super(appName, model);
	}

	@Override
	public void applicationCreated(CloudApplication app) {
		// Ignore. Not applicable
	}

	@Override
	public void applicationUploaded(CloudApplication app) {
		// Ignore. Not applicable
	}

	@Override
	public void applicationStarted(CloudApplication app) {
		updateModel(app);
	}

	@Override
	public void applicationStarting(CloudApplication app) {
		updateModel(RunState.STARTING);
	}

}
