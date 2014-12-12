/*******************************************************************************
 * Copyright (c) 2014 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.propertiesfileeditor.test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;

import junit.framework.TestCase;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.swt.graphics.Point;
import org.springframework.configurationmetadata.ConfigurationMetadataProperty;
import org.springframework.ide.eclipse.propertiesfileeditor.SpringPropertiesCompletionEngine;

public class SpringPropertiesCompletionEngineTests extends TestCase {
	
	/**
	 * Basic 'simulated' editor. Contains text and a cursor position / selection.
	 */
	public class MockEditor {
		
		private int selectionStart;
		private int selectionEnd;
		private Document document;

		/**
		 * Create mock editor. Selection position is initialized by looking for the CURSOR string.
		 * <p>
		 * THe cursor string is not actually considered part of the text, but only a marker for
		 * the cursor position. 
		 * <p>
		 * If one 'cursor' marker is present in the text the selection 
		 * is length 0 and starts at the marker.
		 * <p>
		 * If two markers are present the selection is between the two 
		 * markers.
		 * <p>
		 * If no markers are present the cursor is placed at the very end of the document.
		 */
		public MockEditor(String text) {
			selectionStart = text.indexOf(CURSOR);
			if (selectionStart>=0) {
				text = text.substring(0,selectionStart) + text.substring(selectionStart+CURSOR.length());
				selectionEnd = text.indexOf(CURSOR, selectionStart);
				if (selectionEnd>=0) {
					text = text.substring(0, selectionEnd) + text.substring(selectionEnd+CURSOR.length());
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
			text = text.substring(0, selectionEnd) + CURSOR + text.substring(selectionEnd);
			if (selectionStart<selectionEnd) {
				text = text.substring(0,selectionStart) + CURSOR + text.substring(selectionStart);
			}
			return text;
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
		
	}

	private static final String INTEGER = Integer.class.getName();
	private static final String STRING = String.class.getName();
	private static final String CURSOR = "<*>";
	
	private static final Comparator<? super ICompletionProposal> COMPARATOR = new Comparator<ICompletionProposal>() {
		@Override
		public int compare(ICompletionProposal p1, ICompletionProposal p2) {
			return SpringPropertiesCompletionEngine.SORTER.compare(p1, p2);
		}
	};
	
	private SpringPropertiesCompletionEngine engine;
	
	public void data(String id, String type, Object deflt, String description) {
		ConfigurationMetadataProperty item = new ConfigurationMetadataProperty();
		item.setId(id);
		item.setDescription(description);
		item.setType(type);
		item.setDefaultValue(deflt);
		engine.add(item);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		engine = new SpringPropertiesCompletionEngine();
	}
	
	public void defaultTestData() {
		data("server.port", INTEGER, 8080, "Port where server listens for http.");
		data("server.address", STRING, "localhost", "Host name or address where server listens for http.");
	}
	
	public void testServerPort() throws Exception {
		data("server.port", INTEGER, 8080, "Port where server listens for http.");
		assertCompletion("ser<*>", "server.port=8080<*>");
		assertDisplayString("ser<*>", "server.port=8080 int Port where server listens for http.");
	}
	
	private void assertDisplayString(String editorContents, String expected) throws Exception {
		MockEditor editor = new MockEditor(editorContents);
		ICompletionProposal completion = getFirstCompletion(editor);
		assertEquals(expected, completion.getDisplayString());
	}

	/**
	 * Simulates applying the first completion to a text buffer and checks the result.
	 */
	private void assertCompletion(String textBefore, String expectTextAfter) throws Exception {
		MockEditor editor = new MockEditor(textBefore);
		ICompletionProposal completion = getFirstCompletion(editor);
		editor.apply(completion);
		assertEquals(expectTextAfter, editor.getText());
	}

	private ICompletionProposal getFirstCompletion(MockEditor editor)
			throws BadLocationException {
		Collection<ICompletionProposal> _completions = engine.getCompletions(editor.document, editor.selectionStart);
		ICompletionProposal[] completions = _completions.toArray(new ICompletionProposal[_completions.size()]);
		Arrays.sort(completions, COMPARATOR);
		ICompletionProposal completion = completions[0];
		return completion;
	}

}
