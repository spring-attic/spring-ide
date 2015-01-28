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
package org.springframework.ide.eclipse.propertiesfileeditor.test;

import junit.framework.TestCase;

import org.springframework.ide.eclipse.boot.properties.editor.FuzzyMap;

public class FuzzyMapTests extends TestCase {
	
	public void testMatches() {
		assertMatch(true, "", "");
		assertMatch(true, "", "abc");
		assertMatch(true, "server.port", "server.port");
		assertMatch(true, "port", "server.port");
		assertMatch(true, "sport", "server.port");
		assertMatch(false, "spox", "server.port");
	}
	
	public void testOrder() {
		assertMatchOrder("port", 
				"port",
				"server.port",
				"server.port-mapping",
				"piano.sorting"
		);
	}
	
	public class TestMap extends FuzzyMap<String> {
		public TestMap(String... entries) {
			for (String e : entries) {
				add(e);
			}
		}
		protected String getKey(String entry) {
			return entry;
		}
	}
	
	public void testCommonPrefix() {
		String[] entries = {
				"a",
				"archipel",
				"aardappel",
				"aardbei",
				"aardvark",
				"zoroaster"
		};
		String[] expectPrefix = {
				"a",
				"a",
				"aard",
				"aard",
				"aard",
				""
		};
		for (int focusOn = 0; focusOn < entries.length; focusOn++) {
			TestMap map = new TestMap();
			for (int other = 0; other < entries.length; other++) {
				if (focusOn!=other) {
					map.add(entries[other]);
				}
			}
			String prefix = map.findValidPrefix(entries[focusOn]);
			String prefixEntry = map.findLongestCommonPrefixEntry(entries[focusOn]);
			assertEquals(expectPrefix[focusOn], prefix);
			assertTrue(prefixEntry.startsWith(prefixEntry));
			assertTrue(prefixEntry.length()>prefix.length());
		}
	}
	
	public void testCommonPrefixWithExactMatch() {
		String[] entries = {
				"a",
				"archipel",
				"aardappel",
				"aardbei",
				"aardvark",
				"zoroaster"
		};
		TestMap map = new TestMap(entries);
		for (String find : entries) {
			String found = map.findLongestCommonPrefixEntry(find);
			assertEquals(find, found);
		}
	}
	
	public void testCommonPrefixEmptyMap() {
		TestMap empty = new TestMap();
		assertEquals(null, empty.findValidPrefix("foo"));
		assertEquals(null, empty.findValidPrefix(""));
		assertEquals(null, empty.findLongestCommonPrefixEntry("aaa"));
		assertEquals(null, empty.findLongestCommonPrefixEntry(""));
	}
	

	private void assertMatchOrder(String pattern, String... datas) {
		double previousScore = FuzzyMap.match(pattern, datas[0]);
		assertTrue(previousScore!=0.0);
		for (int i = 1; i < datas.length; i++) {
			String data = datas[i];
			double score = FuzzyMap.match(pattern, data);
			assertTrue("Wrong score order: '"+datas[i-1]+"'["+previousScore+"] '"+data+"' ["+score+"]", previousScore>score);
			previousScore = score;
		}
	}

	private void assertMatch(boolean expect, String pattern, String data) {
		boolean actual = FuzzyMap.match(pattern, data)!=0.0;
		assertEquals(expect, actual);
	}

}
