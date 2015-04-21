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

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension4;

/**
 * Helper methods to mainpulate indentation levels.
 *
 * @author Kris De Volder
 */
public class IndentUtil {

	/**
	 * Number of indentation levels (spaces) added between a child and parent.
	 */
	public static final int INDENT_BY = 2;

	/**
	 * Some functions introduce line separators and this may depend on the context (i.e. defailt line separator
	 * for the current document).
	 */
	public final String NEWLINE;

	public IndentUtil(String newline) {
		this.NEWLINE = newline;
	}

	public IndentUtil(YamlDocument doc) {
		IDocument d = doc.getDocument();
		if (d instanceof IDocumentExtension4) {
			this.NEWLINE = ((IDocumentExtension4) d).getDefaultLineDelimiter();
		} else {
			this.NEWLINE = "\n"; //This shouldn't really happen.
		}
	}

	/**
	 * Determine the 'known minimum' of two indentation levels. Correctly handle
	 * when either one or both indent levels are '-1' (unknown).
	 */
	public static int minIndent(int a, int b) {
		if (a==-1) {
			return b;
		} else if (b==-1) {
			return a;
		} else {
			return Math.min(a, b);
		}
	}

	public static void addIndent(int indent, StringBuilder buf) {
		for (int i = 0; i < indent; i++) {
			buf.append(' ');
		}
	}

	public void addNewlineWithIndent(int indent, StringBuilder buf) {
		buf.append(NEWLINE);
		addIndent(indent, buf);
	}

	public String newlineWithIndent(int indent) {
		StringBuilder buf = new StringBuilder();
		addNewlineWithIndent(indent, buf);
		return buf.toString();
	}

	/**
	 * Applies a certain level of indentation to all new lines in the given text. Newlines
	 * are expressed by '\n' characters in the text will be replaced by the appropriate
	 * newline + indent.
	 * <p>
	 * Notes:
	 *  - '\n' are replaced by the default line delimeter for the current document.
	 *  - indentation is not applied to the first line of text.
	 */
	public String applyIndentation(String text, int indentBy) {
		return text.replaceAll("\\n", newlineWithIndent(indentBy));
	}

}
