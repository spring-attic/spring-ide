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
	
	public void testEmpty() {
		PropertyTree props = new PropertyTree();
		assertCompletions(props, "");
	}
	
	public void testSimple() throws Exception {
		PropertyTree props = propertyTree(
				"start",
				"stop",
				"arbiter",
				"stinger",
				"sanity"
		);
		
		assertCompletions(props, "", 
				//Results expected in alphabetic order
				"arbiter",
				"sanity",
				"start",
				"stinger",
				"stop"
		);
		
		assertCompletions(props, "s", 
		//		"arbiter",
				"sanity",
				"start",
				"stinger",
				"stop"
		);

		assertCompletions(props, "st", 
		//		"arbiter",
		//		"sanity",
				"start",
				"stinger",
				"stop"
		);

		assertCompletions(props, "sta", 
		//		"arbiter",
		//		"sanity",
				"start"
		//		"stinger",
		//		"stop"
		);

		assertCompletions(props, "start", 
		//		"arbiter",
		//		"sanity",
				"start"
		//		"stinger",
		//		"stop"
		);
		
		assertCompletions(props, "starte" 
		//		"arbiter",
		//		"sanity",
		//		"start"
		//		"stinger",
		//		"stop"
		);
	}
	
	public void testNested() throws Exception {
		PropertyTree props = propertyTree(
				"start.the.bus",
				"start.the.train",
				"stop.the.bus",
				"start.a.bus",
				"stop.a.bus",
				"start.a.train",
				"stop.a.train",
				"security"
		);
		
		assertCompletions(props, "", 
				//Results expected in alphabetic order
				"security",
				"start.",
				"stop."
		);

		assertCompletions(props, "start.", 
				"start.a.",
				"start.the."
		);

		assertCompletions(props, "start.t", 
				"start.the."
		);

		assertCompletions(props, "start.the.", 
				"start.the.bus",
				"start.the.train"
		);
		
	}

	public void testNestedAndTerminal() throws Exception {
		PropertyTree props = propertyTree(
				"foo.bar",
				"foo.bar.zinger"
		);
		
		assertCompletions(props, "foo.", 
				"foo.bar",
				"foo.bar."
		);
		
	}
	
	/**
	 * Create a property tree with a bunch of String as intialization data.
	 */
	public PropertyTree propertyTree(String... props){
		PropertyTree tree = new PropertyTree();
		for (String prop : props) {
			tree.insert(prop);
		}
		return tree;
	}
	
	/**
	 * Check expected completions are returned by given property tree for given prefix.
	 */
	public void assertCompletions(PropertyTree props, String prefix, String... expectedCompletions) {
		final StringBuilder results = new StringBuilder();
		props.completions(prefix, new PropertyTree.ICompletionRequestor() {
			public void add(String completion) {
				results.append(completion+"\n");
			}
		});

		StringBuilder expecteds = new StringBuilder();
		for (String expect : expectedCompletions) {
			expecteds.append(expect+"\n");
		}

		assertEquals(expecteds.toString(), results.toString());
	}
	
	
}
