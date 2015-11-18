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

import java.util.Objects;
import java.util.UUID;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudAppCache;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudDashElement;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryBootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springsource.ide.eclipse.commons.cloudfoundry.client.diego.HealthCheckSupport;

/**
 * TODO: when we use v2 java client then this should probably be removed as the
 * healthcheck information will be integrated with the basi summary of an element.
 *
 * @author Kris De Volder
 */
public class HealthCheckRefreshOperation extends CloudOperation {

	public HealthCheckRefreshOperation(CloudFoundryBootDashModel model) {
		super("Refresh health-check infos", model);
	}

	@Override
	protected void doCloudOp(IProgressMonitor monitor) throws Exception, OperationCanceledException {
		for (BootDashElement e : model.getElements().getValues()) {
			CloudDashElement cde = (CloudDashElement) e;
			HealthCheckSupport client = model.getCloudTarget().getHealthCheckSupport();
			CloudAppCache cache = model.getAppCache();
			UUID guid = cde.getAppGuid();
			if (guid!=null) {
				String newHc = client.getHealthCheck(guid);
				String oldHc = cache.getHealthCheck(cde);
				if (!Objects.equals(newHc, oldHc)) {
					cache.setHealthCheck(cde, newHc);
					cde.getParent().notifyElementChanged(cde);
				}
			}
		}
	}

}
