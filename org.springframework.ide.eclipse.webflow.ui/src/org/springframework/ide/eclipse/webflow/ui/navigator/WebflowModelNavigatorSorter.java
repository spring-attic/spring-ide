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

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.springframework.ide.eclipse.ui.SpringUIUtils;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowConfig;

/**
 * {@link ViewerSorter} that sorts the tree elements depending on element's
 * resources and their line location in the resource.
 * <p>
 * If the underlying common navigator is the spring explorer and sorting is
 * enabled, this sorter does a lexical sorting of the elements
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
		if (SpringUIUtils.isSortingEnabled()
				&& SpringUIUtils.isSpringExplorer(viewer)) {
			// hack for sorting web flow configs
			if (e1 instanceof IWebflowConfig && e2 instanceof IWebflowConfig) {
				String f1 = getFileLabel(((IWebflowConfig) e1).getResource());
				String f2 = getFileLabel(((IWebflowConfig) e2).getResource());
				return super.compare(viewer, f1, f2);
			}
			return super.compare(viewer, e1, e2);
		}
		// We don't want to sort it, just show it in the order it's found
		// in the file
		return 0;
	}

	private String getFileLabel(IFile file) {
		return file.getName()
				+ " - "
				+ file.getProjectRelativePath().removeLastSegments(1)
						.toString();
	}
}
