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
package org.springframework.ide.eclipse.boot.dash.cloudfoundry.deployment;

import org.cloudfoundry.client.lib.domain.CloudApplication;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryBootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;

public class ExistingAppDescriptorResolver extends DeploymentDescriptorResolver {

	@Override
	public CloudApplicationDeploymentProperties getProperties(IProject project, String appName,
			CloudFoundryBootDashModel model, UserInteractions ui, IProgressMonitor monitor) throws Exception {

		CloudApplication app = model.getCloudTarget().getClientRequests().getApplication(appName);
		if (app != null) {
			return CloudApplicationDeploymentProperties.getFor(app, project);
		}
		return null;
	}

}
