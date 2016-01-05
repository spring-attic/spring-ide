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

import java.util.ArrayList;
import java.util.List;

import org.cloudfoundry.client.lib.domain.CloudDomain;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.ApplicationManifestHandler;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryBootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;

public class ManifestDescriptorResolver extends DeploymentDescriptorResolver {

	@Override
	public CloudApplicationDeploymentProperties getProperties(IProject project, String appName,
			CloudFoundryBootDashModel model, UserInteractions ui, IProgressMonitor monitor) throws Exception {
		List<CloudDomain> domains = model.getCloudTarget().getDomains(monitor);

		// First detect if there are any manifest files before opening any UI
		List<IPath> manifestFiles = getManifestFiles(project);

		if (!manifestFiles.isEmpty()) {
			IPath path = ui.selectDeploymentManifestFile(project,
					null /* no manifest currently selected yet */);

			if (path != null) {
				ApplicationManifestHandler manifestHandler = new ApplicationManifestHandler(project, domains,
						path.toString());
				List<CloudApplicationDeploymentProperties> appProperties = manifestHandler.load(monitor);
				if (appProperties == null || appProperties.isEmpty()) {
					throw BootDashActivator.asCoreException("manifest file detected for the project "
							+ project.getName()
							+ ", but failed to parse any deployment information. Please verify that the manifest file is correct.");
				} else {
					// Save manifest
					CloudApplicationDeploymentProperties deploymentProperties = appProperties.get(0);
					if (deploymentProperties.writeManifest()) {
						manifestHandler.create(monitor, deploymentProperties);
					}
					return deploymentProperties;
				}
			} else {
				// if nothing is selected, cancel
				throw new OperationCanceledException();
			}
		}

		return null;
	}

	protected List<IPath> getManifestFiles(IProject project) {
		List<IPath> paths = new ArrayList<IPath>();
		try {
			findFiles(project, "manifest", "yml", paths);
		} catch (CoreException e) {
			BootDashActivator.log(e);
		}
		return paths;
	}

	public static void findFiles(IResource resource, String startsWith, String extension, List<IPath> paths)
			throws CoreException {

		if (resource == null || !resource.exists()) {
			return;
		}
		if (resource.exists() && resource.getLocation() != null && resource instanceof IFile
				&& resource.getName().startsWith(startsWith)
				&& ((IFile) resource).getFileExtension().equals(extension)) {
			paths.add(resource.getLocation());
		} else if (resource instanceof IContainer) {
			IContainer container = (IContainer) resource;
			IResource[] children = container.members();

			if (children != null) {
				for (IResource child : children) {
					findFiles(child, startsWith, extension, paths);
				}
			}
		}
	}
}
