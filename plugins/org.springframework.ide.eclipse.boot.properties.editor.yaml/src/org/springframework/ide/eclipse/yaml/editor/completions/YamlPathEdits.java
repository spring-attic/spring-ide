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

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension4;
import org.eclipse.jface.text.IRegion;
import org.springframework.ide.eclipse.yaml.editor.ast.path.YamlPathSegment;
import org.springframework.ide.eclipse.yaml.editor.ast.path.YamlPathSegment.AtScalarKey;
import org.springframework.ide.eclipse.yaml.editor.ast.path.YamlPathSegment.YamlPathSegmentType;
import org.springframework.ide.eclipse.yaml.editor.completions.YamlStructureParser.SChildBearingNode;
import org.springframework.ide.eclipse.yaml.editor.completions.YamlStructureParser.SKeyNode;
import org.springframework.ide.eclipse.yaml.editor.completions.YamlStructureParser.SNode;
import org.springframework.ide.eclipse.yaml.editor.completions.YamlStructureParser.SNodeType;
import org.springframework.ide.eclipse.yaml.editor.completions.YamlStructureParser.SRootNode;

/**
 * Helper class that provides methods for creating the edits in a YamlDocument that
 * insert new 'property paths' into the document.
 *
 * @author Kris De Volder
 */
public class YamlPathEdits extends DocumentModifier {

	private static final int INDENT_BY = 2; // number of spaces to add when indenting a child, relative to parent indentation.
	private YamlDocument doc;
	final private String NEWLINE;

	public YamlPathEdits(YamlDocument doc) {
		this.doc = doc;
		IDocument d = doc.getDocument();
		if (d instanceof IDocumentExtension4) {
			this.NEWLINE = ((IDocumentExtension4) d).getDefaultLineDelimiter();
		} else {
			this.NEWLINE = "\n"; //This shouldn't really happen.
		}
	}

	/**
	 * Create the necessary edits to ensure that a given property
	 * path exists, placing cursor at the right place to start typing
	 * the property value.
	 *
	 * @param fromProperty
	 * @throws Exception
	 */
	public void createPath(YamlPath path) throws Exception {
		SRootNode root = doc.getStructure();
		createPath(root, path);
	}

	private void createPath(SChildBearingNode node, YamlPath path) throws Exception {
		if (!path.isEmpty()) {
			YamlPathSegment s = path.getSegment(0);
			if (s.getType()==YamlPathSegmentType.AT_SCALAR_KEY) {
				String key = s.toPropString();
				SKeyNode existing = findChildForKey(node, key);
				if (existing==null) {
					createNewPath(node, path);
				} else {
					createPath(existing, path.tail());
				}
			}
		}
	}

	private void createNewPath(SChildBearingNode parent, YamlPath path) {
		int indent = getChildIndent(parent);
		int insertionPoint = getNewPathInsertionOffset(parent);
		boolean startOnNewLine = true;
		this.insert(insertionPoint, createPathInsertionText(path, indent, startOnNewLine));
	}

	protected String createPathInsertionText(YamlPath path, int indent, boolean startOnNewLine) {
		StringBuilder buf = new StringBuilder();
		for (int i = 0; i < path.size(); i++) {
			if (startOnNewLine||i>0) {
				buf.append(NEWLINE);
				addIndent(indent, buf);
			}
			String key = path.getSegment(i).toPropString();
			buf.append(key);
			buf.append(":");
			indent += INDENT_BY;
		}
		//TODO: should do something different based on what's expected next
		buf.append(" "); //add a space after last line since it doesn't have newline yet.
		return buf.toString();
	}

	private int getChildIndent(SNode parent) {
		if (parent.getNodeType()==SNodeType.ROOT) {
			return parent.getIndent();
		} else {
			return parent.getIndent()+INDENT_BY;
		}
	}

	private int getNewPathInsertionOffset(SChildBearingNode parent) {
		SNode insertAfter = parent.getLastChild();
		if (insertAfter==null) {
			insertAfter = parent;
		}
		return insertAfter.getNodeEnd();
	}

	private void addIndent(int indent, StringBuilder buf) {
		for (int i = 0; i < indent; i++) {
			buf.append(' ');
		}
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

	public void createPathInPlace(SNode contextNode, YamlPath relativePath, int insertionPoint) throws Exception {
		int indent = getChildIndent(contextNode);
		insert(insertionPoint, createPathInsertionText(relativePath, indent, lineHasTextBefore(insertionPoint)));
	}

	private boolean lineHasTextBefore(int insertionPoint) throws Exception {
		String textBefore = doc.getLineTextBefore(insertionPoint);
		return !textBefore.trim().isEmpty();
	}

}
