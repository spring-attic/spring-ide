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

import java.util.UUID;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.ApplicationRunningStateTracker;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudAppInstances;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryBootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.RunState;

public class ApplicationStartOperation extends CloudApplicationOperation {

	private final RunState preferredState;

	public ApplicationStartOperation(String appName, CloudFoundryBootDashModel model, RunState preferredState) {
		super("Starting application: " + appName, model, appName);
		addOperationEventHandler(new StartingOperationHandler(model));
		this.preferredState = preferredState;
	}

	public ApplicationStartOperation(String appName, CloudFoundryBootDashModel model) {
		super("Starting application: " + appName, model, appName);
		addOperationEventHandler(new StartingOperationHandler(model));
		this.preferredState = null;
	}

	@Override
	protected void doCloudOp(IProgressMonitor monitor) throws Exception, OperationCanceledException {

		CloudAppInstances appInstances = requests.getExistingApplicationInstances(appName);
		if (appInstances == null) {
			throw BootDashActivator.asCoreException(
					"Unable to start the application. Application does not exist anymore in Cloud Foundry: " + appName);
		}

		// Get the guid, as it is more efficient for lookup
		UUID appGuid = appInstances.getApplication().getMeta().getGuid();

		boolean checkTermination = true;
		this.eventHandler.fireEvent(eventFactory.updateRunState(appInstances, getDashElement(), preferredState),
				checkTermination);

		// Use the cached Cloud app instead of fetching a new one to avoid
		// unnecessary
		// network I/O. An updated Cloud application will be fetched when the op
		// completes
		logAndUpdateMonitor("Starting application: " + appName, monitor);

		requests.restartApplication(appName);

		RunState runState = new ApplicationRunningStateTracker(getDashElement(), requests, model, eventHandler)
				.startTracking(monitor);
		CloudAppInstances updatedInstances = requests.getExistingAppInstances(appGuid);

		checkTermination = false;
		this.eventHandler.fireEvent(eventFactory.updateRunState(updatedInstances, getDashElement(), runState),
				checkTermination);
	}

	public ISchedulingRule getSchedulingRule() {
		return new StartApplicationSchedulingRule(model.getRunTarget(), appName);
	}
}
