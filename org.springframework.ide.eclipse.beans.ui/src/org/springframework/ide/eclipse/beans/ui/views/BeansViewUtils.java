/*
 * Copyright 2002-2006 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.springframework.ide.eclipse.beans.ui.views;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.views.navigator.LocalSelectionTransfer;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansConfigSet;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansProject;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.beans.ui.views.model.ConfigNode;
import org.springframework.ide.eclipse.beans.ui.views.model.INode;
import org.springframework.ide.eclipse.core.SpringCore;
import org.springframework.ide.eclipse.core.SpringCoreUtils;
import org.springframework.ide.eclipse.ui.SpringUIMessages;
import org.springframework.ide.eclipse.ui.SpringUIPlugin;

/**
 * Helper classe for DND support in beans view.
 * 
 * @author Pierre-Antoine Grégoire
 * @author Torsten Juergeleit
 */
public final class BeansViewUtils {

	/**
	 * Returns the resource selection from the LocalSelectionTransfer.
	 * 
	 * @return the resource selection from the LocalSelectionTransfer
	 */
	public static Object getSelectedObjects() {
		ArrayList selectedResources = new ArrayList();
		ArrayList selectedNodes = new ArrayList();
		Object result = null;
		ISelection selection = LocalSelectionTransfer.getInstance()
				.getSelection();
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection ssel = (IStructuredSelection) selection;
			for (Iterator it = ssel.iterator(); it.hasNext();) {
				Object o = it.next();
				if (o instanceof IResource) {
					selectedResources.add(o);
				} else if (o instanceof IAdaptable) {
					IAdaptable a = (IAdaptable) o;
					IResource r = (IResource) a.getAdapter(IResource.class);
					if (r != null) {
						selectedResources.add(r);
					} else {
						if (o instanceof INode) {
							selectedNodes.add(o);
						}
					}
				}
			}
		}
		if (!selectedResources.isEmpty() && selectedNodes.isEmpty()) {
			result = selectedResources.toArray(new IResource[selectedResources
					.size()]);
		} else if (selectedResources.isEmpty() && !selectedNodes.isEmpty()) {
			result = selectedNodes.toArray(new INode[selectedNodes.size()]);
		} else {
			result = null;
		}
		return result;
	}

	/**
	 * Adds given projects to beans core model.
	 */
	public static void addProjects(IResource[] projects) {
		for (int i = 0; i < projects.length; i++) {
			if (projects[i] instanceof IProject) {
				IProject project = (IProject) projects[i];
				if (!SpringCoreUtils.isSpringProject(project)) {
					try {
						SpringCoreUtils.addProjectNature(project,
							  SpringCore.NATURE_ID, new NullProgressMonitor());
					} catch (CoreException e) {
						MessageDialog.openError(
							SpringUIPlugin.getActiveWorkbenchShell(),
							SpringUIMessages.ProjectNature_errorMessage,
							NLS.bind(SpringUIMessages.ProjectNature_addError,
								  project.getName(), e.getLocalizedMessage()));
					}
				}
			}
		}
	}

	/**
	 * Adds given configs to specified beans project.
	 */
	public static void addConfigs(IResource[] configs, IBeansProject project) {
		BeansProject proj = (BeansProject) project;
		for (int i = 0; i < configs.length; i++) {
			if (configs[i] instanceof IFile) {
				proj.addConfig((IFile) configs[i], false);
			}
		}
		proj.saveDescription();
	}

	/**
	 * Adds given config nodes to specified beans project.
	 */
	public static void addNodes(INode[] nodes, IBeansConfigSet configSet) {
		BeansProject project = (BeansProject) configSet.getElementParent();
		for (int i = 0; i < nodes.length; i++) {
			if (nodes[i] instanceof ConfigNode) {
				ConfigNode configNode = (ConfigNode) nodes[i];

				// Make sure that config belongs to same project as config set
				if (configNode.getProjectNode().getProject().equals(project)) {
					((BeansConfigSet) configSet).addConfig(configNode.
							getName());
				}
			}
		}
		project.saveDescription();
	}

	public static int getResourcesCommonType(IResource[] resources) {
		int result = IResource.NONE;
		for (int i = 0; i < resources.length; i++) {
			IResource resource = resources[i];
			// if there are resources of different types, consider there are no
			// resources
			if (result != IResource.NONE && resource.getType() != result) {
				result = IResource.NONE;
				break;
			} else {
				result = resource.getType();
			}
		}
		return result;
	}

	public static boolean areResourcesFromTheSameProject(
			IResource[] resources) {
		String projectId = null;
		for (int i = 0; i < resources.length; i++) {
			IResource resource = resources[i];
			if (projectId != null
					&& !(resource.getProject().getName().equals(projectId))) {
				return false;
			}
		}
		return true;
	}

	public static boolean areAllResourcesCompilationUnits(
			IResource[] resources) {
		for (int i = 0; i < resources.length; i++) {
			if (!isCompilationUnit(resources[i])) {
				return false;
			}
		}
		return true;
	}

	public static boolean areAllResourcesConfigFiles(IResource[] resources,
													 IBeansProject project) {
		for (int i = 0; i < resources.length; i++) {
			if (!hasConfigFileExtension(resources[i], project)) {
				return false;
			}
		}
		return true;
	}

	public static boolean isCompilationUnit(IResource resource) {
		if (resource.getAdapter(ICompilationUnit.class) != null) {
			return true;
		}
		return false;
	}

	public static boolean hasConfigFileExtension(IResource resource,
												 IBeansProject project) {
		if (resource instanceof IFile &&
					project.hasConfigExtension(
							((IFile) resource).getFileExtension())) {
			return true;
		}
		return false;
	}
}
