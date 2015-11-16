/*******************************************************************************
 * Copyright (c) 2015 Pivotal Software, Inc.
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
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudDashElement;
import org.springsource.ide.eclipse.commons.cloudfoundry.client.diego.HealthCheckSupport;

/**
 * @author Kris De Volder
 */
public class SetHealthCheckOperation extends CloudApplicationOperation {

	private String hcType;
	private CloudDashElement app;

	public SetHealthCheckOperation(CloudDashElement app, String hcType) {
		super("set-health-check "+app.getName()+" "+hcType, app.getCloudModel(), app.getName());
		this.app = app;
		this.hcType = hcType;
	}

	@Override
	protected void doCloudOp(IProgressMonitor monitor) throws Exception, OperationCanceledException {
		monitor.beginTask(getName(), 2);
		try {
			HealthCheckSupport hc = app.getCloudModel().getCloudTarget().getHealthCheckSupport();
			UUID guid = app.getAppGuid();
			String current = hc.getHealthCheck(guid);
			//When current==null it means that there's no 'health-check' info in the info returned by
			//cloudcontroller. Probably this means app has no support for this and so we shouldn't try to
			//set it.
			monitor.worked(1);
			if (current!=null) {
				if (!current.equals(hcType)) {
					hc.setHealthCheck(guid, hcType);
					monitor.worked(1);
				}
			}
		} finally {
			monitor.done();
		}
	}

}
