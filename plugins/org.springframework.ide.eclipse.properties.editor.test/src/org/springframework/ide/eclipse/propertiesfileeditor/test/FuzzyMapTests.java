package org.springframework.ide.eclipse.propertiesfileeditor.test;

import junit.framework.TestCase;

import org.springframework.ide.eclipse.propertiesfileeditor.FuzzyMap;

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
