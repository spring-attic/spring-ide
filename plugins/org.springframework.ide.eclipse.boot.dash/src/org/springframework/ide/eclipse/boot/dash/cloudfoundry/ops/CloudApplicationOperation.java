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

import org.cloudfoundry.client.lib.domain.ApplicationStats;
import org.cloudfoundry.client.lib.domain.CloudApplication;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudAppInstances;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudErrors;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryBootDashModel;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.console.LogType;

/**
 * A cloud operation that is performed on a Cloud application.
 *
 */
public abstract class CloudApplicationOperation extends CloudOperation {

	protected String appName;


	private ApplicationUpdateListener applicationUpdateListener;

	public CloudApplicationOperation(String opName, CloudFoundryBootDashModel model, String appName) {
		super(opName, model);
		applicationUpdateListener = new ApplicationUpdateListener.DefaultListener(appName, model);
		this.appName = appName;
	}

	protected CloudAppInstances getCloudApplicationInstances() throws Exception {
		CloudApplication app = requests.getApplication(appName);
		if (app != null) {
			ApplicationStats stats = requests.getApplicationStats(app);
			return new CloudAppInstances(app, stats);
		}
		return null;
	}

	protected CloudAppInstances getCachedApplication() {
		return model.getAppCache().getAppInstances(appName);
	}

	public void addApplicationUpdateListener(ApplicationUpdateListener appUpdateListener) {
		if (appUpdateListener != null) {
			this.applicationUpdateListener = appUpdateListener;
		}
	}

	/**
	 * @return listener that updates the application state when notified. Should
	 *         not be null.
	 */
	protected ApplicationUpdateListener getAppUpdateListener() {
		return applicationUpdateListener;
	}



	protected Exception getApplicationException(Exception e) {
		if (CloudErrors.isNotFoundException(e)) {
			IStatus status = BootDashActivator.createErrorStatus(e,
					"Application not found in Cloud Foundry target when verifying that it exists: " + e.getMessage());
			return new CoreException(status);
		}
		return e;
	}

	public ISchedulingRule getSchedulingRule() {
		return new CloudApplicationSchedulingRule(model.getRunTarget(), appName);
	}

	protected void resetAndShowConsole() {
		try {
			model.getElementConsoleManager().resetConsole(appName);
			model.getElementConsoleManager().showConsole(appName);
		} catch (Exception e) {
			BootDashActivator.log(e);
		}
	}

	protected void logAndUpdateMonitor(String message, IProgressMonitor monitor) {
		if (monitor != null) {
			monitor.setTaskName(message);
		}
		try {
			model.getElementConsoleManager().writeToConsole(appName, message, LogType.LOCALSTDOUT);
		} catch (Exception e) {
			BootDashActivator.log(e);
		}
	}
}
