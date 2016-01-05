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
package org.springframework.ide.eclipse.boot.dash.cloudfoundry.deployment;

import java.util.List;

import org.cloudfoundry.client.lib.domain.CloudDomain;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.ApplicationManifestHandler;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryRunTarget;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;

public class ManifestDescriptorResolver extends DeploymentDescriptorResolver {

	@Override
	public CloudApplicationDeploymentProperties getProperties(IProject project, String appName,
			CloudFoundryRunTarget runTarget, UserInteractions ui, IProgressMonitor monitor) throws Exception {
		List<CloudDomain> domains = runTarget.getDomains(monitor);

		IPath path = ui.selectDeploymentManifestFile(project, null /*no manifest currently selected yet */);

		if (path != null) {
			ApplicationManifestHandler manifestHandler = new ApplicationManifestHandler(project, domains, path.toString());

			if (manifestHandler.hasManifest()) {
				List<CloudApplicationDeploymentProperties> appProperties = manifestHandler.load(monitor);
				if (appProperties == null || appProperties.isEmpty()) {
					throw BootDashActivator.asCoreException("manifest.yml file detected for the project "
							+ project.getName()
							+ ", but failed to parse any deployment information. Please verify that the manifest.yml file is correct.");
				} else {
					// Save manifest
					CloudApplicationDeploymentProperties deploymentProperties = appProperties.get(0);
					if (deploymentProperties.writeManifest()) {
						manifestHandler.create(monitor, deploymentProperties);
					}
					return deploymentProperties;
				}
			}
		}


		return null;
	}

}
