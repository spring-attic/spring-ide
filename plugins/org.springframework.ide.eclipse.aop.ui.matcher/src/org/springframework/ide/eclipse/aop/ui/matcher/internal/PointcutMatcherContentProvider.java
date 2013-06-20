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
package org.springframework.ide.eclipse.aop.ui.matcher.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.search.ui.text.AbstractTextSearchViewPage;
import org.springframework.ide.eclipse.aop.core.model.IAopReference;
import org.springframework.ide.eclipse.aop.ui.matcher.PointcutMatcherResultPage;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansModelElement;
import org.springframework.ide.eclipse.beans.core.model.generators.BeansConfigFactory;
import org.springframework.ide.eclipse.beans.core.model.generators.BeansConfigId;
import org.springframework.ide.eclipse.core.model.IModelElement;

/**
 * Content provider that knows how to display {@link IAopReference} which
 * represent pointcut matches.
 * @author Christian Dupuis
 * @since 2.0.2
 */
public class PointcutMatcherContentProvider implements ITreeContentProvider {

	private PointcutMatcherResult result;

	private PointcutMatcherResultPage pointcutMatcherResultPage;

	private StructuredViewer viewer;

	public PointcutMatcherContentProvider(
			PointcutMatcherResultPage pointcutMatcherResultPage) {
		this.pointcutMatcherResultPage = pointcutMatcherResultPage;
	}

	public void clear() {
		// nothing to do
	}

	public void dispose() {
		// nothing to do
	}

	public void elementsChanged(Object[] elements) {
		viewer.refresh();
	}

	public Object[] getChildren(Object parentElement) {
		if (result == null) {
			return IModelElement.NO_CHILDREN;
		}
		if (parentElement instanceof IFile) {
			parentElement = BeansCorePlugin.getModel().getConfig(
					BeansConfigId.create((IFile) parentElement));
		}

		// handle tree layout
		if (this.pointcutMatcherResultPage.getLayout() == AbstractTextSearchViewPage.FLAG_LAYOUT_TREE) {
			// Create list of matched element's children which belong to given
			// parent element
			Object[] elements = result.getElements();
			List<Object> children = new ArrayList<Object>();
			for (Object element : elements) {
				if (element instanceof IAopReference
						&& parentElement instanceof IBeansModelElement) {
					IAopReference reference = (IAopReference) element;
					IModelElement me = BeansCorePlugin.getModel().getElement(
							reference.getTargetBeanId());
					if (me != null) {
						Object child = BeansModelUtils.getChildForElement(
								(IModelElement) parentElement,
								(IModelElement) me);
						if (child instanceof IBeansConfig) {
							child = ((IBeansConfig) child).getElementResource();
						}
						else if (child == null && me instanceof IBean
								&& me.equals(parentElement)) {
							child = reference;
						}
						if (child != null && !children.contains(child)) {
							children.add(child);
						}
					}
				}
			}
			return children.toArray(new Object[children.size()]);
		}
		// handle list layout
		else {
			return IModelElement.NO_CHILDREN;
		}
	}

	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof PointcutMatcherResult) {
			result = (PointcutMatcherResult) inputElement;
		}
		else {
			result = null;
			return IModelElement.NO_CHILDREN;
		}
		
		// handle tree layout
		if (this.pointcutMatcherResultPage.getLayout() == 
			AbstractTextSearchViewPage.FLAG_LAYOUT_TREE) {
			// Create list of projects the matched beans belong to
			Object[] elements = result.getElements();
			List<IModelElement> projects = new ArrayList<IModelElement>();
			for (Object element : elements) {
				if (element instanceof IAopReference) {
					IAopReference reference = (IAopReference) element;
					IModelElement me = BeansCorePlugin.getModel().getElement(
							reference.getTargetBeanId());
					if (me != null) {
						IModelElement project = BeansModelUtils
								.getChildForElement(BeansCorePlugin.getModel(),
										(IModelElement) me);
						if (!projects.contains(project)) {
							projects.add(project);
						}
					}
				}
			}
			return projects.toArray(new IModelElement[projects.size()]);
		}
		// handle list layout
		else {
			Object[] elements = result.getElements();
			List<IAopReference> matches = new ArrayList<IAopReference>();
			for (Object element : elements) {
				if (element instanceof IAopReference) {
					matches.add((IAopReference) element);
				}
			}
			return matches.toArray(new IAopReference[matches.size()]);
		}
	}

	public Object getParent(Object element) {
		return null;
	}

	public boolean hasChildren(Object element) {
		return getChildren(element).length != 0;
	}

	public final void inputChanged(Viewer viewer, Object oldInput,
			Object newInput) {
		if (viewer instanceof StructuredViewer) {
			this.viewer = (StructuredViewer) viewer;
		}
		else {
			this.viewer = null;
		}
	}
}
