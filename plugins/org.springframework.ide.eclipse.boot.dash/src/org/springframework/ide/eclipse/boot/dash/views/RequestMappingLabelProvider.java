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
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.jface.viewers.ViewerCell;
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
	private RequestMappingsColumn column;

	public RequestMappingLabelProvider(Stylers stylers, BootDashElement bde, RequestMappingsColumn column) {
		this(stylers, LiveExpression.constant(bde), column);
	}

	public RequestMappingLabelProvider(Stylers stylers, LiveExpression<BootDashElement> bde, RequestMappingsColumn column) {
		this.bde = bde;
		this.column = column;
		this.stylers = stylers;
	}

	@Override
	public void update(ViewerCell cell) {
		Object o = cell.getElement();
		if (o instanceof String) {
			if (column==RequestMappingsColumn.SRC) {
				cell.setText((String) o);
				cell.setStyleRanges(null);
			} else {
				cell.setText("");
			}
		} else if (o instanceof RequestMapping) {
			StyledString styledText = getStyledText((RequestMapping)o);
			if (styledText!=null) {
				cell.setText(styledText.getString());
				cell.setStyleRanges(styledText.getStyleRanges());
			} else {
				cell.setText(""+cell.getElement());
				cell.setStyleRanges(null);
			}
		} else {
			cell.setText("");
			cell.setStyleRanges(null);
		}
	}

	protected StyledString getStyledText(RequestMapping rm) {
		Styler deemphasize = Stylers.NULL;
		if (!rm.isUserDefined()) {
			 deemphasize = stylers.grey();
		}
		switch (column) {
		case PATH:
			System.out.println(">>> styled path for "+rm);
			try {
				String path = rm.getPath();
				System.out.println("path = "+path);
				String defaultPath = getDefaultPath(bde.getValue());
				System.out.println("defaultPath = "+defaultPath);
				if (defaultPath.equals(path)) {
					System.out.println("BOLDED!");
					return new StyledString(path, stylers.bold());
				} else {
					System.out.println("styler = "+deemphasize);
					return new StyledString(path, deemphasize);
				}
			} finally {
				System.out.println("<<< styled path for "+rm);
			}
		case SRC:
			String m = rm.getMethod();
			if (m!=null) {
				return new StyledString(m, deemphasize);
			}
		default:
			break;
		}
		return new StyledString("???", deemphasize);
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

}
