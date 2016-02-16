/*******************************************************************************
 *  Copyright (c) 2012 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.wizard.ui;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.FilteredResourcesSelectionDialog;
import org.springframework.ide.eclipse.beans.core.BeansCoreUtils;

/**
 * @author Terry Denney
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
public class BeanFileSelectionDialog extends FilteredResourcesSelectionDialog {

	public BeanFileSelectionDialog(Shell shell, boolean multi,
			IContainer container, int typesMask) {
		super(shell, multi, container, typesMask);
	}
		
	@Override
	protected ItemsFilter createFilter() {
		return new BeanFileFilter();
	}
	
	private class BeanFileFilter extends ResourceFilter {
		
		@Override
		public boolean matchItem(Object item) {
			if (item instanceof IFile) {
				if (BeansCoreUtils.isBeansConfig((IResource) item)) {
					return super.matchItem(item);
				} else {
					return false;
				}
			}
			return super.matchItem(item);
		}

	}

}
