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

import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.graphics.Image;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.util.Stylers;
import org.springframework.ide.eclipse.boot.dash.util.TableViewerAnimator;
import org.springframework.ide.eclipse.boot.dash.views.sections.BootDashColumn;

public class BootDashCellLabelProvider extends StyledCellLabelProvider {

	private BootDashElementLabelProvider bdeLabels = new BootDashElementLabelProvider();
	protected final BootDashColumn forColum;
	private Stylers stylers;
	private TableViewerAnimator animator;

	private TableViewer tv;

	public BootDashCellLabelProvider(TableViewer tv, BootDashColumn target, Stylers stylers) {
		this.tv = tv;
		this.stylers = stylers;
		this.forColum = target;
	}

	@Override
	public void update(ViewerCell cell) {
		BootDashElement e = (BootDashElement) cell.getElement();
		switch (forColum) {
		case PROJECT:
			cell.setText(bdeLabels.getText(e, forColum));
			cell.setImage(bdeLabels.getImage(e, forColum));
			break;
		case HOST:
			cell.setText(bdeLabels.getText(e, forColum));
			break;
		case APP:
			cell.setText(bdeLabels.getText(e, forColum));
			break;
//		case RUN_TARGET:
//			cell.setText(e.getTarget().getName());
//			break;
		case RUN_STATE_ICN:
			cell.setText("");
			animate(cell, bdeLabels.getImageAnimation(e, forColum));
			break;
		case TAGS:
			StyledString styled = bdeLabels.getStyledText(e, forColum, stylers);
			cell.setText(styled.getString());
			cell.setStyleRanges(styled.getStyleRanges());
			break;
		case LIVE_PORT:
			cell.setText(bdeLabels.getText(e, forColum));
			break;
		case DEFAULT_PATH:
			cell.setText(bdeLabels.getText(e, forColum));
			break;
		case INSTANCES:
			cell.setText(bdeLabels.getText(e, forColum));
			break;
		default:
			cell.setText(bdeLabels.getText(e, forColum));
		}
	}

	private void animate(ViewerCell cell, Image[] images) {
		if (animator==null) {
			animator = new TableViewerAnimator(tv);
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
