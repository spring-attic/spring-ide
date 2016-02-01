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
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFService;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.ClientRequests;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.ops.CloudOperation;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;

public class ServicesRefreshOperation extends CloudOperation{

	final private CloudDashElementFactory elementFactory;

	public ServicesRefreshOperation(CloudFoundryBootDashModel model, CloudDashElementFactory elementFactory) {
		super("Refresh Cloud Services", model);
		this.elementFactory = elementFactory;
	}

	@Override
	protected void doCloudOp(IProgressMonitor monitor) throws Exception, OperationCanceledException {
		monitor.beginTask("Refresh services", 2);
		try {
			ClientRequests client = getClientRequests();
			monitor.worked(1);
			if (client!=null) {
				List<CFService> serviceInfos = client.getServices();
				Builder<CloudServiceDashElement> services = ImmutableSet.builder();
				for (CFService service : serviceInfos) {
					services.add(elementFactory.createService(service));
				}
				model.setServices(services.build());
			}
		} finally {
			monitor.done();
		}
	}

}
