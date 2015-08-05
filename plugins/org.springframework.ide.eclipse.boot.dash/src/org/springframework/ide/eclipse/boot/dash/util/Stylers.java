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

	/**
	 * The 'Stylers' requires baseFont to render styles using bold
	 * properly. If baseFont is null, then styler created will try to
	 * render things as well as it can, but it will not do 'bold'.
	 */
	public Stylers(Font baseFont) {
		this.baseFont = baseFont;
	}

	public Styler tag() {
		return new Styler() {
			@Override
			public void applyStyles(TextStyle textStyle) {
				textStyle.foreground = getSystemColor(SWT.COLOR_DARK_CYAN);
				//textStyle.rise = 2; //Why?? it makes mixed text with this style and others togther look really ugly!
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
		//If baseFont is null, this Stylers is a bit 'handicapped' and won't
		// be capable of doing 'bold' styling.
		if (boldFont==null && baseFont!=null) {
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

	public Styler darkGrey() {
		return color(SWT.COLOR_DARK_GRAY);
	}

	public Styler grey() {
		return color(SWT.COLOR_GRAY);
	}

	public Styler red() {
		return color(SWT.COLOR_RED);
	}

	/**
	 * Don't make this public. Instead define additional methods. That way it will be easier if we
	 * need to refactor specific color styles later to somehting user-defined.
	 */
	private Styler color(int colorCode) {
		final Color color = getSystemColor(colorCode);
		return new Styler() {
			public void applyStyles(TextStyle textStyle) {
				textStyle.foreground = color;
			}
		};
	}


}
