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

import static org.springframework.ide.eclipse.boot.dash.views.sections.BootDashColumn.APP;
import static org.springframework.ide.eclipse.boot.dash.views.sections.BootDashColumn.RUN_STATE_ICN;

import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.graphics.Image;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.util.ColumnViewerAnimator;
import org.springframework.ide.eclipse.boot.dash.util.Stylers;
import org.springframework.ide.eclipse.boot.dash.views.sections.BootDashColumn;

public class BootDashCellLabelProvider extends StyledCellLabelProvider {

	protected final BootDashColumn forColum;
	private Stylers stylers;
	private ColumnViewerAnimator animator;

	private ColumnViewer tv;
	private BootDashElementLabelProvider bdeLabels;

	public BootDashCellLabelProvider(ColumnViewer tv, BootDashColumn target, Stylers stylers) {
		this.tv = tv;
		this.stylers = stylers;
		this.bdeLabels = new BootDashElementLabelProvider();
		this.forColum = target;
	}

	@Override
	public void update(ViewerCell cell) {
		Object e = cell.getElement();
		BootDashElement bde = null;
		if (e instanceof BootDashElement) {
			bde = (BootDashElement) cell.getElement();
		}
		switch (forColum) {
		case TREE_VIEWER_MAIN:
			if (bde!=null) {
				cell.setText(bdeLabels.getText(bde, APP));
				animate(cell, bdeLabels.getImageAnimation(bde, RUN_STATE_ICN));
			}
			break;
		case PROJECT:
			cell.setText(bdeLabels.getText(bde, forColum));
//			cell.setImage(bdeLabels.getImage(e, forColum));
			break;
		case HOST:
			cell.setText(bdeLabels.getText(bde, forColum));
			break;
		case APP:
			cell.setText(bdeLabels.getText(bde, forColum));
			break;
//		case RUN_TARGET:
//			cell.setText(e.getTarget().getName());
//			break;
		case RUN_STATE_ICN:
			cell.setText("");
			animate(cell, bdeLabels.getImageAnimation(bde, forColum));
			break;
		case TAGS:
			StyledString styled = bdeLabels.getStyledText(bde, forColum, stylers);
			cell.setText(styled.getString());
			cell.setStyleRanges(styled.getStyleRanges());
			break;
		case LIVE_PORT:
			cell.setText(bdeLabels.getText(bde, forColum));
			break;
		case DEFAULT_PATH:
			cell.setText(bdeLabels.getText(bde, forColum));
			break;
		case INSTANCES:
			cell.setText(bdeLabels.getText(bde, forColum));
			break;
		default:
			cell.setText(bdeLabels.getText(bde, forColum));
		}
	}

	private void animate(ViewerCell cell, Image[] images) {
		if (animator==null) {
			animator = new ColumnViewerAnimator(tv);
		}
		animator.setAnimation(cell, images);
	}

	@Override
	public void dispose() {
		super.dispose();
		bdeLabels.dispose();
		if (animator!=null) {
			animator.dispose();
			animator = null;
		}
	}
}
