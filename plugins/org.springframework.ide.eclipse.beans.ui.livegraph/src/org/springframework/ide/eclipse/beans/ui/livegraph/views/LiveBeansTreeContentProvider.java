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
package org.springframework.ide.eclipse.beans.ui.livegraph.views;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.springframework.ide.eclipse.beans.ui.livegraph.model.LiveBeansGroup;
import org.springframework.ide.eclipse.beans.ui.livegraph.model.LiveBeansModel;

/**
 * A content provider for the Live Beans tree display
 * 
 * @author Leo Dos Santos
 */
public class LiveBeansTreeContentProvider implements ITreeContentProvider {

	public void dispose() {
		// TODO Auto-generated method stub

	}

	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof LiveBeansGroup) {
			LiveBeansGroup group = (LiveBeansGroup) parentElement;
			return group.getBeans().toArray();
		}
		return null;
	}

	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof LiveBeansModel) {
			LiveBeansModel model = (LiveBeansModel) inputElement;
			return model.getBeansByContext().toArray();
			// return model.getBeansByResource().toArray();
		}
		return null;
	}

	public Object getParent(Object element) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean hasChildren(Object element) {
		if (element instanceof LiveBeansGroup) {
			return true;
		}
		return false;
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// TODO Auto-generated method stub

	}

}
