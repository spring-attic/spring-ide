/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.views.sections;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.BootDashViewModel;
import org.springframework.ide.eclipse.boot.properties.editor.util.ArrayUtils;

/**
 * @author Kris De Volder
 */
public class BootDashTreeContentProvider implements ITreeContentProvider {

	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	@Override
	public Object[] getElements(Object e) {
		return getChildren(e);
	}

	@Override
	public Object[] getChildren(Object e) {
		if (e instanceof BootDashViewModel) {
			return ((BootDashViewModel) e).getSectionModels().getValue().toArray();
		} else if (e instanceof BootDashModel) {
			return ((BootDashModel) e).getElements().getValues().toArray();
		} else if (e instanceof BootDashElement) {
			return ((BootDashElement)e).getChildren().getValues().toArray();
		}
		return null;
	}

	@Override
	public Object getParent(Object e) {
		if (e instanceof BootDashElement) {
			return ((BootDashElement) e).getParent();
		} else if (e instanceof BootDashModel) {
			return ((BootDashModel) e).getViewModel();
		}
		return null;
	}

	@Override
	public boolean hasChildren(Object e) {
		return ArrayUtils.hasElements(getChildren(e));
	}

}
