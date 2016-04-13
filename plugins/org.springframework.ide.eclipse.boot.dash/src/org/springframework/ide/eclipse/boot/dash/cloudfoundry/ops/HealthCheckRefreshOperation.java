/*******************************************************************************
 * Copyright (c) 2015, 2016 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cloudfoundry.ops;

import java.util.UUID;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudAppDashElement;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryBootDashModel;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.ClientRequests;

/**
 * TODO: when we use v2 java client then this should probably be removed as the
 * healthcheck information will be integrated with the basic summary of an element.
 *
 * @author Kris De Volder
 */
public class HealthCheckRefreshOperation extends CloudOperation {

	public HealthCheckRefreshOperation(CloudFoundryBootDashModel model) {
		super("Refresh health-check infos", model);
	}

	@Override
	protected void doCloudOp(IProgressMonitor monitor) throws Exception, OperationCanceledException {
		OperationsExecution exec = model.getOperationsExecution();
		for (final CloudAppDashElement cde : model.getApplications().getValues()) {
			final UUID guid = cde.getAppGuid();
			if (guid!=null) {
				CloudApplicationOperation singleAppRefresh = new CloudApplicationOperation("Refresh healthcheck for '"+cde.getName(), model, cde.getName(), cde.createCancelationToken()) {
					@Override
					protected void doCloudOp(IProgressMonitor monitor) throws Exception, OperationCanceledException {
						ClientRequests client = getClientRequests();
						cde.setHealthCheck(client.getHealthCheck(guid));
					}
				};
				exec.runAsynch(singleAppRefresh);
			}
		}
	}

}
