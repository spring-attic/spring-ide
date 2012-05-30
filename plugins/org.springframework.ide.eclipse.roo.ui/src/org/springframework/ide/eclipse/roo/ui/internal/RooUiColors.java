/*******************************************************************************
 *  Copyright (c) 2012 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.roo.ui.internal;

import java.util.logging.Level;

import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

/**
 * @author Leo Dos Santos
 */
public class RooUiColors {

	public static final String COLOR_BACKGROUND_SHELL = "com.springsource.sts.roo.ui.color.shellBackground";

	public static final String COLOR_TEXT_ERROR = "com.springsource.sts.roo.ui.color.errorText";

	public static final String COLOR_TEXT_INFO = "com.springsource.sts.roo.ui.color.infoText";

	public static final String COLOR_TEXT_INPUT = "com.springsource.sts.roo.ui.color.inputText";

	public static final String COLOR_TEXT_PROMPT = "com.springsource.sts.roo.ui.color.promptText";

	public static final String COLOR_TEXT_WARNING = "com.springsource.sts.roo.ui.color.warningText";

	public static final String FONT_SHELL = "com.springsource.sts.roo.ui.font.shellFont";

	public static void applyShellBackground(StyledText text) {
		if (getColorRegistry() != null && text != null && !text.isDisposed()) {
			text.setBackground(getColorRegistry().get(COLOR_BACKGROUND_SHELL));
		}
	}

	public static void applyShellFont(StyledText text) {
		if (getFontRegistry() != null && text != null && !text.isDisposed()) {
			text.setFont(getFontRegistry().get(FONT_SHELL));
		}
	}

	public static void applyShellForeground(StyledText text) {
		if (getColorRegistry() != null && text != null && !text.isDisposed()) {
			text.setForeground(getColorRegistry().get(COLOR_TEXT_INPUT));
		}
	}

	public static Color getColor(int severity) {
		ColorRegistry colors = getColorRegistry();
		if (colors != null) {
			if (severity >= Level.SEVERE.intValue()) {
				return colors.get(COLOR_TEXT_ERROR);
			}
			else if (severity >= Level.WARNING.intValue()) {
				return colors.get(COLOR_TEXT_WARNING);
			}
			else if (severity >= Level.INFO.intValue()) {
				return colors.get(COLOR_TEXT_INFO);
			}
			return colors.get(COLOR_TEXT_INPUT);
		}
		else {
			// legacy fallback
			if (severity >= Level.SEVERE.intValue()) {
				return Display.getDefault().getSystemColor(SWT.COLOR_RED);
			}
			else if (severity >= Level.WARNING.intValue()) {
				return Display.getDefault().getSystemColor(SWT.COLOR_DARK_MAGENTA);
			}
			else if (severity >= Level.INFO.intValue()) {
				return Display.getDefault().getSystemColor(SWT.COLOR_DARK_GREEN);
			}
			return Display.getDefault().getSystemColor(SWT.COLOR_BLACK);
		}
	}

	public static ColorRegistry getColorRegistry() {
		if (PlatformUI.getWorkbench() != null) {
			return PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getColorRegistry();
		}
		return null;
	}

	public static FontRegistry getFontRegistry() {
		if (PlatformUI.getWorkbench() != null) {
			return PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getFontRegistry();
		}
		return null;
	}

	public static Color getPromptColor() {
		if (getColorRegistry() != null) {
			return getColorRegistry().get(COLOR_TEXT_PROMPT);
		}
		return Display.getDefault().getSystemColor(SWT.COLOR_DARK_RED);
	}

}
