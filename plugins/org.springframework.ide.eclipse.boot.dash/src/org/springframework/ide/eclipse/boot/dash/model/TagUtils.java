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
package org.springframework.ide.eclipse.boot.dash.model;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.lang.StringUtils;

/**
 * Utilities for generating tags array from text, generating string from tags array and others
 * 
 * @author Alex Boyko
 *
 */
public class TagUtils {
	
	/**
	 * String separator between tags string representation
	 */
	public static final String SEPARATOR = ", ";
	
	/**
	 * Regular Expression pattern for separation string between tags in their textual representation
	 */
	public static final String SEPARATOR_REGEX = "\\s*,\\s*";
	
	/**
	 * Parses text into tags
	 * 
	 * @param text the string text
	 * @return array of string tags
	 */
	public static String[] parseTags(String text) {
		String s = text.trim();
		if (s.isEmpty()) {
			return new String[0];
		} else {
			String[] split = s.split(SEPARATOR_REGEX);
			if (split.length > 0) {
				ArrayList<String> sanitized = new ArrayList<String>(split.length);
				for (String tag : split) {
					if (!tag.isEmpty()) {
						sanitized.add(tag);
					}
				}
				split = sanitized.toArray(new String[sanitized.size()]);
			}
			return split;
		}

	}
	
	/**
	 * Generates string representation for tags
	 * 
	 * @param tags the tags
	 * @return the string representation of the tags
	 */
	public static String toString(Collection<String> tags) {
		return StringUtils.join(tags, SEPARATOR);
	}
	
	/**
	 * Generates string representation for tags
	 * 
	 * @param tags the tags
	 * @return the string representation of the tags
	 */
	public static String toString(String[] tags) {
		return StringUtils.join(tags, SEPARATOR);
	}

}
