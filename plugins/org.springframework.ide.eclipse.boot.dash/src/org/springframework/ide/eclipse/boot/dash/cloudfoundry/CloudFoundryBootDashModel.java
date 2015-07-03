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

import java.util.List;

import org.cloudfoundry.client.lib.domain.CloudApplication;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel;
import org.springsource.ide.eclipse.commons.livexp.core.LiveSet;

public class CloudFoundryBootDashModel extends BootDashModel {

	public CloudFoundryBootDashModel(CloudFoundryRunTarget target) {
		super(target);
	}

	private LiveSet<BootDashElement> elements;

	@Override
	public LiveSet<BootDashElement> getElements() {

		if (elements == null) {
			elements = new LiveSet<BootDashElement>();
			asyncRefreshElements();
		}
		return elements;
	}

	protected void asyncRefreshElements() {
		Job job = new Job("Fetching list of Cloud applications for " + getCloudTarget().getId()) {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					List<CloudApplication> apps = getCloudTarget().getClient().getApplications();
					if (apps != null) {
						for (CloudApplication app : apps) {
							elements.add(new CloudDashElement(getCloudTarget(), app, CloudFoundryBootDashModel.this));
						}
					}
				} catch (Exception e) {
					BootDashActivator.log(e);
				}
				return Status.OK_STATUS;
			}

		};
		job.schedule();

	}

	@Override
	public void dispose() {
		elements = null;
	}

	@Override
	public void refresh() {
		asyncRefreshElements();
	}

	protected CloudFoundryRunTarget getCloudTarget() {
		return (CloudFoundryRunTarget) getRunTarget();
	}
}
