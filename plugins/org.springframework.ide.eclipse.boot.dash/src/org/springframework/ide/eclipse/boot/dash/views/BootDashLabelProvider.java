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
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.graphics.Image;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springframework.ide.eclipse.boot.dash.model.TagUtils;
import org.springframework.ide.eclipse.boot.dash.model.Taggable;
import org.springframework.ide.eclipse.boot.dash.util.Stylers;
import org.springframework.ide.eclipse.boot.dash.views.sections.BootDashColumn;
import org.springframework.ide.eclipse.boot.dash.views.sections.UIUtils;

@SuppressWarnings("restriction")
public class BootDashLabelProvider extends StyledCellLabelProvider {

	private AppearanceAwareLabelProvider javaLabels = new AppearanceAwareLabelProvider();
	protected final BootDashColumn forColum;
	private RunStateImages runStateImages;
	private Stylers stylers;

	public BootDashLabelProvider(BootDashColumn target, Stylers stylers) {
		this.stylers = stylers;
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
		case HOST:
			{
				String name = e.getLiveHost();
				if (name != null) {
					cell.setText(name);
				} else {
					cell.setText("???");
				}
			}
			break;
		case APP:
			{
				String name = e.getName();
				if (name != null) {
					cell.setText(name);
				} else {
					cell.setText("???");
				}
			}
			break;
//		case RUN_TARGET:
//			cell.setText(e.getTarget().getName());
//			break;
		case RUN_STATE_ICN:
			cell.setText("");
			cell.setImage(getRunStateImage(e.getRunState()));
			break;
		case TAGS:
			String txt;
			if (e instanceof Taggable) {
				txt = TagUtils.toString(((Taggable)e).getTags());
			} else {
				txt = "";
				cell.setText("");
			}
			StyledString styled = UIUtils.getStyleRangesForTags(txt, stylers.tag());
			cell.setText(styled.getString());
			cell.setStyleRanges(styled.getStyleRanges());
			break;
		case LIVE_PORT:
			int port = e.getLivePort();
			cell.setText(port>=0?""+port:"?");
			break;
		case DEFAULT_PATH:
			String path = e.getDefaultRequestMappingPath();
			cell.setText(path==null?"":path);
			break;
		case INSTANCES:
			int actual = e.getActualInstances();
			int desired = e.getDesiredInstances();
			cell.setText(actual+"/"+desired);
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
