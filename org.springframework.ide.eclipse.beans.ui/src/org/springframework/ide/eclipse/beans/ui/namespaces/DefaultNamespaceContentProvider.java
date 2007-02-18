/*
 * Copyright 2002-2007 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ide.eclipse.beans.ui.namespaces;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.model.IBeanConstructorArgument;
import org.springframework.ide.eclipse.beans.core.model.IBeanProperty;
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
			return !(element instanceof IBeanProperty
					|| element instanceof IBeanConstructorArgument);
		}
		return false;
	}

	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof ISourceModelElement) {
			return ((ISourceModelElement) parentElement).getElementChildren();
		}
		return ISourceModelElement.NO_CHILDREN;
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
