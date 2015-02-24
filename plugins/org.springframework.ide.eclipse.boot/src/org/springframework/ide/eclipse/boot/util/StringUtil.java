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
package org.springframework.ide.eclipse.boot.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {

	public static boolean hasText(String name) {
		return name!=null && !name.trim().equals("");
	}

	public static String trim(String s) {
		if (s!=null) {
			return s.trim();
		}
		return null;
	}

	public static int commonPrefixLength(String s, String t) {
		int shortestStringLen = Math.min(s.length(), t.length());
		for (int i = 0; i < shortestStringLen; i++) {
			if (s.charAt(i)!=t.charAt(i)) {
				return i;
			}
		}
		//no difference found upto entire length of shortest string.
		return shortestStringLen;
	}

	/**
	 * @return longest string which is a prefix of both argument Strings.
	 */
	public static String commonPrefix(String s, String t) {
		int len = commonPrefixLength(s, t);
		if (len>0) {
			if (s.length()==len) {
				return s; //Avoid needlessly copying s
			}
			if (t.length()==len) {
				return t; //Avoid needlessly copying s
			}
			return s.substring(0,len);
		}
		return "";
	}

	public static String camelCaseToHyphens(String value) {
		Matcher matcher = CAMEL_CASE_PATTERN.matcher(value);
		StringBuffer result = new StringBuffer();
		while (matcher.find()) {
			matcher.appendReplacement(result, matcher.group(1) + '-'
					+ matcher.group(2).toLowerCase());
		}
		matcher.appendTail(result);
		return result.toString();
	}

	private static final Pattern CAMEL_CASE_PATTERN = Pattern.compile("([^A-Z-])([A-Z])");

	public static String arrayToCommaDelimitedString(Object[] array) {
		return collectionToCommaDelimitedString(Arrays.asList(array));
	}

	public static String collectionToCommaDelimitedString(Collection<?> items) {
		StringBuilder buf = new StringBuilder();
		boolean first = true;
		for (Object item : items) {
			if (!first) {
				buf.append(",");
			}
			buf.append(item);
			first = false;
		}
		return buf.toString();
	}

	public static String upperCaseToHyphens(String v) {
		if (v!=null) {
			return v.toLowerCase().replace('_', '-');
		}
		return null;
	}

}
