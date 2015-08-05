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

import org.cloudfoundry.client.lib.CloudFoundryOperations;
import org.cloudfoundry.client.lib.domain.ApplicationStats;
import org.cloudfoundry.client.lib.domain.CloudApplication;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudAppInstances;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryBootDashModel;

/**
 * A cloud operation that is performed on a Cloud application.
 *
 */
public abstract class CloudApplicationOperation extends CloudOperation {

	protected String appName;

	protected final CloudFoundryBootDashModel model;

	private ApplicationUpdateListener applicationUpdateListener;

	public CloudApplicationOperation(String opName, CloudFoundryBootDashModel model, String appName) {
		super(opName);
		this.model = model;
		applicationUpdateListener = new ApplicationUpdateListener.DefaultListener(appName, model);
		this.appName = appName;
	}

	@Override
	protected CloudFoundryOperations getClient() throws Exception {
		return this.model.getCloudTarget().getClient();
	}

	protected CloudAppInstances getCloudApplicationInstances() throws Exception {
		CloudApplication app = getCloudApplication();
		if (app != null) {
			ApplicationStats stats = getClient().getApplicationStats(app);
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

	/**
	 * This may be a slow process. Call only when really needed. Otherwise, use
	 * a cache version.
	 *
	 * @return existing Cloud application in Cloud Foundry backend or null if it
	 *         does not exist.
	 * @throws Exception
	 *             if error occurred while fetching Cloud application
	 */
	protected CloudApplication getCloudApplication() throws Exception {
		try {
			return getClient().getApplication(appName);
		} catch (Exception e) {
			// Ignore if 404
			if (!e.getMessage().contains("404")) {
				throw e;
			}
		}
		return null;
	}

	protected Exception getApplicationException(Exception e) {
		if (is404Error(e)) {
			IStatus status = BootDashActivator.createErrorStatus(e,
					"Application not found in Cloud Foundry target when verifying that it exists: " + e.getMessage());
			return new CoreException(status);
		}
		return e;
	}

	protected boolean is404Error(Exception e) {
		return e.getMessage() != null && e.getMessage().contains("404");
	}

	public ISchedulingRule getSchedulingRule() {
		return new CloudApplicationSchedulingRule(model.getRunTarget(), appName);
	}

}
