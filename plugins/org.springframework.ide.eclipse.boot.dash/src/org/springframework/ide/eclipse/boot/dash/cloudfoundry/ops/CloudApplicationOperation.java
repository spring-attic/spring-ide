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
import org.cloudfoundry.client.lib.domain.CloudApplication;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryBootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;

/**
 * A cloud operation that is performed on a Cloud application.
 *
 */
public abstract class CloudApplicationOperation extends CloudOperation<CloudApplication> {

	protected final String appName;

	private ApplicationUpdateListener applicationUpdateListener;

	public CloudApplicationOperation(String opName, CloudFoundryOperations client, String appName,
			CloudFoundryBootDashModel model, UserInteractions ui) {
		super(opName, client, model, ui);
		applicationUpdateListener = new ApplicationUpdateListener.DefaultListener(appName, model);
		this.appName = appName;
	}

	protected CloudApplication getCloudApplication() throws Exception {
		return getCloudApplication(client, appName);
	}

	protected CloudApplication getCachedApplication() {
		return model.getAppCache().getApp(appName);
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
	 *
	 * @return existing Cloud application in Cloud Foundry backend or null if it
	 *         does not exist.
	 * @throws Exception
	 *             if error occurred while fetching Cloud application
	 */
	public static CloudApplication getCloudApplication(CloudFoundryOperations client, String appName) throws Exception {
		try {
			return client.getApplication(appName);
		} catch (Exception e) {
			// Ignore if 404
			if (!e.getMessage().contains("404")) {
				throw e;
			}
		}
		return null;
	}

	protected Exception getApplicationException(Exception e) {
		if (e.getMessage() != null) {
			if (e.getMessage().contains("404")) {
				IStatus status = BootDashActivator.createErrorStatus(e,
						"Application not found in Cloud Foundry target when verifying that it exists: "
								+ e.getMessage());
				return new CoreException(status);
			}

		}
		return e;
	}

	public ISchedulingRule getSchedulingRule() {
		return new CloudApplicationSchedulingRule(model.getRunTarget(), appName);
	}

}
