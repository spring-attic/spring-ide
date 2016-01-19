/*******************************************************************************
 * Copyright (c) 2015, 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.properties.editor.test;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.swt.graphics.Point;
import org.springframework.ide.eclipse.editor.support.completions.ProposalApplier;

/**
 * Basic 'simulated' editor. Contains text and a cursor position / selection.
 */
public class MockEditor {

	protected int selectionStart;
	private int selectionEnd;
	Document document;
	public static final String CURSOR = "<*>";

	public Document getDocument() {
		return document;
	}

	public MockEditor(String text) {
		selectionStart = text.indexOf(MockPropertiesEditor.CURSOR);
		if (selectionStart>=0) {
			text = text.substring(0,selectionStart) + text.substring(selectionStart+MockPropertiesEditor.CURSOR.length());
			selectionEnd = text.indexOf(MockPropertiesEditor.CURSOR, selectionStart);
			if (selectionEnd>=0) {
				text = text.substring(0, selectionEnd) + text.substring(selectionEnd+MockPropertiesEditor.CURSOR.length());
			} else {
				selectionEnd = selectionStart;
			}
		} else {
			//No CURSOR markers found
			selectionStart = text.length();
			selectionEnd = text.length();
		}
		this.document = new Document(text);
	}

	/**
	 * Get the editor text, with cursor markers inserted (for easy textual comparison
	 * after applying a proposal)
	 */
	public String getText() {
		String text = document.get();
		text = text.substring(0, selectionEnd) + MockPropertiesEditor.CURSOR + text.substring(selectionEnd);
		if (selectionStart<selectionEnd) {
			text = text.substring(0,selectionStart) + MockPropertiesEditor.CURSOR + text.substring(selectionStart);
		}
		return deWindowsify(text);
	}

	private String deWindowsify(String text) {
		return text.replaceAll("\\r\\n", "\n");
	}

	/**
	 * Get the editor text, as is, without cursor markers.
	 */
	public String getRawText() {
		return document.get();
	}

	/**
	 * Set selection based on result returned by ICompletionProposal getSelection method.
	 */
	public void setSelection(Point selection) {
		if (selection!=null) {
			selectionStart = selection.x;
			selectionEnd = selectionStart+selection.y;
		}
	}

	public void apply(ICompletionProposal completion) {
		completion.apply(document);
		setSelection(completion.getSelection(document));
	}

	public String getText(int offset, int length) throws BadLocationException {
		if (offset>=document.getLength()) {
			//no bad location exception please. There's no text past the end of doc so return "". This is quite logical, thank you very much.
			return "";
		}
		return document.get(offset, length);
	}

	public void apply(ProposalApplier edit) throws Exception {
		edit.apply(document);
		Point sel = edit.getSelection(document);
		selectionStart = sel.x;
		selectionEnd = selectionStart+sel.y;
	}

	@Override
	public String toString() {
		return "===== editor ====\n"+getText()+"\n===============\n";
	}
}
