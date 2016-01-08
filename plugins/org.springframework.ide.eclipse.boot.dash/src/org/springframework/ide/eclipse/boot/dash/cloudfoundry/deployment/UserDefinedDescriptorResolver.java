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

import java.util.List;

import org.cloudfoundry.client.lib.domain.CloudApplication;
import org.cloudfoundry.client.lib.domain.CloudDomain;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.ApplicationManifestHandler;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryBootDashModel;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryRunTarget;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;

public class UserDefinedDescriptorResolver extends DeploymentDescriptorResolver {

	@Override
	public CloudApplicationDeploymentProperties getProperties(IProject project, String appName,
			CloudFoundryBootDashModel model, UserInteractions ui, IProgressMonitor monitor) throws Exception {
		CloudFoundryRunTarget runTarget = model.getRunTarget();
		List<CloudDomain> domains = runTarget.getDomains(monitor);
		CloudApplicationDeploymentProperties deploymentProperties = ui.promptApplicationDeploymentProperties(project,
				domains);

		if (deploymentProperties == null) {
			// if no deployment properties specified, cancel
			throw new OperationCanceledException();
		}

		// Get the app AGAIN in case the app name was changed in the UI
		// (e.g. a user wants to "link" the project to another
		// existing application with a different name)
		CloudApplication existingApp = runTarget.getClientRequests().getApplication(deploymentProperties.getAppName());
		if (existingApp != null) {

			CloudApplicationDeploymentProperties existingProps = CloudApplicationDeploymentProperties
					.getFor(existingApp, project);

			deploymentProperties = deploymentProperties.mergeInto(existingProps);
		}

		// Create default manifest
		if (deploymentProperties.writeManifest()) {
			ApplicationManifestHandler manifestHandler = new ApplicationManifestHandler(project, domains,
					deploymentProperties.getManifestPath());
			manifestHandler.create(monitor, deploymentProperties);
		}
		return deploymentProperties;
	}

}
