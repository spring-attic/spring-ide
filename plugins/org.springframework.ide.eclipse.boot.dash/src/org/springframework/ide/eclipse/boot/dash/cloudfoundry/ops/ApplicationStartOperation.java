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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.ApplicationRunningStateTracker;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryBootDashModel;

public class ApplicationStartOperation extends CloudApplicationOperation {

	private final String appName;

	public ApplicationStartOperation(String appName, CloudFoundryBootDashModel model) {
		super("Starting application: " + appName, model, appName);
		this.appName = appName;
	}

	@Override
	protected void doCloudOp(IProgressMonitor monitor) throws Exception, OperationCanceledException {

		// Use the cached Cloud app instead of fetching a new one to avoid
		// unnecessary
		// network I/O. An updated Cloud application will be fetched when the op
		// completes
		logAndUpdateMonitor("Starting application: " + appName, monitor);

		getAppUpdateListener().applicationStarting(getCachedApplication());

		getClient().restartApplication(appName);

		new ApplicationRunningStateTracker(appName, getClient(), appLogger).startTracking(monitor);
	}

	public ISchedulingRule getSchedulingRule() {
		return new CloudApplicationSchedulingRule(model.getRunTarget(), appName);
	}
}
