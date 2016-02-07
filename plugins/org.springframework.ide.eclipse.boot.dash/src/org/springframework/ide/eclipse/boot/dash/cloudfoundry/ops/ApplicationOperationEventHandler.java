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

import org.eclipse.core.runtime.OperationCanceledException;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudAppInstances;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryBootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.RunState;

/**
 * Fires events and checks if an operation should be terminated while the
 * operation is running
 */
public abstract class ApplicationOperationEventHandler {

	protected final CloudFoundryBootDashModel model;

	public ApplicationOperationEventHandler(CloudFoundryBootDashModel model) {
		this.model = model;
	}

	public void onError(String appName, Throwable t) {
		model.updateApplication(appName, RunState.UNKNOWN);
	}

	abstract public void checkTerminate(CloudAppInstances appInstances) throws OperationCanceledException;

	public void fireEvent(ApplicationOperationEvent event, boolean checkTerminate) throws OperationCanceledException {
		if (event == null) {
			return;
		}
		event.fire();
		if (checkTerminate) {
			checkTerminate(event.getAppInstances());
		}
	}
}
