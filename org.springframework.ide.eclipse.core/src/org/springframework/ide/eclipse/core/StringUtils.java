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

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Miscellaneous string utility methods.
 */
public final class StringUtils {

	public static final char SINGLE_QUOTE = '\'';
	public static final char DOUBLE_QUOTE = '\"';

    /**
     * Returns concatenated text from given two texts delimited by given
     * delimiter. Both texts can be empty or <code>null</code>.
     */
    public static String concatenate(String text1, String text2,
    								 String delimiter) {
        StringBuffer buf = new StringBuffer();
        if (text1 != null && text1.length() > 0) {
            buf.append(text1);
        }
        if (text2 != null && text2.length() > 0) {
            if (buf.length() > 0) {
                buf.append(delimiter);
            }
            buf.append(text2);
        }
        return buf.toString();
    }

	/**
	 * Convenience method to return a <code>Collection</code> as a delimited
	 * (e.g. CSV) <code>String</code>.
	 * @param coll  <code>Collection</code> to display
	 * @param delim  delimiter to use (probably a ",")
	 */
	public static String collectionToDelimitedString(Collection coll,
													 String delim) {
		if (coll == null) {
			return "";
		}
		StringBuffer sb = new StringBuffer();
		Iterator it = coll.iterator();
		int i = 0;
		while (it.hasNext()) {
			if (i > 0) {
				sb.append(delim);
			}
			sb.append(it.next());
			i++;
		}
		return sb.toString();
	}

	/**
	 * Returns <code>true</code> if given string has a leading and trailing
	 * single or double quote character.
	 */
	public static boolean isQuoted(String string) {
		if (string == null || string.length() < 2) {
			return false;
		}

		int lastIndex = string.length() - 1;
		char firstChar = string.charAt(0);
		char lastChar = string.charAt(lastIndex);

		return ((firstChar == SINGLE_QUOTE && lastChar == SINGLE_QUOTE) ||
				(firstChar == DOUBLE_QUOTE && lastChar == DOUBLE_QUOTE));
	}

	/**
	 * Returns <code>true</code> if given string's first character is upper
	 * case as per {@link Character#isUpperCase(char)}.
	 */
    public static boolean isCapitalized(String string) {
		if (string != null && string.length() > 0) {
			return !Character.isUpperCase(string.charAt(0));
		}
		return false;
	}

	/**
	 * Capitalize a <code>String</code>, changing the first letter to upper
	 * case as per {@link Character#toUpperCase(char)}. No other letters are
	 * changed.
	 * @param str the String to capitalize, may be null
	 * @return the capitalized String, <code>null</code> if null
	 */
	public static String capitalize(String str) {
		return changeFirstCharacterCase(str, true);
	}

	/**
	 * Uncapitalize a <code>String</code>, changing the first letter to
	 * lower case as per {@link Character#toLowerCase(char)}.
	 * No other letters are changed.
	 * @param str the String to uncapitalize, may be null
	 * @return the uncapitalized String, <code>null</code> if null
	 */
	public static String uncapitalize(String str) {
		return changeFirstCharacterCase(str, false);
	}

	private static String changeFirstCharacterCase(String str, boolean capitalize) {
		if (str == null || str.length() == 0) {
			return str;
		}
		StringBuffer buf = new StringBuffer(str.length());
		if (capitalize) {
			buf.append(Character.toUpperCase(str.charAt(0)));
		}
		else {
			buf.append(Character.toLowerCase(str.charAt(0)));
		}
		buf.append(str.substring(1));
		return buf.toString();
	}

	public static boolean isAlphaNumeric(String str) {
		int strLen;
		if (str == null || (strLen = str.length()) == 0) {
			return false;
		}
		for (int i = 0; i < strLen; i++) {
			char c = str.charAt(i);
			if (!Character.isLetterOrDigit(c) && c != ' ') {
				return false;
			}
		}
		return true;
	}

	/**
	 * Convenience method to convert a CSV string list to a set.
	 * Note that this will suppress duplicates.
	 * @param str CSV String
	 * @return a Set of String entries in the list
	 */
	public static Set<String> commaDelimitedListToSet(String str) {
		Set<String> set = new LinkedHashSet<String>();
		String[] tokens = org.springframework.util.StringUtils
				.commaDelimitedListToStringArray(str);
		for (int i = 0; i < tokens.length; i++) {
			set.add(tokens[i]);
		}
		return set;
	}
}
