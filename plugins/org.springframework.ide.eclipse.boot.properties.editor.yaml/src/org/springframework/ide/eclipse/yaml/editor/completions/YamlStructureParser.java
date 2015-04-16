/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.yaml.editor.completions;

import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

/**
 * A robust, coarse-grained parser that guesses the structure of a
 * yml document based on indentation levels.
 * <p>
 * This is not a full parser but is desgned to succeed computing some kind of 'structure' tree
 * for anything you might throw at it. The goal is to be accurate only for 'typical' yml files
 * used to define spring-boot properties. Essentially a the file contains a bunch of nested
 * mapping nodes in 'block' style using 'simple' keys.
 * <p>
 * I.e something like this:
 * <pre>
 * foo:
 *   bar:
 *     zor: Hello
 *       this is
 *       tex
 *     more-keys:
 *       - foo
 *       - bar
 * </pre>
 * <p>
 * When the parser encounters something it can not identify as a 'simple-key: <value>'
 * binding then it treats that just as 'raw' text data and associates it as nested
 * information with the closest preceding recognized key node which is indented
 * at the same or lower level than this node.
 *
 * @author Kris De Volder
 */
public class YamlStructureParser {

	/**
	 * Pattern that matches a line starting with a 'simple key'
	 */
	public static final Pattern SIMPLE_KEY_LINE = Pattern.compile(
			"^(\\s*)(\\w(\\w|-)*):.*");

	/* for simple key we recognize

 	ns-plain-single(flow-key)
 	   ::= ( ns-plain-first-char(c)
             ( nb-plain-char(c)* ns-plain-char(c) )? )



	 */

	public static enum SNodeType {
		ROOT, KEY, RAW
	}

	private YamlDocument doc;
	private YamlLineReader input;

	public static class YamlLine {
		private YamlDocument doc;
		private int line;
		private int indent;
		public YamlLine(YamlDocument doc, int line) {
			this.line = line;
			this.doc = doc;
			this.indent = doc.getLineIndentation(line);
		}
		public int getIndent() {
			return indent;
		}
		public int getEnd() throws Exception {
			IRegion r = doc.getLineInformation(line);
			return r.getOffset()+r.getLength();
		}
		public int getStart() throws Exception {
			IRegion r = doc.getLineInformation(line);
			return r.getOffset();
		}
		public boolean matches(Pattern pat) throws Exception {
			return pat.matcher(getText()).matches();
		}
		public String getText() throws Exception {
			return doc.textBetween(getStart(), getEnd());
		}
		@Override
		public String toString() {
			try {
				return "YamlLine("+line+": "+getText()+")";
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	public class YamlLineReader {
		private YamlDocument doc;
		private int nextLine = 0; //next line to read

		public YamlLineReader(YamlDocument doc) {
			this.doc = doc;
		}

		public YamlLine read() {
			if (nextLine < doc.getDocument().getNumberOfLines()) {
				return new YamlLine(doc, nextLine++);
			}
			return null; //means EOF
		}
	}


	public YamlStructureParser(IDocument _doc) {
		this(new YamlDocument(_doc));
	}

	public YamlStructureParser(YamlDocument doc) {
		this.doc = doc;
		this.input = new YamlLineReader(doc);
	}

	private static void indent(Writer out, int indent) throws Exception {
		for (int i = 0; i < indent; i++) {
			out.write("  ");
		}
	}

	public static abstract class SNode {
		private SChildBearingNode parent;
		private int start;
		private int end;
		public SNode(SChildBearingNode parent, int start, int end) {
			Assert.isLegal(this instanceof SRootNode || parent!=null);
			this.parent = parent;
			this.start = start;
			this.end = end;
			if (parent!=null) {
				parent.addChild(this);
			}
		}
		public SChildBearingNode getParent() {
			return parent;
		}
		public int getStart() {
			return start;
		}
		public int getNodeEnd() {
			return end;
		}
		public abstract int getTreeEnd();
		public abstract int getIndent();

		public final String toString() {
			StringWriter out = new StringWriter();
			try {
				dump(out, 0);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			return out.toString();
		}

		public abstract SNodeType getNodeType();

		/**
		 * Get the raw text of the node itself, this does not include the text
		 * of its children
		 */
		public abstract String getRawNodeText() throws Exception;

		public String getIndentedText() throws Exception {
			return getRawNodeText().substring(Math.max(0, getIndent()));
		}

		protected abstract void dump(Writer out, int indent) throws Exception;
	}

	public static class SRootNode extends SChildBearingNode {

		public SRootNode() {
			super(null, 0,0);
		}

		@Override
		public int getIndent() {
			return 0;
		}

		@Override
		public SNodeType getNodeType() {
			return SNodeType.ROOT;
		}

		@Override
		public String getRawNodeText() {
			//Root node ha no text of its own
			return "";
		}

	}

	public static abstract class SChildBearingNode extends SNode {
		private List<SNode> children = null;

		public SChildBearingNode(SChildBearingNode parent, int start, int end) {
			super(parent, start, end);

			}

		public List<SNode> getChildren() {
			if (children!=null) {
				return Collections.unmodifiableList(children);
			}
			return Collections.emptyList();
		}
		public void addChild(SNode c) {
			if (children==null) {
				children = new ArrayList<SNode>();
			}
			children.add(c);
		}
		public SNode getLastChild() {
			List<SNode> cs = getChildren();
			return cs.get(cs.size()-1);
		}
		@Override
		public int getTreeEnd() {
			if (getChildren().isEmpty()) {
				return getNodeEnd();
			}
			return getLastChild().getTreeEnd();
		}
		@Override
		protected final void dump(Writer out, int indent) throws Exception {
			indent(out, indent);
			out.write(getNodeType().toString());
			out.write('(');
			int nodeIndent = getIndent();
			out.write(""+nodeIndent);
			out.write("): ");
			out.write(getIndentedText());
			out.write('\n');
			for (SNode child : getChildren()) {
				child.dump(out, indent+1);
			}
		}

}

	public abstract class SLeafNode extends SNode {

		public SLeafNode(SChildBearingNode parent, int start, int end) {
			super(parent, start, end);
		}

		public int getTreeEnd() {
			return getNodeEnd();
		}

		@Override
		protected final void dump(Writer out, int indent) throws Exception {
			indent(out, indent);
			out.write(getNodeType().toString());
			out.write('(');
			int nodeIndent = getIndent();
			out.write(""+nodeIndent);
			out.write("): ");
			out.write(getIndentedText());
			out.write('\n');
		}
	}



	public class SRawNode extends SLeafNode {

		private YamlLine line;

		public SRawNode(SChildBearingNode parent, YamlLine line) throws Exception {
			super(parent, line.getStart(), line.getEnd());
			this.line = line;
		}

		@Override
		public int getIndent() {
			return line.getIndent();
		}

		@Override
		public SNodeType getNodeType() {
			return SNodeType.RAW;
		}

		@Override
		public String getRawNodeText() throws Exception {
			return line.getText();
		}

	}


	public SRootNode parse() throws Exception {
		SRootNode root = new SRootNode();
		SChildBearingNode parent = root; //path is defined by parent, parent.getParent(), etc
		YamlLine line;
		while (null!=(line=input.read())) {
			int indent = line.getIndent();
			if (indent==-1) {
				new SRawNode(parent, line);
			} else {
				parent = dropTo(parent, indent);
				if (line.matches(SIMPLE_KEY_LINE)) {
					int currentIndent = parent.getIndent();
					if (currentIndent==line.getIndent() && parent.getParent()!=null) {
						parent = parent.getParent();
					}
					parent = new SKeyNode(parent, line);
				} else {
					new SRawNode(parent, line);
				}
			}
		}
		return root;
	}

	private SChildBearingNode dropTo(SChildBearingNode node, int indent) {
		while (indent<node.getIndent()) {
			node = node.getParent();
		}
		//node never null because RootNode always has indent 0
		return node;
	}

	public class SKeyNode extends SChildBearingNode {

		private YamlLine line;

		public SKeyNode(SChildBearingNode parent, YamlLine line) throws Exception {
			super(parent, line.getStart(), line.getEnd());
			this.line = line;
		}

		@Override
		public int getIndent() {
			return line.getIndent();
		}

		@Override
		public SNodeType getNodeType() {
			return SNodeType.KEY;
		}

		@Override
		public String getRawNodeText() throws Exception {
			return line.getText();
		}
	}
}
