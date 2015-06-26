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

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.ui.viewsupport.AppearanceAwareLabelProvider;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.graphics.Image;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springframework.ide.eclipse.boot.dash.model.Taggable;
import org.springframework.ide.eclipse.boot.dash.views.sections.BootDashColumn;

@SuppressWarnings("restriction")
public class BootDashLabelProvider extends CellLabelProvider {
	
	/**
	 * String separator between tags string representation
	 */
	public static String TAGS_SEPARATOR = " ";

	private AppearanceAwareLabelProvider javaLabels = new AppearanceAwareLabelProvider();
	private BootDashColumn forColum;
	private RunStateImages runStateImages;

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
			//cell.setImage(getRunStateImage(e.getRunState()));
			cell.setText(e.getRunState().toString());
			break;
		case RUN_STATE_ICN:
			cell.setText("");
			cell.setImage(getRunStateImage(e.getRunState()));
			break;
		case TAGS:
			if (e instanceof Taggable) {
				cell.setText(StringUtils.join(((Taggable)e).getTags(), TAGS_SEPARATOR));
			} else {
				cell.setText("");
			}
			break;
		case LIVE_PORT:
			int port = e.getLivePort();
			cell.setText(port>=0?""+port:"?");
			break;
		default:
			cell.setText("???");
		}
	}

	private Image getRunStateImage(RunState runState) {
		if (runStateImages==null) {
			runStateImages = new RunStateImages();
		}
		return runStateImages.getImg(runState);
	}

	@Override
	public void dispose() {
		super.dispose();
		javaLabels.dispose();
		if (runStateImages!=null) {
			runStateImages.dispose();
			runStateImages = null;
		}
	}
}
