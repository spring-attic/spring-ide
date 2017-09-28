/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.livegraph.actions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jst.ws.internal.common.ResourceUtils;
import org.springframework.ide.eclipse.beans.ui.live.actions.OpenBeanDefinitionAction;
import org.springframework.ide.eclipse.beans.ui.live.model.LiveBean;
import org.springframework.ide.eclipse.beans.ui.live.model.TypeLookup;
import org.springframework.ide.eclipse.beans.ui.livegraph.LiveGraphUiPlugin;
import org.springsource.ide.eclipse.commons.core.StatusHandler;
import org.springsource.ide.eclipse.commons.ui.SpringUIUtils;

/**
 * Legacy action to open XML bean resource
 * 
 * @author Alex Boyko
 *
 */
@SuppressWarnings("restriction")
public class OpenXmlBeanDefinitionAction extends OpenBeanDefinitionAction {
	
	public OpenXmlBeanDefinitionAction() {
		super();
	}

	private void openXmlFiles(List<String> contexts, TypeLookup workspaceContext) {
		try {
			IProject[] projects = workspaceContext.relatedProjects();
			for (IProject project : projects) {
				IPath[] paths = ResourceUtils.getAllJavaSourceLocations(project);
				if (paths.length > 0) {
					for (IPath path : paths) {
						IResource resource = ResourceUtils.findResource(path);
						if (resource.exists()) {
							resource.accept(new ResourceProxyVisitor(contexts), IContainer.EXCLUDE_DERIVED);
						}
					}
				}
				else {
					project.accept(new ResourceProxyVisitor(contexts), IContainer.EXCLUDE_DERIVED);
				}
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
		List<?> elements = selection.toList();
		TypeLookup session = null;
		final List<String> contexts = new ArrayList<String>();
		for (Object obj : elements) {
			if (obj instanceof LiveBean) {
				LiveBean bean = (LiveBean) obj;
				session = bean.getTypeLookup();
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
				}
			}
		}

		if (session != null) {
			// Find the XML files in the workspace and open them
			openXmlFiles(contexts, session);
		}
		
		// Open Java classes
		super.run();
	}

	private class ResourceProxyVisitor implements IResourceProxyVisitor {

		private final List<String> contexts;

		private ResourceProxyVisitor(List<String> contexts) {
			this.contexts = contexts;
		}

		public boolean visit(IResourceProxy proxy) throws CoreException {
			if (IResource.FILE == proxy.getType()) {
				for (String appContext : contexts) {
					if (appContext.equals(proxy.getName().trim())) {
						SpringUIUtils.openInEditor((IFile) proxy.requestResource(), 0);
					}
				}
				return false;
			}
			return true;
		}

	}


}
