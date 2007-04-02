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
package org.springframework.ide.eclipse.ui.internal.navigator;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.navigator.ICommonContentExtensionSite;
import org.eclipse.ui.navigator.ICommonContentProvider;
import org.springframework.ide.eclipse.core.SpringCoreUtils;
import org.springframework.ide.eclipse.core.model.IModelElement;

/**
 * This {@link ICommonContentProvider} knows about the Spring projects.
 * 
 * @author Torsten Juergeleit
 * @since 2.0
 */
public class SpringExplorerContentProvider implements ICommonContentProvider {

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	public void init(ICommonContentExtensionSite config) {
	}

	public Object[] getElements(Object inputElement) {
		return SpringCoreUtils.getSpringProjects().toArray();
	}

	public boolean hasChildren(Object element) {
		return false;
	}

	public Object[] getChildren(Object parentElement) {
		return IModelElement.NO_CHILDREN;
	}

	public Object getParent(Object element) {
		return null;
	}

	public void saveState(IMemento aMemento) {
	}

	public void restoreState(IMemento aMemento) {
	}

	public void dispose() {
	}
}
