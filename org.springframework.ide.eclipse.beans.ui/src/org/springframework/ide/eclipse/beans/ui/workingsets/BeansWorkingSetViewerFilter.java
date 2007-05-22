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
package org.springframework.ide.eclipse.beans.ui.workingsets;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.IWorkingSet;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.BeansCoreUtils;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
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
		for (IAdaptable elem : elements) {
			if ((element instanceof IModelElement && elem instanceof IModelElement)
					&& (elem.equals(element)
							|| BeansModelUtils.getChildForElement(
									(IModelElement) elem,
									(IModelElement) element) != null || BeansModelUtils
							.getChildForElement((IModelElement) element,
									(IModelElement) elem) != null)) {
				return true;
			}
			else if (element instanceof ISpringProject) {
				IResource resource = (IResource) elem
						.getAdapter(IResource.class);
				if (resource != null
						&& resource.getProject().equals(
								((ISpringProject) element).getProject())) {
					return true;
				}
			}
			else if (element instanceof IModelElement && elem instanceof IFile
					&& BeansCoreUtils.isBeansConfig((IFile) elem)) {
				IBeansConfig bc = BeansCorePlugin.getModel().getConfig(
						(IFile) elem);
				if (bc.equals(element)
						|| BeansModelUtils.getChildForElement(
								(IModelElement) element, bc) != null
						|| BeansModelUtils.getChildForElement(bc,
								(IModelElement) element) != null) {
					return true;
				}
			}
			else if (element instanceof IFile) {
				IResource resource = (IResource) elem
						.getAdapter(IResource.class);
				if (resource != null && resource.equals((IFile) element)) {
					return true;
				}
			}
			else if (elem instanceof IBeansConfigSet
					&& element instanceof IBean) {
				if (((IBeansConfigSet) elem).getBeans().contains(element)) {
					return true;
				}
			}
		}

		return false;
	}
}
