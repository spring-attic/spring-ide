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
package org.springframework.ide.eclipse.boot.dash.util;

import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Display;
import org.springsource.ide.eclipse.commons.livexp.ui.Disposable;

public class Stylers implements Disposable {

	public static final Styler NULL = new Styler() {
		public void applyStyles(TextStyle textStyle) {
		}
		public String toString() {
			return "Styler.NULL";
		};
	};
	private Font baseFont; // borrowed
	private Font boldFont = null; //owned (must dispose!)

	public Stylers(Font baseFont) {
		this.baseFont = baseFont;
	}

	public Styler tag() {
		return new Styler() {
			@Override
			public void applyStyles(TextStyle textStyle) {
				textStyle.foreground = getSystemColor(SWT.COLOR_DARK_CYAN);
				textStyle.rise = 2;
				textStyle.underline = true;
				textStyle.font = getBoldFont();
			}
		};
	}

	private Color getSystemColor(int colorCode) {
		return Display.getDefault().getSystemColor(colorCode);
	}

	public Styler bold() {
		return new Styler() {
			@Override
			public void applyStyles(TextStyle textStyle) {
				textStyle.font = getBoldFont();
			}
		};
	}

	private synchronized Font getBoldFont() {
		if (boldFont==null) {
			FontData[] data= baseFont.getFontData();
			for (int i= 0; i < data.length; i++) {
				data[i].setStyle(SWT.BOLD);
			}
			boldFont = new Font(baseFont.getDevice(), data);
		}
		return boldFont;
	}

	@Override
	public void dispose() {
		if (boldFont!=null) {
			boldFont.dispose();
			boldFont = null;
		}
	}

	public Styler grey() {
		return color(SWT.COLOR_GRAY);
	}

	private Styler color(int colorCode) {
		final Color color = getSystemColor(colorCode);
		return new Styler() {
			public void applyStyles(TextStyle textStyle) {
				textStyle.foreground = color;
			}
		};
	}

}
