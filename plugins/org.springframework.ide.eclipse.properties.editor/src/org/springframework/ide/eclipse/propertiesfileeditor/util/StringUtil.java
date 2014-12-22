/*******************************************************************************
 * Copyright (c) 2014 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.propertiesfileeditor.util;

public class StringUtil {

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

}
