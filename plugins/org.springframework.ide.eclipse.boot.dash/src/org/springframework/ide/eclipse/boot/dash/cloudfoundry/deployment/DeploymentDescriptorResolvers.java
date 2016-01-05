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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryRunTarget;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;

public class DeploymentDescriptorResolvers {

	// Added from highest (manifest parsing) to lowest (user defined) priority
	protected DeploymentDescriptorResolver[] resolvers = { new ManifestDescriptorResolver(),
			new ExistingAppDescriptorResolver(), new UserDefinedDescriptorResolver() };

	public CloudApplicationDeploymentProperties getProperties(IProject project, String appName,
			CloudFoundryRunTarget runTarget, UserInteractions ui, IProgressMonitor monitor)
					throws Exception {

		CloudApplicationDeploymentProperties deploymentProperties = null;
		for (DeploymentDescriptorResolver resolver : resolvers) {
			deploymentProperties = resolver.getProperties(project, appName, runTarget, ui, monitor);
			if (deploymentProperties != null) {
				break;
			}
		}

		// Set any default buildpack that may be defined for the target, if the
		// buildpack is not set already
		if (deploymentProperties != null && deploymentProperties.getBuildpack() == null) {
			String buildpack = runTarget.getBuildpack(project);
			if (buildpack != null) {
				deploymentProperties.setBuildpack(buildpack);
			}
		}

		return deploymentProperties;
	}

}
