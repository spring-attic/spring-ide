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

import java.util.UUID;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.ApplicationRunningStateTracker;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudAppDashElement;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudAppInstances;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryBootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.RunState;

/**
 * Restarts the application in Cloud Foundry. Does not create, update or push the
 * application resources.
 *
 */
public class ApplicationRestartOnlyOp extends CloudApplicationOperation {

	private CloudAppDashElement app;

	public ApplicationRestartOnlyOp(CloudAppDashElement cde) {
		super("Starting application: " + cde.getName(), cde.getCloudModel(), cde.getName());
		this.app = cde;
	}


	@Override
	protected void doCloudOp(IProgressMonitor monitor) throws Exception, OperationCanceledException {
		app.startOperationStarting();
		try {
			CloudAppInstances appInstances = model.getRunTarget().getClient().getExistingAppInstances(appName);
			if (appInstances == null) {
				throw BootDashActivator.asCoreException(
						"Unable to start the application. Application does not exist anymore in Cloud Foundry: " + appName);
			}

			// Get the guid, as it is more efficient for lookup
			UUID appGuid = appInstances.getApplication().getGuid();
			checkTerminationRequested(monitor);

			// Use the cached Cloud app instead of fetching a new one to avoid
			// unnecessary
			// network I/O. An updated Cloud application will be fetched when the op
			// completes
			logAndUpdateMonitor("Starting application: " + appName, monitor);

			model.getRunTarget().getClient().restartApplication(appName);

			RunState runState = new ApplicationRunningStateTracker(this, app)
					.startTracking(monitor);
			CloudAppInstances updatedInstances = model.getRunTarget().getClient().getExistingAppInstances(appGuid);
			app.setInstanceData(updatedInstances);

			app.startOperationEnded(null);
		} catch (Throwable e) {
			app.startOperationEnded(e);
		}
	}

	public ISchedulingRule getSchedulingRule() {
		return new StartApplicationSchedulingRule(model.getRunTarget(), appName);
	}
}
