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
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.views.navigator.LocalSelectionTransfer;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansConfigSet;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansProject;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.beans.ui.model.BeansModelLabelDecorator;
import org.springframework.ide.eclipse.beans.ui.views.model.BeanNode;
import org.springframework.ide.eclipse.beans.ui.views.model.ConfigNode;
import org.springframework.ide.eclipse.beans.ui.views.model.ConfigSetNode;
import org.springframework.ide.eclipse.beans.ui.views.model.INode;
import org.springframework.ide.eclipse.beans.ui.views.model.ProjectNode;
import org.springframework.ide.eclipse.beans.ui.views.model.RootNode;
import org.springframework.ide.eclipse.core.SpringCore;
import org.springframework.ide.eclipse.core.SpringCoreUtils;

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
	 * This method creates beanNodes as childs to a ConfigNode from java files
	 * IResources.
	 * 
	 * @param resources
	 *            the java files to add
	 * @param configNode
	 *            the parent configNode
	 * @return the created bean nodes
	 */
	public static BeanNode[] createBeanNodes(IResource[] resources,
			ConfigNode configNode) {
		// nothing for now
		return null;
	}

	/**
	 * This method creates configNodes as childs to a ProjectNode from Xml files
	 * IResources.
	 * 
	 * @param resources
	 *            the Xml files to add
	 * @param projectNode
	 *            the parent projectNode
	 * @return the created config nodes
	 */
	public static ConfigNode[] createConfigNodes(IResource[] resources,
			ProjectNode projectNode) {
		ConfigNode[] configNodes = new ConfigNode[resources.length];
		for (int i = 0; i < resources.length; i++) {
			String configName = resources[i].getProjectRelativePath()
					.toString();
			BeansProject beansProject = (BeansProject) projectNode.getProject();
			if (projectNode.getProject().getProject().exists(
					resources[i].getProjectRelativePath())
					&& !beansProject.hasConfig(configName)) {
				ConfigNode configNode = new ConfigNode(projectNode, configName);
				configNodes[i] = configNode;
			}
		}
		return configNodes;
	}

	/**
	 * This method creates projectNodes as childs to the RootNode ProjectNode
	 * from java projects IResources.
	 * 
	 * @param resources
	 *            the java projects to add
	 * @param rootNode
	 *            the parent rootNode
	 * @return the created project nodes
	 */
	public static ProjectNode[] createProjectNodes(IResource[] resources,
			RootNode rootNode) {
		ProjectNode[] projectNodes = new ProjectNode[resources.length];
		for (int i = 0; i < resources.length; i++) {
			if (!SpringCoreUtils.isSpringProject(resources[i])) {
				IProject project = (IProject) resources[i];
				ProjectNode projectNode = new ProjectNode(rootNode, project
						.getName());
				BeansProject beansProject = new BeansProject(project);
				projectNode.setElement(beansProject);
				projectNodes[i] = projectNode;
			}
		}
		return projectNodes;
	}

	/**
	 * This method adds beanNodes to the beansView as childs to a ConfigNode.
	 * 
	 * @param treeViewer
	 *            the beansView treeViewer
	 * @param beanNodes
	 *            the beanNodes to add
	 * @param configNode
	 *            the configNode under which they're added
	 */
	public static void addBeanNodes(TreeViewer treeViewer,
			BeanNode[] beanNodes, ConfigNode configNode) {
		// nothing for now
	}

	/**
	 * This method adds configNodes to the beansView as childs to a ProjectNode.
	 * 
	 * @param treeViewer
	 *            the beansView treeViewer
	 * @param configNodes
	 *            the configNodes to add
	 * @param projectNode
	 *            the projectNode under which they're added
	 */
	public static void addConfigNodes(TreeViewer treeViewer,
			ConfigNode[] configNodes, ProjectNode projectNode) {
		for (int i = 0; i < configNodes.length; i++) {
			BeansProject beansProject = (BeansProject) projectNode.getProject();
			// if the file is not in the target project, we'll copy it to the
			// target project if possible.
			configNodes[i].setParent(projectNode);
			projectNode.addConfig(configNodes[i].getName());
			beansProject.setConfigs(projectNode.getConfigNames(), true);
			BeansModelLabelDecorator.update();
			treeViewer.add(projectNode, configNodes[i]);
			treeViewer.reveal(configNodes[i]);
		}
	}

	/**
	 * This method adds configNodes to a ConfigSetNode and refreshes the
	 * ConfigSet's display.
	 * 
	 * @param treeViewer
	 *            the beansView treeViewer
	 * @param configNodes
	 *            the configNodes to add
	 * @param configSetNode
	 *            the configSetNode under which they're added
	 */
	public static void addConfigNodes(TreeViewer treeViewer,
			ConfigNode[] configNodes, ConfigSetNode configSetNode) {
		for (int i = 0; i < configNodes.length; i++) {
			ConfigNode droppedConfigNode = (ConfigNode) configNodes[i];
			if (droppedConfigNode.getProjectNode().getProject().equals(
					configSetNode.getProjectNode().getProject())) {
				BeansConfigSet beansConfigSet = (BeansConfigSet) configSetNode
						.getConfigSet();
				BeansProject beansProject = (BeansProject) configSetNode
						.getProjectNode().getProject();
				beansConfigSet.addConfig(droppedConfigNode.getName());
				configSetNode.addConfig(droppedConfigNode);
				List configSets = new ArrayList();
				for (Iterator it = configSetNode.getProjectNode()
						.getConfigSets().iterator(); it.hasNext();) {
					ConfigSetNode node = (ConfigSetNode) it.next();
					BeansConfigSet configSet = new BeansConfigSet(beansProject,
							node.getName(), node.getConfigNames());
					configSet.setAllowBeanDefinitionOverriding(node
							.isOverrideEnabled());
					configSet.setIncomplete(node.isIncomplete());
					configSets.add(configSet);
				}
				beansProject.setConfigSets(configSets, true);
				BeansModelLabelDecorator.update();
				treeViewer.reveal(configSetNode);
			}
		}
	}

	/**
	 * This method adds projectNodes to the beansView as childs to the RootNode.
	 * 
	 * @param treeViewer
	 *            the beansView treeViewer
	 * @param projectNodes
	 *            the projectNodes to add
	 * @param rootNode
	 *            the rootNode under which they're added
	 */
	public static void addProjectNodes(TreeViewer treeViewer,
			ProjectNode[] projectNodes, RootNode rootNode) {
		for (int i = 0; i < projectNodes.length; i++) {
			SpringCoreUtils.addProjectNature(projectNodes[i].getProject()
					.getProject(), SpringCore.NATURE_ID);
			projectNodes[i].setParent(rootNode);
			rootNode.addProject(projectNodes[i].getName(), new ArrayList(),
								new ArrayList(), new ArrayList());
			BeansModelLabelDecorator.update();
			treeViewer.add(rootNode, projectNodes[i]);
			treeViewer.reveal(projectNodes[i]);
		}
	}

	/**
	 * This method casts an INode[] table to a ConfigNode[] table.
	 * 
	 * @param nodes
	 *            the original table
	 * @return a ConfigNode[] table
	 */
	public static ConfigNode[] castToConfigNodes(INode[] nodes) {
		ConfigNode[] configNodes = new ConfigNode[nodes.length];
		for (int i = 0; i < nodes.length; i++) {
			configNodes[i] = (ConfigNode) nodes[i];
		}
		return configNodes;
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

	public static boolean areAllResourcesJavaProjects(
			IResource[] resources) {
		for (int i = 0; i < resources.length; i++) {
			if (!SpringCoreUtils.isJavaProject(resources[i])) {
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
