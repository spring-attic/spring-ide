/*
 * Copyright 2002-2006 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 

package org.springframework.ide.eclipse.core;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public final class PatternUtils {

	private PatternUtils() {
		// Not for instantiation
	}

	/**
	 * Creates a pattern element from the pattern string which is either a
	 * reg-ex expression or of wildcard format ('*' matches any character and
	 * '?' matches one character).
	 * @param pattern  the search pattern
	 * @param isCaseSensitive set to <code>true</code> to create a case
	 * 				insensitve pattern
	 * @param isRegexSearch <code>true</code> if the passed string is a reg-ex
	 * 				pattern
	 * @throws PatternSyntaxException
	 */
	public static Pattern createPattern(String pattern,
					boolean isCaseSensitive, boolean isRegexSearch)
					throws PatternSyntaxException {
		if (!isRegexSearch) {
			pattern = toRegExFormat(pattern);
		}
		if (!isCaseSensitive) {
			return Pattern.compile(pattern, Pattern.CASE_INSENSITIVE |
									 Pattern.UNICODE_CASE | Pattern.MULTILINE);
		}
		return Pattern.compile(pattern, Pattern.MULTILINE);
	}

	/**
	 * Converts wildcard format ('*' and '?') to reg-ex format.
	 */
	private static String toRegExFormat(String pattern) {
		StringBuffer regex = new StringBuffer(pattern.length());
		boolean escaped = false;
		boolean quoting = false;
		for (int i = 0; i < pattern.length(); i++) {
			char c = pattern.charAt(i);
			if (c == '*' && !escaped) {
				if (quoting) {
					regex.append("\\E");
					quoting = false;
				}
				regex.append(".*");
				escaped = false;
				continue;
			} else if (c == '?' && !escaped) {
				if (quoting) {
					regex.append("\\E");
					quoting = false;
				}
				regex.append(".");
				escaped = false;
				continue;
			} else if (c == '\\' && !escaped) {
				escaped = true;
				continue;								
			} else if (c == '\\' && escaped) {
				escaped = false;
				if (quoting) {
					regex.append("\\E");
					quoting = false;
				}
				regex.append("\\\\");
				continue;								
			}
			if (!quoting) {
				regex.append("\\Q");
				quoting = true;
			}
			if (escaped && c != '*' && c != '?' && c != '\\') {
				regex.append('\\');
			}
			regex.append(c);
			escaped = c == '\\';
		}
		if (quoting) {
			regex.append("\\E");
		}
		return regex.toString();
	}
}
