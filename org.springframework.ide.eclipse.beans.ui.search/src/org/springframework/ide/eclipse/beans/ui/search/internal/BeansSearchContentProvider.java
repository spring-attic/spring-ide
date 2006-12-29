/*
 * Copyright 2002-2006 the original author or authors.
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
 *
 * Created on 22-Aug-2004
 */

package org.springframework.ide.eclipse.beans.ui.search.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.core.model.IModelElement;

/**
 * @author Torsten Juergeleit
 */
public class BeansSearchContentProvider implements ITreeContentProvider,
		IStructuredContentProvider {

	private Viewer viewer;
	private BeansSearchResult result;

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.viewer = viewer;
	}

	public void dispose() {
	}

	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof BeansSearchResult) {
			result = (BeansSearchResult) inputElement;
		}

		if (result == null) {
			return null;
		}

		// Create list of projects the matched beans belong to
		Object[] matches = result.getElements();
		List<IModelElement> projects = new ArrayList<IModelElement>();
		for (int i = 0; i < matches.length; i++) {
			IModelElement element = (IModelElement) matches[i];
			IModelElement project = BeansModelUtils.getChildForElement(
					BeansCorePlugin.getModel(), element);
			if (!projects.contains(project)) {
				projects.add(project);
			}
		}
		return projects.toArray(new IModelElement[projects.size()]);
	}

	public Object[] getChildren(Object parentElement) {
		if (result == null) {
			return IModelElement.NO_CHILDREN;
		}

		// Create list of matched element's child elements which belong to
		// given parent element
		Object[] matches = result.getElements();
		List<IModelElement> childs = new ArrayList<IModelElement>();
		for (int i = 0; i < matches.length; i++) {
			IModelElement element = (IModelElement) matches[i];
			IModelElement child = BeansModelUtils.getChildForElement(
					(IModelElement) parentElement, element);
			if (child != null && !childs.contains(child)) {
				childs.add(child);
			}
		}
		return childs.toArray(new IModelElement[childs.size()]);
	}

	public Object getParent(Object element) {
		return ((IModelElement) element).getElementParent();
	}

	public boolean hasChildren(Object parent) {
		return ((IModelElement) parent).getElementChildren().length > 0;
	}

	public void elementsChanged(Object[] objects) {
		viewer.refresh();
	}
}
