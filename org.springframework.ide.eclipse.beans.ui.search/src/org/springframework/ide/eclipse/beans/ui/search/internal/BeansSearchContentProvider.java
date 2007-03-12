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

package org.springframework.ide.eclipse.beans.ui.search.internal;

import java.util.ArrayList;
import java.util.List;

import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.ui.model.BeansModelContentProvider;
import org.springframework.ide.eclipse.core.model.IModelElement;

/**
 * @author Torsten Juergeleit
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
	public Object[] getChildren(Object parentElement) {
		if (result == null) {
			return IModelElement.NO_CHILDREN;
		}

		// Create list of matched element's children which belong to given
		// parent element
		Object[] matches = result.getElements();
		List<IModelElement> children = new ArrayList<IModelElement>();
		for (Object element0 : matches) {
			IModelElement element = (IModelElement) element0;
			IModelElement child = BeansModelUtils.getChildForElement(
					(IModelElement) parentElement, element);
			if (child != null) {
				if (child instanceof IBeansConfig) {
					
				}
				if (!children.contains(child)) {
					children.add(child);
				}
			}
		}
		return children.toArray(new IModelElement[children.size()]);
	}

	public void elementsChanged(Object[] elements) {
		getViewer().refresh();
	}

	public void clear() {
		getViewer().refresh();
	}
}
