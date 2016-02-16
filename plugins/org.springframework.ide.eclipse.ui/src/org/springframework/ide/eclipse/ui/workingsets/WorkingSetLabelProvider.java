/*******************************************************************************
 * Copyright (c) 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.ui.workingsets;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * @author Christian Dupuis
 * @since 2.0
 */
public class WorkingSetLabelProvider implements ILabelProvider {

	private WorkbenchLabelProvider labelProvider = new WorkbenchLabelProvider();

	public Image getImage(Object element) {
		if (element instanceof IWorkingSet)
			return labelProvider.getImage(element);
		return null;
	}

	public String getText(Object element) {
		if (element instanceof IWorkingSet)
			return ((IWorkingSet) element).getLabel();
		return null;
	}

	public void addListener(ILabelProviderListener listener) {
	}
	
	public void dispose() {
	}

	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	public void removeListener(ILabelProviderListener listener) {
	}

}
