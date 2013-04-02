/*******************************************************************************
 *  Copyright (c) 2012 - 2013 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.livegraph.actions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.springframework.ide.eclipse.beans.ui.livegraph.LiveGraphUiPlugin;
import org.springframework.ide.eclipse.beans.ui.livegraph.model.LiveBean;
import org.springsource.ide.eclipse.commons.core.StatusHandler;
import org.springsource.ide.eclipse.commons.ui.SpringUIUtils;

/**
 * @author Leo Dos Santos
 */
public class OpenBeanDefinitionAction extends AbstractOpenResourceAction {

	public OpenBeanDefinitionAction() {
		super("Open Bean Definition File");
	}

	private void openXmlFiles(final List<String> contexts, String appName) {
		try {
			IProject[] projects = findProjects(appName);
			for (IProject project : projects) {
				project.accept(new IResourceVisitor() {
					public boolean visit(final IResource resource) throws CoreException {
						if (resource instanceof IFile) {
							for (String appContext : contexts) {
								if (appContext.equals(resource.getName().trim())) {
									SpringUIUtils.openInEditor((IFile) resource, 0);
								}
							}
							return false;
						}
						return true;
					}
				});
			}
		}
		catch (CoreException e) {
			StatusHandler.log(new Status(IStatus.ERROR, LiveGraphUiPlugin.PLUGIN_ID,
					"An error occurred while attempting to open an application context file.", e));
		}
	}

	@Override
	public void run() {
		IStructuredSelection selection = getStructuredSelection();
		List elements = selection.toList();
		String appName = null;
		final List<String> contexts = new ArrayList<String>();
		for (Object obj : elements) {
			if (obj instanceof LiveBean) {
				LiveBean bean = (LiveBean) obj;
				appName = bean.getApplicationName();
				String resource = bean.getResource();
				if (resource != null && resource.trim().length() > 0 && !resource.equalsIgnoreCase("null")) {
					String resourcePath = extractResourcePath(resource);
					if (resourcePath.endsWith(".xml")) {
						// Strip the path until we can map it properly to a
						// project resource. For now we're going to traverse
						// the project structure to open XML files
						if (resourcePath.contains(File.separator)) {
							int pathSeparator = resourcePath.lastIndexOf(File.separator);
							resourcePath = resourcePath.substring(pathSeparator + 1);
							contexts.add(resourcePath);
						}
					}
					else if (resourcePath.endsWith(".class")) {
						openInEditor(appName, extractClassName(resourcePath));
					}
				}
			}
		}

		if (appName != null) {
			// Find the XML files in the workspace and open them
			openXmlFiles(contexts, appName);
		}
	}

	@Override
	protected boolean updateSelection(IStructuredSelection selection) {
		if (!selection.isEmpty()) {
			List elements = selection.toList();
			for (Object obj : elements) {
				if (obj instanceof LiveBean) {
					LiveBean bean = (LiveBean) obj;
					String resource = bean.getResource();
					if (resource != null && resource.trim().length() > 0 && !resource.equalsIgnoreCase("null")) {
						return true;
					}
				}
			}
		}
		return false;
	}

}
