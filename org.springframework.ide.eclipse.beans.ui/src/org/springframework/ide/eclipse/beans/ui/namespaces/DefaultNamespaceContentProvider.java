/*******************************************************************************
 * Copyright (c) 2005, 2010 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.namespaces;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.metadata.BeansMetadataPlugin;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.ISourceModelElement;

/**
 * This class is a content provider which knows about the beans core model's
 * {@link ISourceModelElement source elements} which belong to a namespace.
 * 
 * @author Torsten Juergeleit
 */
public class DefaultNamespaceContentProvider implements ITreeContentProvider {

	public Object[] getElements(Object inputElement) {
		return getChildren(BeansCorePlugin.getModel());
	}

	public boolean hasChildren(Object element) {
		if (element instanceof ISourceModelElement) {
			return (((ISourceModelElement) element).getElementChildren()
					.length > 0);
		}
		return false;
	}

	public Object[] getChildren(Object parentElement) {
		// Special handling for IBean to get annotated IBeanProperties into the tree
		if (parentElement instanceof IBean) {
			java.util.List<IModelElement> children = new ArrayList<IModelElement>();
			children.addAll(Arrays.asList(((ISourceModelElement) parentElement).getElementChildren()));
			children.addAll(BeansMetadataPlugin.getMetadataModel().getBeanProperties((IBean) parentElement));
			return children.toArray(new Object[children.size()]);
		}
		else if (parentElement instanceof ISourceModelElement) {
			return ((ISourceModelElement) parentElement).getElementChildren();
		}
		return IModelElement.NO_CHILDREN;
	}

	public Object getParent(Object element) {
		if (element instanceof ISourceModelElement) {
			return ((ISourceModelElement) element).getElementParent();
		}
		return null;
	}

	public void dispose() {
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}
}
