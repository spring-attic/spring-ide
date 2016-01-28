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

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.cloudfoundry.client.lib.domain.ApplicationStats;
import org.cloudfoundry.client.lib.domain.CloudApplication;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudAppInstances;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryBootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.RefreshState;

/**
 * Refreshes the application instances.
 * <p/>
 * This will indirectly refresh the application running state as the running
 * state of an app is resolved from the number of running instances
 *
 */
public class AppInstancesRefreshOperation extends CloudOperation {

	private List<CloudApplication> appsToLookUp;

	public AppInstancesRefreshOperation(CloudFoundryBootDashModel model, List<CloudApplication> appsToLookUp) {
		super("Refreshing running state of applications in: " + model.getRunTarget().getName(), model);
		this.appsToLookUp = appsToLookUp;
	}

	@Override
	protected void doCloudOp(IProgressMonitor monitor) throws Exception {
		this.model.setRefreshState(RefreshState.loading("Fetching App Instances..."));
		try {
			if (!appsToLookUp.isEmpty()) {
				long timeToWait = 1000*30;
				Map<CloudApplication, ApplicationStats> stats = model.getRunTarget().getClient().waitForApplicationStats(appsToLookUp, timeToWait);
				for (Entry<CloudApplication, ApplicationStats> entry : stats.entrySet()) {
					CloudAppInstances instances = new CloudAppInstances(entry.getKey(), entry.getValue());
					this.model.updateApplication(instances);
				}
			}
			model.setRefreshState(RefreshState.READY);
		} catch (Exception e) {
			this.model.setRefreshState(RefreshState.error(e));
			throw e;
		}
	}

	public ISchedulingRule getSchedulingRule() {
		return new RefreshSchedulingRule(model.getRunTarget());
	}
}
