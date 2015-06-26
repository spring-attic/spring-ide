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
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.graphics.Font;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.requestmappings.RequestMapping;
import org.springframework.ide.eclipse.boot.dash.util.Stylers;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;

/**
 * @author Kris De Volder
 */
public class RequestMappingLabelProvider extends StyledCellLabelProvider {

	private LiveExpression<BootDashElement> bde;
	private Stylers stylers;

	public RequestMappingLabelProvider(Font baseFont, LiveExpression<BootDashElement> bde) {
		this.bde = bde;
		this.stylers = new Stylers(baseFont);
	}

	@Override
	public void update(ViewerCell cell) {
		StyledString styledText = getStyledText(cell);
		if (styledText!=null) {
			cell.setText(styledText.getString());
			cell.setStyleRanges(styledText.getStyleRanges());
		} else {
			cell.setText(""+cell.getElement());
			cell.setStyleRanges(null);
		}
	}

	protected StyledString getStyledText(ViewerCell cell) {
		Object o = cell.getElement();
		if (o instanceof RequestMapping) {
			RequestMapping rm = (RequestMapping) o;
			String path = rm.getPath();
			String defaultPath = getDefaultPath(bde.getValue());
			if (defaultPath.equals(path)) {
				return new StyledString(path, stylers.bold());
			} else {
				return new StyledString(path);
			}
		}
		return null;
	}

	private String getDefaultPath(BootDashElement value) {
		if (value!=null) {
			String path = value.getDefaultRequestMappingPath();
			if (path!=null) {
				return path;
			}
		}
		return "";
	}

	@Override
	public void dispose() {
		if (stylers!=null) {
			stylers.dispose();
			stylers = null;
		}
		super.dispose();
	}

}
