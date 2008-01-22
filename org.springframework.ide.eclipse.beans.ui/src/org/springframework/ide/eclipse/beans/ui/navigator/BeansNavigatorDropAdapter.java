/*******************************************************************************
 * Copyright (c) 2005, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.navigator;

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
import org.springframework.ide.eclipse.beans.core.internal.model.BeansConfigSet;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansProject;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansModelElement;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.beans.ui.model.BeansModelLabelDecorator;
import org.springframework.ide.eclipse.core.SpringCoreUtils;
import org.springframework.ide.eclipse.core.model.ISpringProject;

/**
 * {@link CommonDropAdapterAssistant} that handles drop requests of
 * {@link IResource} instances to the Spring Explorer and requests the origin
 * from within the Spring Explorer, like {@link IBeansConfig} dropped on
 * {@link IBeansConfigSet}.
 * @author Christian Dupuis
 * @since 2.0.2
 */
public class BeansNavigatorDropAdapter extends CommonDropAdapterAssistant {

	/**
	 * Resolve the current {@link IBeansModelElement} from the drop target.
	 * <p>
	 * If dragged element can't be resolved to an instance of
	 * {@link IBeansModelElement} <code>null</code> will be returned.
	 */
	private IBeansModelElement getBeansModelElementFromTarget(
			IResource resource, Object target) {
		if (target instanceof IWorkspaceRoot) {
			return BeansCorePlugin.getModel().getProject(resource.getProject());
		}
		else if (target instanceof ISpringProject) {
			return BeansCorePlugin.getModel().getProject(
					((ISpringProject) target).getProject());
		}
		else if (target instanceof IBeansModelElement) {
			return (IBeansModelElement) target;
		}
		return null;
	}

	/**
	 * Resolve {@link IProject} instance from the given
	 * {@link IBeansModelElement}.
	 */
	private IProject getProject(IBeansModelElement modelElement) {
		IBeansProject beansProject = BeansModelUtils.getProject(modelElement);
		if (beansProject != null) {
			return beansProject.getProject();
		}
		return null;
	}

	/**
	 * Resolve the current {@link IResource} from the drop target.
	 * <p>
	 * If dragged element can't be resolved to an instance of {@link IResource}
	 * <code>null</code> will be returned.
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
			return (IResource) ((IAdaptable) object)
					.getAdapter(IResource.class);
		}
		else {
			return null;
		}
	}

	/**
	 * Executes the drop action. First it is checked if the dropped object can
	 * be resolved to an {@link IResource} and if the corresponding
	 * {@link IProject} has the Spring nature applied.
	 * <p>
	 * If so the dropped object will be added as a {@link IBeansConfig} to the
	 * corresponding {@link IBeansProject} and - if applicable - added to the
	 * target {@link IBeansConfigSet}.
	 */
	@Override
	public IStatus handleDrop(CommonDropAdapter dropAdapter,
			DropTargetEvent dropTargetEvent, Object target) {
		IResource resource = getResourceFromDropTarget(dropTargetEvent);

		// handle drag'n drop from resource
		if (SpringCoreUtils.isSpringProject(resource)
				&& resource instanceof IFile) {
			IFile file = (IFile) resource;
			IBeansModelElement parent = getBeansModelElementFromTarget(
					resource, target);
			// handle resource drop to project or IWorkspaceRoot
			if (parent instanceof BeansProject) {
				BeansProject beansProject = (BeansProject) parent;
				if (!beansProject.isUpdatable()) {
					return Status.CANCEL_STATUS;
				}
				// check if target project is actually the parent of
				// resource
				IProject project = getProject(parent);
				if (resource.getProject().equals(project)
						&& !beansProject.hasConfig(file)) {
					beansProject.addConfig(file);
					return saveProject(beansProject);
				}
			}
			// handle resource drop to config set
			else if (parent instanceof BeansConfigSet) {
				BeansConfigSet beansConfigSet = (BeansConfigSet) parent;
				IProject project = getProject(parent);
				BeansProject beansProject = (BeansProject) BeansCorePlugin
						.getModel().getProject(project);
				if (!beansProject.isUpdatable()) {
					return Status.CANCEL_STATUS;
				}
				// TODO CD add support for linked project and config sets
				if (resource.getProject().equals(project)
						&& !beansConfigSet.hasConfig(file)) {
					IBeansConfig bc = BeansCorePlugin.getModel().getConfig(
							(IFile) resource);
					// check if resource is already a beans config
					if (bc != null) {
						beansConfigSet.addConfig(bc.getElementName());
					}
					else {
						beansProject.addConfig(file);
						bc = beansProject.getConfig(file);
						beansConfigSet.addConfig(bc.getElementName());
					}
					return saveProject(beansProject);
				}
			}
		}
		return Status.CANCEL_STATUS;
	}

	/**
	 * Saves the given {@link BeansProject}'s project description and refreshes
	 * the registered decorators.
	 */
	private IStatus saveProject(BeansProject beansProject) {
		beansProject.saveDescription();
		BeansModelLabelDecorator.update();
		return Status.OK_STATUS;
	}

	/**
	 * Checks if the drop request is actually support by this
	 * {@link CommonDropAdapterAssistant}.
	 * <p>
	 * Because JDT's package explorer only supports {@link DND#DROP_COPY}
	 * requests we check if this is the current operation.
	 * <p>
	 * Note: For some reason this method is called a second time (once the drop
	 * has been initiated by a mouse button release) by the common navigator
	 * framework with a possible <code>null</code> target.
	 */
	@Override
	public IStatus validateDrop(Object target, int operation,
			TransferData transferType) {
		if (operation == DND.DROP_COPY) {
			return Status.OK_STATUS;
		}
		else {
			return Status.CANCEL_STATUS;
		}
	}
}