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
package org.springframework.ide.eclipse.boot.dash.cloudfoundry;

import org.cloudfoundry.client.lib.CloudFoundryOperations;
import org.cloudfoundry.client.lib.domain.CloudApplication;
import org.cloudfoundry.client.lib.domain.CloudApplication.AppState;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;

public class ApplicationStartOperation extends CloudOperation<CloudApplication> {

	public static final long START_TIMEOUT = 1000 * 60 * 5;

	public static final long WAIT_TIME = 1000 * 5;

	private final String appName;

	private final CloudFoundryBootDashModel model;

	public ApplicationStartOperation(String appName, CloudFoundryBootDashModel model, CloudFoundryOperations client,
			UserInteractions ui) {
		super("Starting application", client, ui);
		this.appName = appName;
		this.model = model;
	}

	@Override
	protected CloudApplication doCloudOp(CloudFoundryOperations client, IProgressMonitor monitor) throws Exception {

		CloudDashElement element = model.getElement(appName);

		if (element == null) {
			throw BootDashActivator.asCoreException("No Cloud element found for: " + appName
					+ ". Unable to restart the application. Refresh the target and make sure the application still exists.");
		}

		client.startApplication(appName);

		CloudApplication app = element.refreshCloudApplication(monitor);

		// fetch an updated Cloud Application that reflects changes that
		// were
		// performed on it. Make sure the element app reference is updated
		// as
		// run state of the element depends on the app being up to date.
		// Wait for application to be started
		long total = START_TIMEOUT;
		while (app.getState() != AppState.STARTED && (total -= WAIT_TIME) > 0) {
			try {
				Thread.sleep(WAIT_TIME);
			} catch (InterruptedException e) {

			}
			app = element.refreshCloudApplication(monitor);
		}

		model.notifyElementChanged(element);

		return app;
	}

	public ISchedulingRule getSchedulingRule() {
		return new CloudApplicationSchedulingRule(model.getRunTarget(), appName);
	}

}
