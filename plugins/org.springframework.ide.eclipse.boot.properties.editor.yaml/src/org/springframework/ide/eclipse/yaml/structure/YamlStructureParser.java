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
package org.springframework.ide.eclipse.yaml.structure;

import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.springframework.ide.eclipse.yaml.editor.completions.IndentUtil;
import org.springframework.ide.eclipse.yaml.editor.completions.YamlDocument;
import org.springframework.ide.eclipse.yaml.editor.completions.YamlNavigable;
import org.springframework.ide.eclipse.yaml.editor.path.YamlPathSegment;
import org.springframework.ide.eclipse.yaml.editor.path.YamlPathSegment.YamlPathSegmentType;
import org.springframework.ide.eclipse.yaml.structure.YamlStructureParser.SChildBearingNode;
import org.springframework.ide.eclipse.yaml.structure.YamlStructureParser.SNode;

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
			"^(\\w(\\w|-)*):.*");
	//TODO: the parrern above is too selective (e.g. in real yaml one can have
	//spaces in simple keys and lots of other characters that this pattern does not
	//allow. For now it is good enough because we are only interested in spring property
	//names which typically do not contain spaces and other funky characters.

	/**
	 * Pattern that matches a line starting with a sequence header '- '
	 */
	public static final Pattern SEQ_LINE = Pattern.compile(
			"^(\\-( |$)).*");


//	public static final Pattern SEQ_LINE = Pattern.compile(
//			"^( *)- .*");

	public static enum SNodeType {
		ROOT, KEY, SEQ, RAW
	}

	private YamlLineReader input;

	public static class YamlLine {

		// line = "    hello"
		//         ^   ^    ^
		//         |   |    end
		//         |   indent
		//         start

		public static YamlLine atLineNumber(YamlDocument doc, int line) throws Exception {
			if (line<doc.getDocument().getNumberOfLines()) {
				IRegion l = doc.getLineInformation(line);
				int start = l.getOffset();
				int end = start + l.getLength();
				return new YamlLine(doc, start, doc.getLineIndentation(line), end);
			}
			return null;
		}

		private YamlDocument doc;
		private int start;
		private int indent;
		private int end;

		private YamlLine(YamlDocument doc, int start, int indent, int end) {
			this.doc = doc;
			this.start = start;
			this.indent = indent;
			this.end = end;
		}
		public int getIndent() {
			return indent;
		}
		public int getEnd() {
			return end;
		}
		public int getStart() {
			return start;
		}
		public boolean matches(Pattern pat) throws Exception {
			return pat.matcher(getTextWithoutIndent()).matches();
		}
		public String getTextWithoutIndent() throws Exception {
			return doc.textBetween(getStart()+getIndent(), getEnd());
		}
		public String getText() throws Exception {
			return doc.textBetween(getStart(), getEnd());
		}
		@Override
		public String toString() {
			try {
				return "YamlLine("+getLineNumber()+": "+getText()+")";
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		private int getLineNumber() throws Exception {
			return doc.getLineOfOffset(start);
		}
		public YamlLine moveIndentMark(int moveBy) throws Exception {
			return new YamlLine(doc, start, Math.min(indent+moveBy, getLineLength()), end);
		}
		private int getLineLength() throws Exception {
			return getEnd()-getStart();
		}
		public YamlDocument getDocument() {
			return doc;
		}
	}

	public class YamlLineReader {
		private final YamlDocument doc;
		private int nextLine = 0; //next line to read

		public YamlLineReader(YamlDocument doc) {
			this.doc = doc;
		}

		public YamlLine read() throws Exception {
			if (nextLine < doc.getDocument().getNumberOfLines()) {
				return YamlLine.atLineNumber(doc, nextLine++);
			}
			return null; //means EOF
		}

		public YamlDocument getDocument() {
			return doc;
		}
	}

	public YamlStructureParser(YamlDocument doc) {
		this.input = new YamlLineReader(doc);
	}

	private static void indent(Writer out, int indent) throws Exception {
		for (int i = 0; i < indent; i++) {
			out.write("  ");
		}
	}

	public static abstract class SNode implements YamlNavigable<SNode> {
		private SChildBearingNode parent;
		private int indent;
		private int start;
		private int end;
		protected final YamlDocument doc;

		public SNode(SChildBearingNode parent, YamlDocument doc, int indent, int start, int end) {
			Assert.isLegal(this instanceof SRootNode || parent!=null);
			this.parent = parent;
			this.doc = doc;
			this.indent = indent;
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
		public final int getIndent() {
			return indent;
		}

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
		public abstract SNode find(int offset);

		public boolean nodeContains(int offset) {
			return getStart()+Math.max(0, getIndent())<=offset && offset<=getNodeEnd();
		}

		public boolean treeContains(int offset) {
			return getStart()<=offset && offset<=getTreeEnd();
		}

		public String getText() throws Exception {
			return doc.textBetween(start, end);
		}

		/**
		 * Default implementation, doesn't support any type of traversal operation.
		 * Subclasses must override and implement where appropriate.
		 */
		@Override
		public SNode traverse(YamlPathSegment s) throws Exception {
			return null;
		}

		protected abstract void dump(Writer out, int indent) throws Exception;

	}

	public static class SRootNode extends SChildBearingNode {

		public SRootNode(YamlDocument doc) {
			super(null, doc, 0,0,0);
		}

		@Override
		public SNodeType getNodeType() {
			return SNodeType.ROOT;
		}

	}

	public static abstract class SChildBearingNode extends SNode {
		private List<SNode> children = null;

		public SChildBearingNode(SChildBearingNode parent, YamlDocument doc, int indent, int start, int end) {
			super(parent, doc, indent, start, end);
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
			if (!cs.isEmpty()) {
				return cs.get(cs.size()-1);
			}
			return null;
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
			out.write(getText());
			out.write('\n');
			for (SNode child : getChildren()) {
				child.dump(out, indent+1);
			}
		}

		@Override
		public SNode find(int offset) {
			if (!treeContains(offset)) {
				return null;
			}
			for (SNode c : getChildren()) {
				SNode fromChild = c.find(offset);
				if (fromChild!=null) {
					return fromChild;
				}
			}
			return this;
		}

		@Override
		public SNode traverse(YamlPathSegment s) throws Exception {
			switch (s.getType()) {
			case AT_KEY:
				return this.getChildWithKey(s.toPropString());
			case AT_INDEX:
				return this.getSeqChildWithIndex(s.toIndex());
			default:
				return null;
			}
		}

		private SSeqNode getSeqChildWithIndex(int index) {
			if (index>=0) {
				List<SNode> children = getChildren();
				if (index<children.size()) {
					SNode child = children.get(index);
					if (child instanceof SSeqNode) {
						return (SSeqNode) child;
					}
				}
			}
			return null;
		}

		public SKeyNode getChildWithKey(String key) throws Exception {
			//TODO: index based on keys? May not be worth it for small number of keys
			for (SNode node: getChildren()) {
				if (node.getNodeType()==SNodeType.KEY) {
					String nodeKey = ((SKeyNode)node).getKey();
					if (key.equals(nodeKey)) {
						return (SKeyNode) node;
					}
				}
			}
			return null;
		}

		public SNode getFirstRealChild() {
			for (SNode c : getChildren()) {
				if (c.getIndent()>=0) {
					return c;
				}
			}
			return null;
		}


	}

	public abstract class SLeafNode extends SNode {


		public SLeafNode(SChildBearingNode parent, YamlDocument doc,
				int indent, int start, int end) {
			super(parent, doc, indent, start, end);
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
			out.write(getText());
			out.write('\n');
		}

		@Override
		public SNode find(int offset) {
			if (treeContains(offset)) {
				return this;
			}
			return null;
		}
	}

	public class SRawNode extends SLeafNode {

		public SRawNode(SChildBearingNode parent, YamlDocument doc, int indent,
				int start, int end) {
			super(parent, doc, indent, start, end);
		}

		@Override
		public SNodeType getNodeType() {
			return SNodeType.RAW;
		}
	}

	public SRootNode parse() throws Exception {
		SRootNode root = new SRootNode(input.getDocument());
		SChildBearingNode parent = root; //path is defined by parent, parent.getParent(), etc
		YamlLine line;
		while (null!=(line=input.read())) {
			int indent = line.getIndent();
			if (indent==-1) {
				createRawNode(parent, line);
			} else {
				parent = dropTo(parent, indent);
				parent = parseLine(parent, line, true);
			}
		}
		return root;
	}

	protected SChildBearingNode parseLine(SChildBearingNode parent, YamlLine line, boolean createRawNode) throws Exception {
		if (line.matches(SIMPLE_KEY_LINE)) {
			int currentIndent = line.getIndent();
			while (currentIndent==parent.getIndent() && parent.getParent()!=null) {
				parent = parent.getParent();
			}
			parent = createKeyNode(parent, line);
		} else if (line.matches(SEQ_LINE)) {
			int currentIndent = line.getIndent();
			while (currentIndent==parent.getIndent() && parent.getNodeType()==SNodeType.SEQ) {
				parent = parent.getParent();
			}
			parent = createSeqNode(parent, line);
			parent = parseLine(parent, line.moveIndentMark(2), false); //parse from just after "- " for nested seq and key nodes
		} else if (createRawNode) {
			createRawNode(parent, line);
		}
		return parent;
	}

	private SChildBearingNode createSeqNode(SChildBearingNode parent, YamlLine line) throws Exception {
		int indent = line.getIndent();
		int start = line.getStart() + line.getIndent(); //use + is okay because seq node never have 'indefined' indent
		int end = line.getEnd();
		return new SSeqNode(parent, line.getDocument(), indent, start, end);
	}

	private SChildBearingNode createKeyNode(SChildBearingNode parent, YamlLine line) throws Exception {
		int indent = line.getIndent();
		int start = line.getStart() + line.getIndent(); //use + is okay because key node never have 'indefined' indent
		int end = line.getEnd();
		return new SKeyNode(parent, line.getDocument(), indent, start, end);
	}

	private SRawNode createRawNode(SChildBearingNode parent, YamlLine line) {
		int indent = line.getIndent();
		int start = IndentUtil.addToOffset(line.getStart(), indent);
		int end = line.getEnd();
		return new SRawNode(parent, line.getDocument(), indent, start, end);
	}


	private SChildBearingNode dropTo(SChildBearingNode node, int indent) {
		while (indent<node.getIndent()) {
			node = node.getParent();
		}
		return node;
	}

	public class SSeqNode extends SChildBearingNode {
		public SSeqNode(SChildBearingNode parent, YamlDocument doc, int indent, int start, int end) throws Exception {
			super(parent, doc, indent, start, end);
		}

		@Override
		public SNodeType getNodeType() {
			return SNodeType.SEQ;
		}

		public boolean isInValue(int offset) {
			return offset>=getStart()+2 //"- ".length()
					&& offset <= getTreeEnd();
		}
	}

	public class SKeyNode extends SChildBearingNode {

		private int colonOffset;

		public SKeyNode(SChildBearingNode parent, YamlDocument doc, int indent, int start, int end) throws Exception {
			super(parent, doc, indent, start, end);
			int relativeColonOffset = doc.textBetween(start, end).indexOf(':');
			Assert.isLegal(relativeColonOffset>=0);
			this.colonOffset = relativeColonOffset + start;
		}

		@Override
		public SNodeType getNodeType() {
			return SNodeType.KEY;
		}

		public String getKey() throws Exception {
			return doc.textBetween(getStart(), getColonOffset());
		}

		/**
		 * Get the offset of the ':' character that separates the 'key' from the 'value' area.
		 * @return Absolute offset (from beginning of document).
		 */
		public int getColonOffset() {
			return colonOffset;
		}

		public boolean isInKey(int offset) throws Exception {
			return getStart()<=offset && offset <= getColonOffset();
		}

		public boolean isInValue(int offset) {
			return offset> getColonOffset() && offset<=getTreeEnd();
		}

	}
}
