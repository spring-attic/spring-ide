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

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.ui.viewsupport.AppearanceAwareLabelProvider;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;

@SuppressWarnings("restriction")
public class BootDashLabelProvider extends CellLabelProvider {

	private AppearanceAwareLabelProvider javaLabels = new AppearanceAwareLabelProvider();
	private BootDashColumn forColum;

	public BootDashLabelProvider(BootDashColumn target) {
		this.forColum = target;
	}

	@Override
	public void update(ViewerCell cell) {
		BootDashElement e = (BootDashElement) cell.getElement();
		switch (forColum) {
		case PROJECT:
			IJavaProject jp = e.getJavaProject();
			if (jp!=null) {
				cell.setText(javaLabels.getText(jp));
				cell.setImage(javaLabels.getImage(jp));
			} else {
				cell.setText(""+e);
			}
			break;
		case RUN_TARGET:
			cell.setText(e.getTarget().getName());
			break;
		case RUN_STATE:
			cell.setText(e.getRunState().toString());
			break;
		default:
			cell.setText("???");
		}
	}

	@Override
	public void dispose() {
		super.dispose();
		javaLabels.dispose();
	}
}
