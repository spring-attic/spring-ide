/*******************************************************************************
 * Copyright (c) 2012, 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.live.tree;

import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.springframework.ide.eclipse.beans.ui.live.model.LiveBean;
import org.springframework.ide.eclipse.beans.ui.live.model.LiveBeanRelation;
import org.springframework.ide.eclipse.beans.ui.live.model.LiveBeansGroup;

/**
 * A content provider for the Live Beans tree display
 * 
 * @author Leo Dos Santos
 * @author Alex Boyko
 */
public abstract class AbstractLiveBeansTreeContentProvider implements ITreeContentProvider {

	public void dispose() {

	}

	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof LiveBeansGroup) {
			LiveBeansGroup group = (LiveBeansGroup) parentElement;
			return group.getBeans().toArray();
		}
		else if (parentElement instanceof LiveBean) {
			Set<LiveBeanRelation> children = new LinkedHashSet<LiveBeanRelation>();
			LiveBean bean = (LiveBean) parentElement;
			Set<LiveBean> dependencies = bean.getDependencies();
			for (LiveBean child : dependencies) {
				children.add(new LiveBeanRelation(child, true));
			}
			Set<LiveBean> injectInto = bean.getInjectedInto();
			for (LiveBean child : injectInto) {
				children.add(new LiveBeanRelation(child));
			}
			return children.toArray();
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
		else if (element instanceof LiveBean) {
			LiveBean bean = (LiveBean) element;
			return !bean.getDependencies().isEmpty() || !bean.getInjectedInto().isEmpty();
		}
		return false;
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// TODO Auto-generated method stub

	}

}
