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
package org.springframework.ide.eclipse.beans.ui.navigator.internal;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.core.model.ISourceModelElement;

/**
 * Sorter for Spring beans {@link ISourceModelElement}s. It keeps them in the
 * order found in the corresponding {@link IBeansConfig} file.
 * 
 * @author Torsten Juergeleit
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
		if (e1 instanceof ISourceModelElement
				|| e2 instanceof ISourceModelElement) {

			// We don't want to sort it, just show it in the order it's found
			// in the file
			return 0;
		}
		return super.compare(viewer, e1, e2);
	}
}
