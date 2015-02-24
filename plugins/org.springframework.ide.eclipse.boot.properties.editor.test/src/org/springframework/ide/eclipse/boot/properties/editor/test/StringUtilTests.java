/*******************************************************************************
 * Copyright (c) 2014-2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.properties.editor.test;

import org.springframework.ide.eclipse.boot.util.StringUtil;

import junit.framework.TestCase;

/**
 * @author Kris De Volder
 */
public class StringUtilTests extends TestCase {

	public void testUpperCaseToHyphens() throws Exception {
		assertEquals("extra-small", StringUtil.upperCaseToHyphens("EXTRA_SMALL"));
		assertEquals("extra-small", StringUtil.upperCaseToHyphens("extra-small")); //can be applied to already converted string without any harm
		assertEquals("", StringUtil.upperCaseToHyphens(""));
		assertNull(StringUtil.upperCaseToHyphens(null));
	}

	public void testHasText() throws Exception {
		assertFalse(StringUtil.hasText(null));
		assertFalse(StringUtil.hasText(""));
		assertFalse(StringUtil.hasText("   \t\n\r"));
		assertTrue(StringUtil.hasText("something"));
	}

	public void testTrim() throws Exception {
		assertNull(StringUtil.trim(null));
		assertEquals("foo", StringUtil.trim("   foo  \n\r\t"));
	}

	public void testCommonPrefixLen() throws Exception {
		assertEquals("foo".length(), StringUtil.commonPrefixLength("foobarlonger", "fooshort"));
		assertEquals("foo".length(), StringUtil.commonPrefixLength("fooshort", "foobarlonger"));
		assertEquals("foo".length(), StringUtil.commonPrefixLength("foo", "foobarlonger"));
		assertEquals("foo".length(), StringUtil.commonPrefixLength("foobarlonger", "foo"));
		assertEquals(0, StringUtil.commonPrefixLength("", ""));
		assertEquals(0, StringUtil.commonPrefixLength("", "something"));
		assertEquals(0, StringUtil.commonPrefixLength("something", ""));
		assertEquals(0, StringUtil.commonPrefixLength("nothing", "in common"));
	}

	public void testCommonPrefix() throws Exception {
		assertEquals("foo", StringUtil.commonPrefix("foobarlonger", "fooshort"));
		assertEquals("foo", StringUtil.commonPrefix("fooshort", "foobarlonger"));
		assertEquals("foo", StringUtil.commonPrefix("foo", "foobarlonger"));
		assertEquals("foo", StringUtil.commonPrefix("foobarlonger", "foo"));
		assertEquals("", StringUtil.commonPrefix("", ""));
		assertEquals("", StringUtil.commonPrefix("", "something"));
		assertEquals("", StringUtil.commonPrefix("something", ""));
		assertEquals("", StringUtil.commonPrefix("nothing", "in common"));
	}

}
