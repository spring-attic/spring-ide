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
package org.springframework.ide.eclipse.boot.dash.cloudfoundry.ops;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudfoundry.client.lib.domain.CloudApplication;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudAppInstances;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryBootDashModel;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFApplication;

/**
 * Operation for refreshing existing cloud applications.
 *
 * @author Alex Boyko
 *
 */
public class RefreshApplications extends CloudOperation {

	private Collection<CFApplication> apps;

	public RefreshApplications(CloudFoundryBootDashModel model, Collection<CFApplication> apps) {
		super("Refreshing applications", model);
		this.apps = apps;
	}

	@Override
	protected void doCloudOp(IProgressMonitor monitor) throws Exception, OperationCanceledException {
		if (apps != null && !apps.isEmpty()) {
			List<CloudAppInstances> updatedApplications = new ArrayList<>();
			List<CFApplication> toUpdateStats = new ArrayList<>();

			for (CloudAppInstances instances : model.getAppCache().getAppInstances()) {
				if (apps.contains(instances.getApplication())) {
					CFApplication newApplication = model.getRunTarget().getClient()
							.getApplication(instances.getApplication().getName());
					updatedApplications.add(new CloudAppInstances(newApplication, instances.getStats()));
					toUpdateStats.add(newApplication);
				} else {
					updatedApplications.add(instances);
				}
			}

			model.updateElements(updatedApplications);

			new AppInstancesRefreshOperation(model, toUpdateStats).run(monitor);
		}
	}

}
