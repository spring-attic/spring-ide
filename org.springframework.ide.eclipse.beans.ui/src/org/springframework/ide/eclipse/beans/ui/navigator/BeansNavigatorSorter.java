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

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.core.model.ISourceModelElement;
import org.springframework.ide.eclipse.ui.SpringUIUtils;

/**
 * Sorter for Spring beans {@link ISourceModelElement}s. It keeps them in the
 * order found in the corresponding {@link IBeansConfig} file in case the common
 * navigator is not the spring explorer and sorting is not enabled.
 * <p>
 * Otherwise this sorter does a lexical sorting of the elements
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 */
public class BeansNavigatorSorter extends ViewerSorter {

	@Override
	public int category(Object element) {

		// Keep the config sets separate
		if (element instanceof IBeansConfigSet) {
			return 1;
		}
		return 0;
	}

	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		if (SpringUIUtils.isSortingEnabled()
				&& SpringUIUtils.isSpringExplorer(viewer)) {
			// add hack for beans config sorting
			if (e1 instanceof IFile && e2 instanceof IFile) {
				String f1 = getFileLabel((IFile) e1);
				String f2 = getFileLabel((IFile) e2);
				return super.compare(viewer, f1, f2);
			}
			else {
				return super.compare(viewer, e1, e2);
			}
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
