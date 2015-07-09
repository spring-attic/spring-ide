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
package org.springframework.ide.eclipse.boot.dash.views.sections;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.springframework.ide.eclipse.boot.dash.model.TagUtils;

/**
 * SWT/JFace dependent utility methods and constants
 *
 * @author Alex Boyko
 *
 */
public class UIUtils {

	public static final char[] PATH_CA_AUTO_ACTIVATION_CHARS = "/.ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".toCharArray();

	public static final char[] TAG_CA_AUTO_ACTIVATION_CHARS = "/,-.ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".toCharArray();

	public static final KeyStroke CTRL_SPACE = KeyStroke.getInstance(SWT.CTRL, SWT.SPACE);

	private static final String TAG_FONT_KEY = "tag";

	private static FontRegistry fontRegistry = null;

	private static FontRegistry getFontRegistry() {
		if (fontRegistry == null) {
			Display display = PlatformUI.getWorkbench().getDisplay();
			fontRegistry = new FontRegistry(display);
			FontData systemFontData = display.getSystemFont().getFontData()[0];
			fontRegistry.put(TAG_FONT_KEY, new FontData[] { new FontData(systemFontData.getName(), systemFontData.getHeight(), SWT.BOLD) });
		}
		return fontRegistry;
	}

	private static StyleRange createTagStyleRange(int start, int length) {
		StyleRange styleRange = new StyleRange();
		styleRange.start = start;
		styleRange.length = length;
		styleRange.underline = true;
		styleRange.rise = 2;
		styleRange.font = getFontRegistry().get(TAG_FONT_KEY);
		styleRange.foreground = PlatformUI.getWorkbench().getDisplay().getSystemColor(SWT.COLOR_DARK_CYAN);
		return styleRange;
	}

	/**
	 * Creates styles for tags in a string
	 * @param text tags in a string
	 * @return array of style ranges
	 */
	public static StyleRange[] getStyleRangesForTags(String text) {
		List<StyleRange> styleRanges = new ArrayList<StyleRange>();
		Matcher matcher = Pattern.compile(TagUtils.SEPARATOR_REGEX).matcher(text);
		int position = 0;
		while (matcher.find()) {
			if (position < matcher.start()) {
				styleRanges.add(createTagStyleRange(position, matcher.start() - position));
			}
			position = matcher.end();
		}
		if (position < text.length()) {
			styleRanges.add(createTagStyleRange(position, text.length() - position));
		}
		return styleRanges.toArray(new StyleRange[styleRanges.size()]);
	}

}
