/*******************************************************************************
 * Copyright (c) 2007, 2009 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.webflow.ui.navigator;

import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.ui.navigator.CommonDropAdapter;
import org.eclipse.ui.navigator.CommonDropAdapterAssistant;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansModelElement;
import org.springframework.ide.eclipse.beans.core.model.generators.BeansConfigFactory;
import org.springframework.ide.eclipse.core.SpringCoreUtils;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.ISpringProject;
import org.springframework.ide.eclipse.webflow.core.Activator;
import org.springframework.ide.eclipse.webflow.core.internal.model.WebflowConfig;
import org.springframework.ide.eclipse.webflow.core.internal.model.WebflowProject;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowConfig;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowProject;

/**
 * {@link CommonDropAdapterAssistant} that handles drop requests of {@link IResource} instances to the Spring Explorer
 * and requests the origin from within the Spring Explorer, like {@link IBeansConfig} or {@link IBeansConfigSet} on
 * {@link IWebflowConfig}.
 * @author Christian Dupuis
 * @since 2.0.2
 */
public class WebflowNavigatorDropAdapter extends CommonDropAdapterAssistant {

	/**
	 * Resolve the current {@link IModelElement} from the drop target.
	 * <p>
	 * If dragged element can't be resolved to an instance of {@link IModelElement} <code>null</code> will be returned.
	 */
	private IModelElement getWebflowModelElementFromTarget(IResource resource, Object target) {
		if (target instanceof IWorkspaceRoot) {
			return Activator.getModel().getProject(resource.getProject());
		}
		else if (target instanceof ISpringProject) {
			return Activator.getModel().getProject(((ISpringProject) target).getProject());
		}
		else if (target instanceof IModelElement) {
			return (IModelElement) target;
		}
		return null;
	}

	/**
	 * Resolve {@link IProject} instance from the given {@link IModelElement}.
	 */
	private IProject getProject(IModelElement modelElement) {
		if (modelElement instanceof IWebflowProject) {
			return ((IWebflowProject) modelElement).getProject();
		}
		else if (modelElement instanceof IWebflowConfig) {
			return ((IWebflowConfig) modelElement).getElementResource().getProject();
		}
		return null;
	}

	/**
	 * Resolve the current {@link IResource} from the drop target.
	 * <p>
	 * If dragged element can't be resolved to an instance of {@link IResource} <code>null</code> will be returned.
	 */
	private IResource getResourceFromDropTarget(DropTargetEvent dropTargetEvent) {
		Object object = dropTargetEvent.data;
		if (object instanceof ITreeSelection) {
			object = ((ITreeSelection) object).getFirstElement();
		}
		if (object instanceof IResource) {
			return (IResource) object;
		}
		else if (object instanceof IAdaptable) {
			return (IResource) ((IAdaptable) object).getAdapter(IResource.class);
		}
		else {
			return null;
		}
	}

	/**
	 * Executes the drop action. First it is checked if the dropped object can be resolved to an {@link IResource} and
	 * if the corresponding {@link IProject} has the Spring nature applied.
	 * <p>
	 * If so the dropped object will be added as a {@link IWebflowConfig} to the corresponding {@link IWebflowProject}
	 * and - if applicable - added to the target {@link IWebflowConfig}'s list of linked {@link IBeansModelElement}s.
	 */
	@Override
	public IStatus handleDrop(CommonDropAdapter dropAdapter, DropTargetEvent dropTargetEvent, Object target) {
		IResource resource = getResourceFromDropTarget(dropTargetEvent);

		// handle drag'n drop from resource
		if (SpringCoreUtils.isSpringProject(resource) && resource instanceof IFile) {
			IFile file = (IFile) resource;
			IModelElement parent = getWebflowModelElementFromTarget(resource, target);
			// handle resource drop to project or IWorkspaceRoot
			if (parent instanceof WebflowProject) {
				WebflowProject webflowProject = (WebflowProject) parent;
				// check if target project is actually the parent of resource
				IProject project = getProject(parent);
				if (resource.getProject().equals(project) && webflowProject.getConfig(file) == null) {
					List<IWebflowConfig> configs = webflowProject.getConfigs();
					WebflowConfig config = new WebflowConfig(webflowProject);
					config.setResource(file);
					configs.add(config);
					webflowProject.setConfigs(configs);
					return Status.OK_STATUS;
				}
			}
			// handle BeansConfig or BeansConfigSet drop to WebflowConfig
			else if (parent instanceof WebflowConfig) {
				WebflowConfig webflowConfig = (WebflowConfig) parent;
				IProject project = getProject(parent);
				WebflowProject webflowProject = (WebflowProject) Activator.getModel().getProject(project);
				if (!webflowProject.isUpdatable()) {
					return Status.CANCEL_STATUS;
				}
				IBeansModelElement beansElement = BeansCorePlugin.getModel().getConfig(BeansConfigFactory.getConfigId(file));

				if (beansElement != null && resource.getProject().equals(project)
						&& !webflowConfig.getBeansConfigs().contains(beansElement)) {
					Set<IModelElement> beanElements = webflowConfig.getBeansConfigs();
					beanElements.add(beansElement);
					webflowConfig.setBeansConfigs(beanElements);
					webflowProject.saveDescription();
					return Status.OK_STATUS;
				}
			}
		}
		return Status.CANCEL_STATUS;
	}

	/**
	 * Checks if the drop request is actually support by this {@link CommonDropAdapterAssistant}.
	 * <p>
	 * Because JDT's package explorer only supports {@link DND#DROP_COPY} requests we check if this is the current
	 * operation.
	 * <p>
	 * Note: For some reason this method is called a second time (once the drop has been initiated by a mouse button
	 * release) by the common navigator framework with a possible <code>null</code> target.
	 */
	@Override
	public IStatus validateDrop(Object target, int operation, TransferData transferType) {
		if (operation == DND.DROP_COPY) {
			return Status.OK_STATUS;
		}
		else {
			return Status.CANCEL_STATUS;
		}
	}
}