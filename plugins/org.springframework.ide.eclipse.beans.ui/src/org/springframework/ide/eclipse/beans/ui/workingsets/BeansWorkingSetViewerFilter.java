/*******************************************************************************
 * Copyright (c) 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.workingsets;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.ui.IWorkingSet;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.BeansCoreUtils;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansModelElement;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.ISpringProject;
import org.springframework.ide.eclipse.ui.workingsets.IWorkingSetFilter;

/**
 * {@link IWorkingSetFilter} that filters those {@link IModelElement}s that are
 * not in a given list of elements derived form a {@link IWorkingSet}.t
 * @author Christian Dupuis
 * @since 2.0
 */
public class BeansWorkingSetViewerFilter implements IWorkingSetFilter {

	public boolean isInWorkingSet(IAdaptable[] elements, Object parentElement,
			Object element) {

		if (element instanceof ISpringProject) {
			IProject project = ((ISpringProject) element).getProject();
			if (checkProject(elements, project)) {
				return true;
			}
		}
		else if (element instanceof IBeansProject) {
			IProject project = ((IBeansProject) element).getProject();
			if (checkProject(elements, project)) {
				return true;
			}
		}
		// check if the TreePath contains a IBeansProject
		else if (parentElement instanceof TreePath) {
			IProject project = getProjectFromTreePath((TreePath) parentElement);
			if (checkProject(elements, project)) {
				for (IAdaptable adaptable : elements) {
					if (element.equals(adaptable)) {
						return true;
					}
					else if (adaptable instanceof IFile) {
						IBeansConfig bc = BeansCorePlugin.getModel().getConfig(
								(IFile) adaptable);
						if (element.equals(bc)) {
							return true;
						}
					}
					else if (adaptable instanceof IBeansConfigSet) {
						if (adaptable.equals(element)) {
							return true;
						}
					}

					if (element instanceof IBeansModelElement
							&& !(element instanceof IBeansConfigSet)) {
						return true;
					}
				}
			}
		}

		return false;
	}

	private boolean checkProject(IAdaptable[] elements, IProject project) {
		if (project == null) {
			return false;
		}
		for (IAdaptable adaptable : elements) {
			IResource resource = (IResource) adaptable
					.getAdapter(IResource.class);
			if (resource != null && BeansCoreUtils.isBeansConfig(resource)) {
				if (project.equals(resource.getProject())) {
					return true;
				}
			}
			else if (adaptable instanceof IBeansConfigSet) {
				IBeansProject bp = BeansModelUtils
						.getProject((IBeansConfigSet) adaptable);
				if (project.equals(bp.getProject())) {
					return true;
				}
			}
		}
		return false;
	}

	private IProject getProjectFromTreePath(TreePath treePath) {
		for (int i = 0; i < treePath.getSegmentCount(); i++) {
			if (treePath.getSegment(i) instanceof IBeansProject) {
				return ((IBeansProject) treePath.getSegment(i)).getProject();
			}
		}
		return null;
	}
}
