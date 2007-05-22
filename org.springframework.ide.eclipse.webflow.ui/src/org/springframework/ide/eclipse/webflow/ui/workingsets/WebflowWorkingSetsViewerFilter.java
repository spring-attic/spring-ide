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
package org.springframework.ide.eclipse.webflow.ui.workingsets;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.IWorkingSet;
import org.springframework.ide.eclipse.ui.workingsets.IWorkingSetsFilter;
import org.springframework.ide.eclipse.webflow.core.internal.model.WebflowModelUtils;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;

/**
 * {@link IWorkingSetsFilter} that filters {@link IWebflowModelElement} that are
 * not part of the {@link IWorkingSet}.
 * @author Christian Dupuis
 * @since 2.0
 */
public class WebflowWorkingSetsViewerFilter implements IWorkingSetsFilter {

	public boolean isInWorkingSet(IAdaptable[] elements, Object parentElement,
			Object element) {
		if (element instanceof IResource
				&& WebflowModelUtils.isWebflowConfig((IResource) element)) {
			for (IAdaptable adaptable : elements) {
				IResource resource = (IResource) adaptable
						.getAdapter(IResource.class);
				if (resource != null && resource.equals(element)) {
					return true;
				}
			}
			return false;
		}
		// let pass all other elements
		return true;
	}

}
