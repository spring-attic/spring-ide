package org.springframework.ide.eclipse.propertiesfileeditor.test;

import static org.junit.Assert.assertArrayEquals;
import junit.framework.TestCase;

import org.springframework.ide.eclipse.propertiesfileeditor.PropertyTree;

public class PropertyTreeTest extends TestCase {

	public void testStringSplit1() throws Exception {
		assertArrayEquals(new String [] {"foo", "bar"}, 
				PropertyTree.split("foo.bar")
		);
	}
	public void testStringSplit2() throws Exception {
		assertArrayEquals(new String [] {"foo"}, 
				PropertyTree.split("foo")
		);
	}
	public void testStringSplit3() throws Exception {
		assertArrayEquals(new String [] {"foo", ""}, 
				PropertyTree.split("foo.")
		);
	}
	public void testStringSplit4() throws Exception {
		assertArrayEquals(new String [] {"foo", "bar", ""}, 
				PropertyTree.split("foo.bar.")
		);
	}
	public void testStringSplit5() throws Exception {
		assertArrayEquals(new String [] {"", ""}, 
				PropertyTree.split(".")
		);
	}
	public void testStringSplit6() throws Exception {
		assertArrayEquals(new String [] {"a", "bc", "defg"}, 
				PropertyTree.split("a.bc.defg")
		);
	}
}
