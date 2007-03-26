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
