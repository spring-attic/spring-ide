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
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudAppDashElement;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFApplicationDetail;
import org.springframework.ide.eclipse.boot.dash.util.CancelationTokens.CancelationToken;

public class ApplicationStopOperation extends CloudApplicationOperation {

	private boolean updateElementRunState = true;
	private CloudAppDashElement app;

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
	 * @param cancelationToken
	 */
	public ApplicationStopOperation(CloudAppDashElement app, boolean updateElementRunState, CancelationToken cancelationToken) {
		super("Stopping application", app.getCloudModel(), app.getName(), cancelationToken);
		this.app = app;
		this.updateElementRunState = updateElementRunState;
	}

	@Override
	protected void doCloudOp(IProgressMonitor monitor) throws Exception, OperationCanceledException {
		model.getRunTarget().getClient().stopApplication(this.appName);

		// If the element run state in the model needs to be updated (i.e. the
		// "view" needs to show the app to be stopped)
		if (updateElementRunState) {
			model.getElementConsoleManager().terminateConsole(this.appName);

			CFApplicationDetail updatedInstances = model.getRunTarget().getClient().getApplication(this.appName);
			app.setDetailedData(updatedInstances);
			checkTerminationRequested(monitor);
		}
	}

	public ISchedulingRule getSchedulingRule() {
		return new StopApplicationSchedulingRule(model.getRunTarget(), appName);
	}
}
