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

import java.io.File;
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
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudDashElement;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryBootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;

public class ManifestDescriptorResolver extends DeploymentDescriptorResolver {

	@Override
	public CloudApplicationDeploymentProperties getProperties(IProject project, String appName,
			CloudFoundryBootDashModel model, UserInteractions ui, IProgressMonitor monitor) throws Exception {
		List<CloudDomain> domains = model.getCloudTarget().getDomains(monitor);

		// NOTE: Element may not yet exist at this stage. Be sure to do null
		// checks where appropriate
		CloudDashElement element = model.getElement(appName);

		// First detect if there are any manifest files before opening any UI
		List<IPath> manifestFiles = getManifestFiles(project);
		IPath path = null;

		CloudApplicationDeploymentProperties deploymentProperties = null;
		if (!manifestFiles.isEmpty()) {

			if (element == null || !exists(element.getDeploymentManifestFile(), project)) {
				IPath existingPath = element != null ? element.getDeploymentManifestFile() : null;
				path = ui.selectDeploymentManifestFile(project, existingPath);
			} else {
				path = element.getDeploymentManifestFile();
			}

			if (path != null) {

				ApplicationManifestHandler manifestHandler = new ApplicationManifestHandler(project, domains,
						path.toString());
				List<CloudApplicationDeploymentProperties> appProperties = manifestHandler.load(monitor);
				if (appProperties == null || appProperties.isEmpty()) {
					throw BootDashActivator.asCoreException("manifest file detected for the project "
							+ project.getName()
							+ ", but failed to parse any deployment information. Please verify that the manifest file is correct.");
				} else {
					deploymentProperties = appProperties.get(0);
				}
			} else {
				// if nothing is selected, cancel
				throw new OperationCanceledException();
			}
		}

		// Element may not exist at this stage so always check that it exists
		// before persisting change
		if (element != null) {
			element.setDeploymentManifestFile(path);
		}

		// Set it in the properties as well in case the element needs to be
		// created later, so that the manifest is also saved
		if (deploymentProperties != null) {
			deploymentProperties.setManifestPath(path);
		}

		return deploymentProperties;
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

	protected boolean exists(IPath path, IProject project) {
		if (path == null) {
			return false;
		}
		if (path.isAbsolute()) {
			File file = new File(path.toString());
			return file.exists();
		} else {
			IFile file = project.getFile(path);
			return file.exists();
		}
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
