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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.springsource.ide.eclipse.commons.tests.util.StsTestCase.assertContains;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.swt.graphics.Point;
import org.springframework.ide.eclipse.editor.support.completions.ProposalApplier;
import org.springframework.ide.eclipse.editor.support.hover.HoverInfo;
import org.springframework.ide.eclipse.editor.support.hover.HoverInfoProvider;
import org.springframework.ide.eclipse.editor.support.reconcile.QuickfixContext;
import org.springframework.ide.eclipse.editor.support.util.DocumentUtil;
import org.springframework.ide.eclipse.editor.support.util.UserInteractions;

/**
 * Basic 'simulated' editor. Contains text and a cursor position / selection.
 */
public class MockEditor {

	protected int selectionStart;
	private int selectionEnd;
	Document document;
	private final HoverInfoProvider hoverProvider;
	public static final String CURSOR = "<*>";

	public Document getDocument() {
		return document;
	}

	public MockEditor(String text) {
		this(text, null);
	}

	public MockEditor(String text, HoverInfoProvider hoverProvider) {
		this.hoverProvider = hoverProvider;
		selectionStart = text.indexOf(MockEditor.CURSOR);
		if (selectionStart>=0) {
			text = text.substring(0,selectionStart) + text.substring(selectionStart+MockEditor.CURSOR.length());
			selectionEnd = text.indexOf(MockEditor.CURSOR, selectionStart);
			if (selectionEnd>=0) {
				text = text.substring(0, selectionEnd) + text.substring(selectionEnd+MockEditor.CURSOR.length());
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
		text = text.substring(0, selectionEnd) + MockEditor.CURSOR + text.substring(selectionEnd);
		if (selectionStart<selectionEnd) {
			text = text.substring(0,selectionStart) + MockEditor.CURSOR + text.substring(selectionStart);
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

	public void assertHoverContains(String hoverOver, String expect) {
		HoverInfo info = getHoverInfo(middleOf(hoverOver));
		assertNotNull("No hover info for '"+ hoverOver +"'", info);
		assertContains(expect, info.getHtml());
	}

	public int middleOf(String nodeText) {
		int start = startOf(nodeText);
		if (start>=0) {
			return start + nodeText.length()/2;
		}
		return -1;
	}

	public int startOf(String nodeText) {
		return document.get().indexOf(nodeText);
	}

	public int endOf(String nodeText) {
		int start = startOf(nodeText);
		if (start>=0) {
			return start+nodeText.length();
		}
		return -1;
	}

	public String textBetween(int start, int end) {
		return DocumentUtil.textBetween(document, start, end);
	}

	public String textUnder(IRegion r) throws BadLocationException {
		return document.get(r.getOffset(), r.getLength());
	}

	public IRegion getHoverRegion(int offset) {
		return hoverProvider.getHoverRegion(document, offset);
	}

	public HoverInfo getHoverInfo(int offset) {
		IRegion r = getHoverRegion(offset);
		if (r!=null) {
			return hoverProvider.getHoverInfo(document, r);
		}
		return null;
	}

	public void assertNoHover(String hoverOver) {
		HoverInfo info = getHoverInfo(middleOf(hoverOver));
		assertNull(info);
	}

	public void assertIsHoverRegion(String string) throws BadLocationException {
		assertHoverRegionCovers(middleOf(string), string);
		assertHoverRegionCovers(startOf(string), string);
		assertHoverRegionCovers(endOf(string)-1, string);
	}

	public void assertHoverRegionCovers(int offset, String expect) throws BadLocationException {
		IRegion r = getHoverRegion(offset);
		String actual = textUnder(r);
		assertEquals(expect, actual);
	}

	public void assertText(String expected) {
		if (expected.contains(CURSOR)) {
			assertEquals(expected, getText());
		} else {
			//assume the test doesn't care about cursor position so ignore it
			assertEquals(expected, getRawText());
		}
	}
}
