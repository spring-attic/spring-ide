/*******************************************************************************
 * Copyright (c) 2006, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.search.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.generators.BeansConfigFactory;
import org.springframework.ide.eclipse.beans.ui.model.BeansModelContentProvider;
import org.springframework.ide.eclipse.core.model.IModelElement;

/**
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 */
public class BeansSearchContentProvider extends BeansModelContentProvider {

	private BeansSearchResult result;

	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof BeansSearchResult) {
			result = (BeansSearchResult) inputElement;
		} else {
			result = null;
			return IModelElement.NO_CHILDREN;
		}

		// Create list of projects the matched beans belong to
		Object[] elements = result.getElements();
		List<IModelElement> projects = new ArrayList<IModelElement>();
		for (Object element : elements) {
			if (element instanceof IModelElement) {
				IModelElement project = BeansModelUtils.getChildForElement(
						BeansCorePlugin.getModel(), (IModelElement) element);
				if (!projects.contains(project)) {
					projects.add(project);
				}
			}
		}
		return projects.toArray(new IModelElement[projects.size()]);
	}

	@Override
    public boolean hasChildren(Object element) {
		if (element instanceof IBean) {
			return false;
		}
		return super.hasChildren(element);
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if (result == null) {
			return IModelElement.NO_CHILDREN;
		}
		if (parentElement instanceof IFile) {
			parentElement = BeansCorePlugin.getModel().getConfig(BeansConfigFactory.getConfigId((IFile) parentElement));
		}

		// Create list of matched element's children which belong to given
		// parent element
		Object[] matches = result.getElements();
		List<Object> children = new ArrayList<Object>();
		for (Object match : matches) {
			Object child = BeansModelUtils.getChildForElement(
					(IModelElement) parentElement, (IModelElement) match);
			if (child instanceof IBeansConfig) {
				child = ((IBeansConfig) child).getElementResource();
			}
			if (child != null && !children.contains(child)) {
				children.add(child);
			}
		}
		return children.toArray(new Object[children.size()]);
	}

	public void elementsChanged(Object[] elements) {
		getViewer().refresh();
	}

	public void clear() {
		getViewer().refresh();
	}
}
