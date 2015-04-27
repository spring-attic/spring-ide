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

import org.springframework.ide.eclipse.yaml.editor.path.YamlPath;
import org.springframework.ide.eclipse.yaml.editor.path.YamlPathSegment;
import org.springframework.ide.eclipse.yaml.editor.path.YamlPathSegment.YamlPathSegmentType;
import org.springframework.ide.eclipse.yaml.editor.structure.YamlStructureParser.SChildBearingNode;
import org.springframework.ide.eclipse.yaml.editor.structure.YamlStructureParser.SKeyNode;
import org.springframework.ide.eclipse.yaml.editor.structure.YamlStructureParser.SNode;
import org.springframework.ide.eclipse.yaml.editor.structure.YamlStructureParser.SNodeType;
import org.springframework.ide.eclipse.yaml.editor.structure.YamlStructureParser.SRootNode;

/**
 * Helper class that provides methods for creating the edits in a YamlDocument that
 * insert new 'property paths' into the document.
 *
 * @author Kris De Volder
 */
public class YamlPathEdits extends DocumentEdits {

	private YamlDocument doc;
	private IndentUtil indentUtil;

	public YamlPathEdits(YamlDocument doc) {
		super(doc.getDocument());
		this.doc = doc;
		this.indentUtil = new IndentUtil(doc);
	}

	/**
	 * Create the necessary edits to ensure that a given property
	 * path exists, placing cursor right after that the right place to start typing
	 * the property value.
	 */
	public void createPath(YamlPath path, String appendText) throws Exception {
		SRootNode root = doc.getStructure();
		createPath(root, path, appendText);
	}

	private void createPath(SChildBearingNode node, YamlPath path, String appendText) throws Exception {
		if (!path.isEmpty()) {
			YamlPathSegment s = path.getSegment(0);
			if (s.getType()==YamlPathSegmentType.VAL_AT_KEY) {
				String key = s.toPropString();
				SKeyNode existing = findChildForKey(node, key);
				if (existing==null) {
					createNewPath(node, path, appendText);
				} else {
					createPath(existing, path.tail(), appendText);
				}
			}
		} else {
			//whole path already exists. Just try to move cursor somewhere
			// sensible in the existing tail-end-node of the path.
			SNode child = node.getFirstRealChild();
			if (child!=null) {
				moveCursorTo(child.getStart());
			} else if (node.getNodeType()==SNodeType.KEY) {
				SKeyNode keyNode = (SKeyNode) node;
				int colonOffset = keyNode.getColonOffset();
				char c = doc.getChar(colonOffset+1);
				if (c==' ') {
					moveCursorTo(colonOffset+2); //cursor after the ": "
				} else {
					moveCursorTo(colonOffset+1); //cursor after the ":"
				}
			}
		}
	}

	private void createNewPath(SChildBearingNode parent, YamlPath path, String appendText) {
		int indent = getChildIndent(parent);
		int insertionPoint = getNewPathInsertionOffset(parent);
		boolean startOnNewLine = true;
		insert(insertionPoint, createPathInsertionText(path, indent, startOnNewLine, appendText));
	}

	protected String createPathInsertionText(YamlPath path, int indent, boolean startOnNewLine, String appendText) {
		StringBuilder buf = new StringBuilder();
		for (int i = 0; i < path.size(); i++) {
			if (startOnNewLine||i>0) {
				indentUtil.addNewlineWithIndent(indent, buf);
			}
			String key = path.getSegment(i).toPropString();
			buf.append(key);
			buf.append(":");
			indent += IndentUtil.INDENT_BY;
		}
		buf.append(indentUtil.applyIndentation(appendText, indent));
		return buf.toString();
	}

	private int getChildIndent(SNode parent) {
		if (parent.getNodeType()==SNodeType.ROOT) {
			return parent.getIndent();
		} else {
			return parent.getIndent()+IndentUtil.INDENT_BY;
		}
	}

	private int getNewPathInsertionOffset(SChildBearingNode parent) {
		SNode insertAfter = parent.getLastChild();
		if (insertAfter==null) {
			insertAfter = parent;
		}
		return insertAfter.getTreeEnd();
	}

	private SKeyNode findChildForKey(SChildBearingNode node, String key) throws Exception {
		for (SNode c : node.getChildren()) {
			if (c.getNodeType()==SNodeType.KEY) {
				String nodeKey = ((SKeyNode)c).getKey();
				//TODO: relax matching camel-case -> hyphens
				if (key.equals(nodeKey)) {
					return (SKeyNode)c;
				}
			}
		}
		return null;
	}

	public void createPathInPlace(SNode contextNode, YamlPath relativePath, int insertionPoint, String appendText) throws Exception {
		int indent = getChildIndent(contextNode);
		insert(insertionPoint, createPathInsertionText(relativePath, indent, needNewline(contextNode, insertionPoint), appendText));
	}

	private boolean needNewline(SNode contextNode, int insertionPoint) throws Exception {
		if (contextNode.getNodeType()==SNodeType.SEQ) {
			// after a '- ' its okay to put key on same line
			return false;
		} else {
			return lineHasTextBefore(insertionPoint);
		}
	}

	private boolean lineHasTextBefore(int insertionPoint) throws Exception {
		String textBefore = doc.getLineTextBefore(insertionPoint);
		return !textBefore.trim().isEmpty();
	}

}
