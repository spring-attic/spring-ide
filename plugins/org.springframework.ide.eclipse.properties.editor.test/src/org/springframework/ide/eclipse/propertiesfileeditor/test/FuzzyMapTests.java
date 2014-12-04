package org.springframework.ide.eclipse.propertiesfileeditor.test;

import org.springframework.ide.eclipse.propertiesfileeditor.FuzzyMap;

import junit.framework.TestCase;

public class FuzzyMapTests extends TestCase {
	
	public void testMatches() {
		assertMatch(true, "", "");
		assertMatch(true, "", "abc");
		assertMatch(true, "server.port", "server.port");
		assertMatch(true, "port", "server.port");
		assertMatch(true, "sport", "server.port");
		assertMatch(false, "spox", "server.port");
	}

	private void assertMatch(boolean expect, String pattern, String data) {
		boolean actual = FuzzyMap.match(pattern, data)!=0.0;
		assertEquals(expect, actual);
	}

}
