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
package org.springframework.ide.eclipse.webflow.ui.navigator;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowConfig;

/**
 * {@link ViewerSorter} that sorts the tree elements depending on element's
 * resources and their line location in the resource.
 * @author Christian Dupuis
 * @since 2.0
 */
public class WebflowModelNavigatorSorter extends ViewerSorter {

	@Override
	public int category(Object element) {
		if (element instanceof IWebflowConfig) {
			return 2;
		}
		return 0;
	}

	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		if (e1 instanceof IWebflowConfig && e2 instanceof IWebflowConfig) {
			IWebflowConfig ref1 = (IWebflowConfig) e1;
			IWebflowConfig ref2 = (IWebflowConfig) e2;
			super.compare(viewer, ref1.getResource(), ref2.getResource());
		}
		return super.compare(viewer, e1, e2);
	}
}
