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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.SWT;
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

	/**
	 * Creates styled string applying tagStyle at appropriate locations in a raw tags string.
	 */
	public static StyledString applyTagStyles(String text, Styler tagStyler) {
		StyledString styledString = new StyledString(text);
		Matcher matcher = Pattern.compile(TagUtils.SEPARATOR_REGEX).matcher(text);
		int position = 0;
		while (matcher.find()) {
			if (position < matcher.start()) {
				styledString.setStyle(position, matcher.start() - position, tagStyler);
			}
			position = matcher.end();
		}
		if (position < text.length()) {
			styledString.setStyle(position, text.length() - position, tagStyler);
		}
		return styledString;
	}

}
