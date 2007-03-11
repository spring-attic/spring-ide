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

package org.springframework.ide.eclipse.beans.ui.navigator;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.part.IShowInTarget;
import org.eclipse.ui.part.ShowInContext;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.beans.ui.BeansUIUtils;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.IResourceModelElement;
import org.w3c.dom.Element;

/**
 * Adds an implementation of {@link IShowInTarget} to select and reveal a
 * {@link IModelElement} for a selection provided by the Beans XML Editor.
 * 
 * @author Torsten Juergeleit
 */
public class BeansExplorer extends CommonNavigator implements IShowInTarget {

	public static final String BEANS_EXPLORER_ID = Activator.PLUGIN_ID
			+ ".BeansExplorer";

	@Override
	protected IAdaptable getInitialInput() {
		return BeansCorePlugin.getModel();
	}

	@Override
	public boolean show(ShowInContext context) {

		// First check the context's input object for a resource model element
		IResourceModelElement contextElement = BeansModelUtils
				.getResourceModelElement(context.getInput());
		if (contextElement instanceof IBeansProject) {
			if (showElement(contextElement)) {
				return true;
			}
		} else if (contextElement instanceof IBeansConfig) {

			// Now check the context's selection object
			ISelection selection = context.getSelection();
			if (selection != null && selection instanceof IStructuredSelection
					&& ((IStructuredSelection) selection).size() == 1) {
				Object sElement = ((IStructuredSelection) selection)
						.getFirstElement();
				if (sElement instanceof Element) {
					IModelElement element = BeansModelUtils.getModelElement(
							(Element) sElement, contextElement);
					if (element != null) {
						TreePath path = BeansUIUtils.createTreePath(element);
						if (showElement(path)) {
							return true;
						}
					}
				}
			}
			if (showElement(contextElement)) {
				return true;
			}
		}
		return false;
	}

	protected boolean showElement(Object element) {
		selectReveal(new StructuredSelection(element));
		return !getSite().getSelectionProvider().getSelection().isEmpty();
	}
}
