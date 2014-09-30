package org.springframework.ide.eclipse.propertiesfileeditor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * Organizes a list of 'property strings' in a tree. 
 * A property string is just a string consisting of segments separated by 
 * a separator character (typically a '.') e.g. 'spring.foo.bar'
 * 
 * @author Kris De Volder
 */
public class PropertyTree {

	//TODO: save memory by creating immutable EmtpyTerminalNode so a single instance can be reused to
	// represent all 'empty' nodes in the tree. Right now a new object is created
	// for each terminal empty node. This is potentially a lot of nodes (approximately equal
	// to the number of properties inserted to the tree.

	public interface ICompletionRequestor {
		void add(String join);
	}

	/**
	 * Like SEPARATOR_REGEXP but contains separate string literally. Used when
	 * building up properties by joining individual segments together.
	 */
	private static final char SEPARATOR = '.';
	
	/**
	 * Joins two string together adding a SEPARATOR in between if needed.
	 */
	private static String join(String prefix, String postfix) {
		if (prefix.isEmpty()) {
			return postfix;
		} else {
			return prefix+SEPARATOR+postfix;
		}
	}
	

	/**
	 * Split string at separator. Why not use String.split? Because it drops the final empty string 
	 * when splitting somehting like 'foo.bar.' => ['foo', 'bar'] but we want
	 * ['foo', 'bar', '' ] in this case.
	 */
	public static String[] split(String str) {
		ArrayList<String> segments = new ArrayList<String>();
		int pos = 0;
		while (pos>=0) {
			int splitPos = str.indexOf(SEPARATOR, pos);
			if (splitPos>=0) {
				segments.add(str.substring(pos, splitPos));
				pos = splitPos+1;
			} else {
				segments.add(str.substring(pos));
				pos = -1;
			}
		}
		return segments.toArray(new String[segments.size()]);
	}
	
	private static class Node {
		private Map<String, Node> children;
		
		/** Set to true when lookup can end here (i.e. the string upto this point is a 'valid' property */
		private boolean isTerminal = false; 

		public Node insert(int i, String[] segments) {
			if (i<segments.length) {
				Node c = ensureChild(segments[i]);
				c.insert(i+1, segments);
			} else {
				isTerminal = true;
			}
			return this;
		}

		private Node ensureChild(String segment) {
			if (children==null) {
				children = new TreeMap<String, Node>(); //Use treemap so things are sorted based on key.
			}
			Node child = children.get(segment);
			if (child==null) {
				child = new Node();
				children.put(segment, child);
			}
			return child;
		}

		public void findCompletions(int i, String[] segments, String prefix, ICompletionRequestor requestor) {
			if (children==null) {
				//nothing to search
				return;
			}
			String segment = segments[i];
			if (i==segments.length-1) {
				//last segment
				for (Entry<String, Node> child : children.entrySet()) {
					String childName = child.getKey();
					if (childName.startsWith(segment)) {
						Node childNode = child.getValue();
						if (childNode.isTerminal) {
							//isTerminal means the lookup could end here so we need no '.' at the end to continue down the 
							//tree.
							requestor.add(join(prefix,childName)); 
						}
						if (childNode.hasChildren()) {
							requestor.add(join(prefix,childName)+SEPARATOR); 
						}
					}
				}
			} else {
				//not last segment
				Node c = children.get(segment);
				c.findCompletions(i+1, segments, join(prefix, segment), requestor);
			}
		}

		private boolean hasChildren() {
			return children!=null;
		}

		public void dump(String prefix) {
			if (isTerminal) {
				System.out.println(prefix);
			}
			if (hasChildren()) {
				for (Entry<String, Node> child : children.entrySet()) {
					String childName = child.getKey();
					child.getValue().dump(join(prefix, childName));
				}
			}
		}

	}
	
	private Node root = new Node();
	
	public void insert(String property) {
		String[] segments = split(property);
		root = root.insert(0, segments);
	}
	
	/**
	 * Find completions based on given prefix.
	 */
	public void completions(String prefix, ICompletionRequestor requestor) {
		String[] segments = split(prefix);
		root.findCompletions(0, segments, "", requestor);
	}
	
	/**
	 * For debugging, dump all properties contain in this tree onto system out.
	 */
	public void dump() {
		System.out.println(">>> PropertyTree ====");
		root.dump("");
		System.out.println("<<< PropertyTree ====");
	}
}
