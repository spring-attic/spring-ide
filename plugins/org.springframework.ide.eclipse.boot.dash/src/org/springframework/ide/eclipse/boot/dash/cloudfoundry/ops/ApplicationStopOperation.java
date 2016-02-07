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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.ApplicationRunningStateTracker;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudAppInstances;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryBootDashModel;

public class ApplicationStopOperation extends CloudApplicationOperation {

	private boolean updateElementRunState = true;

	/**
	 * Note some stop operations are part of a larger composite operation that
	 * has a preferred runState (e.g. STARTING) that does not reflect the actual
	 * run state of the app in CF. For example, as part of a large restart
	 * operation, an app may be first stopped before restarted. However, in
	 * these cases the app run state in the model should not be updated as to
	 * not stomp the preferred state of the app.
	 *
	 * @param appName
	 * @param model
	 * @param updateElementRunState
	 *            true if element run state in model should be updated. False
	 *            otherwise.
	 */
	public ApplicationStopOperation(String appName, CloudFoundryBootDashModel model, boolean updateElementRunState) {
		super("Stopping application", model, appName);
		this.updateElementRunState = updateElementRunState;
	}

	@Override
	protected void doCloudOp(IProgressMonitor monitor) throws Exception, OperationCanceledException {
		model.getRunTarget().getClient().stopApplication(this.appName);

		// If the element run state in the model needs to be updated (i.e. the
		// "view" needs to show the app to be stopped)
		if (updateElementRunState) {
			model.getElementConsoleManager().terminateConsole(this.appName);

			CloudAppInstances updatedInstances = model.getRunTarget().getClient().getExistingAppInstances(this.appName);

			boolean checkTermination = false;
			this.eventHandler.fireEvent(eventFactory.getUpdateRunStateEvent(updatedInstances, getDashElement(),
					ApplicationRunningStateTracker.getRunState(updatedInstances)), checkTermination);
		}

	}

	public ISchedulingRule getSchedulingRule() {
		return new StopApplicationSchedulingRule(model.getRunTarget(), appName);
	}
}
