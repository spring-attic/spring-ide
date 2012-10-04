/*******************************************************************************
 *  Copyright (c) 2012 VMware, Inc.
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
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.actions.BaseSelectionListenerAction;
import org.springframework.ide.eclipse.beans.ui.livegraph.LiveGraphUiPlugin;
import org.springframework.ide.eclipse.beans.ui.livegraph.model.LiveBean;
import org.springsource.ide.eclipse.commons.core.JdtUtils;
import org.springsource.ide.eclipse.commons.core.SpringCoreUtils;
import org.springsource.ide.eclipse.commons.core.StatusHandler;
import org.springsource.ide.eclipse.commons.ui.SpringUIUtils;

/**
 * @author Leo Dos Santos
 */
public class OpenBeanDefinitionAction extends BaseSelectionListenerAction {

	public OpenBeanDefinitionAction() {
		super("Open Bean Definition File");
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
				final String appContext = bean.getResource();
				if (appContext != null && appContext.trim().length() > 0 && !appContext.equalsIgnoreCase("null")) {
					String resourceStr = null;

					// extract the resource path out of the descriptive text
					int indexStart = appContext.indexOf("[");
					int indexEnd = appContext.indexOf("]");
					if (indexStart > -1 && indexEnd > -1 && indexStart < indexEnd) {
						resourceStr = appContext.substring(indexStart + 1, indexEnd);
					}

					if (resourceStr != null) {
						if (resourceStr.endsWith(".xml")) {
							// Strip the path until we can map it properly to a
							// project resource. For new we're going to traverse
							// the project structure to open XML files
							if (resourceStr.contains(File.separator)) {
								int pathSeparator = resourceStr.lastIndexOf(File.separator);
								resourceStr = resourceStr.substring(pathSeparator + 1);
								contexts.add(resourceStr);
							}
						}
						else if (resourceStr.endsWith(".class")) {
							// Strip the path until we can map it properly to a
							// project resource. For now if the .class file name
							// matches the bean type, open the bean type.
							if (resourceStr.contains(File.separator)) {
								int pathSeparator = resourceStr.lastIndexOf(File.separator);
								String className = resourceStr.substring(pathSeparator + 1,
										resourceStr.lastIndexOf(".class"));
								String beanType = bean.getBeanType();
								if (beanType != null && beanType.endsWith(className) && appName != null) {
									try {
										IProject project = SpringCoreUtils.createProject(appName, null,
												new NullProgressMonitor());
										IType type = JdtUtils.getJavaType(project, beanType);
										SpringUIUtils.openInEditor(type);
									}
									catch (CoreException e) {
										StatusHandler.log(new Status(IStatus.ERROR, LiveGraphUiPlugin.PLUGIN_ID,
												"An error occurred while attempting to open a class file.", e));
									}
								}
							}
						}
					}
				}
			}
		}

		if (appName != null) {
			// find the XML files in the workspace and open them
			try {
				IProject project = SpringCoreUtils.createProject(appName, null, new NullProgressMonitor());
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
			catch (CoreException e) {
				StatusHandler.log(new Status(IStatus.ERROR, LiveGraphUiPlugin.PLUGIN_ID,
						"An error occurred while attempting to open an application context file.", e));
			}
		}
	}

	@Override
	protected boolean updateSelection(IStructuredSelection selection) {
		if (!selection.isEmpty()) {
			List elements = selection.toList();
			for (Object obj : elements) {
				if (obj instanceof LiveBean) {
					LiveBean bean = (LiveBean) obj;
					String appContext = bean.getResource();
					if (appContext != null && appContext.trim().length() > 0 && !appContext.equalsIgnoreCase("null")) {
						return true;
					}
				}
			}
		}
		return false;
	}

}
