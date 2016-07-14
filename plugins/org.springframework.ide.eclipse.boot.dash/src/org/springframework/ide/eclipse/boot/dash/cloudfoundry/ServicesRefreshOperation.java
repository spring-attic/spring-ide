/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFServiceInstance;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.ClientRequests;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.ops.CloudOperation;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.ops.RefreshSchedulingRule;
import org.springframework.ide.eclipse.boot.util.Log;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;

public class ServicesRefreshOperation extends CloudOperation{

//	private static final boolean DEBUG = (""+Platform.getLocation()).contains("kdvolder");

//	private static void debug(String msg) {
//		if (DEBUG) {
//			System.out.println(msg);
//		}
//	}

	final private CloudDashElementFactory elementFactory;

	public ServicesRefreshOperation(CloudFoundryBootDashModel model) {
		super("Refresh Cloud Services", model);
		this.elementFactory = model.getElementFactory();
	}

	@Override
	protected void doCloudOp(IProgressMonitor monitor) throws Exception, OperationCanceledException {
		monitor.beginTask("Refresh services", 2);
		boolean success = false;
		try {
			ClientRequests client = getClientRequests();
			monitor.worked(1);
			if (client!=null) {
//				debug("Resfres Services for connected client");
				List<CFServiceInstance> serviceInfos = client.getServices();
				Builder<CloudServiceInstanceDashElement> services = ImmutableSet.builder();
				for (CFServiceInstance service : serviceInfos) {
					services.add(elementFactory.createService(service));
				}
				model.setServices(services.build());
				success = true;
			}
		} catch (Exception e) {
			//If NW is down, typically the same error will happen in parallel for refresing the aps.
			//We don't want a double popup so just log this here instead of letting it fly.
			Log.log(e);
		} finally {
			if (!success) {
//				debug("Resfresh Services for DISconnected client");
				model.setServices(ImmutableSet.<CloudServiceInstanceDashElement>of());
			}
			monitor.done();
		}
	}

	public ISchedulingRule getSchedulingRule() {
		return new RefreshSchedulingRule(model.getRunTarget());
	}

}
