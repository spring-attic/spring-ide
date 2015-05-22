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
package org.springframework.ide.eclipse.boot.dash.views;

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.ui.viewsupport.AppearanceAwareLabelProvider;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.RunState;

@SuppressWarnings("restriction")
public class BootDashLabelProvider extends CellLabelProvider {

	AppearanceAwareLabelProvider javaLabels = new AppearanceAwareLabelProvider();

	public static enum BootDashColumn {
		PROJECT, RUN_STATE
	}

	private BootDashColumn forColum;

	public BootDashLabelProvider(BootDashColumn target) {
		this.forColum = target;
	}

	@Override
	public void update(ViewerCell cell) {
		Object e = cell.getElement();
		switch (forColum) {
		case PROJECT:
			IJavaProject jp = getJavaProject(e);
			if (jp!=null) {
				cell.setText(javaLabels.getText(jp));
				cell.setImage(javaLabels.getImage(jp));
			} else {
				cell.setText(""+e);
			}
			break;
		case RUN_STATE:
			cell.setText(""+getRunState(e));
			break;
		}
	}

	private RunState getRunState(Object e) {
		if (e instanceof BootDashElement) {
			return ((BootDashElement) e).getRunState();
		}
		return null;
	}

	private IJavaProject getJavaProject(Object e) {
		if (e instanceof BootDashElement) {
			return ((BootDashElement) e).getJavaProject();
		}
		return null;
	}

}
