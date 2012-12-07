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

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.zest.core.viewers.IGraphEntityContentProvider;
import org.eclipse.zest.core.viewers.INestedContentProvider;
import org.springframework.ide.eclipse.beans.ui.livegraph.model.LiveBean;
import org.springframework.ide.eclipse.beans.ui.livegraph.model.LiveBeansContext;
import org.springframework.ide.eclipse.beans.ui.livegraph.model.LiveBeansGroup;
import org.springframework.ide.eclipse.beans.ui.livegraph.model.LiveBeansModel;

/**
 * A content provider for the Live Beans Graph
 * 
 * @author Leo Dos Santos
 */
public class LiveBeansGraphContentProvider implements IGraphEntityContentProvider, INestedContentProvider {

	public void dispose() {
		// TODO Auto-generated method stub

	}

	public Object[] getChildren(Object element) {
		if (element instanceof LiveBeansGroup) {
			LiveBeansGroup group = (LiveBeansGroup) element;
			return group.getBeans().toArray();
		}
		return null;
	}

	public Object[] getConnectedTo(Object entity) {
		if (entity instanceof LiveBean) {
			LiveBean bean = (LiveBean) entity;
			return bean.getDependencies().toArray();
		}
		else if (entity instanceof LiveBeansContext) {
			LiveBeansContext context = (LiveBeansContext) entity;
			return new Object[] { context.getParent() };
		}
		return null;
	}

	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof LiveBeansModel) {
			LiveBeansModel model = (LiveBeansModel) inputElement;
			// return model.getBeansByContext().toArray();
			// return model.getBeansByResource().toArray();
			return model.getBeans().toArray();
		}
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
