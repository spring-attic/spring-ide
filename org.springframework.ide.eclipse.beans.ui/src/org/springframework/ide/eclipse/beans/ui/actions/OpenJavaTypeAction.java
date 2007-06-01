/*******************************************************************************
 * Copyright (c) 2005, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.actions;

import org.eclipse.jdt.core.IType;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ui.texteditor.ITextEditor;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.ide.eclipse.ui.SpringUIUtils;

/**
 * Opens the Java {@link IType} at current selection in {@link ITextEditor}.
 * 
 * @author Torsten Juergeleit
 */
public class OpenJavaTypeAction extends AbstractBeansConfigEditorAction {

	@Override
	public void run(IAction action) {
		if (getTextEditor() != null && getConfigFile() != null) {
			String className = guessType(getTextEditor());
			if (className != null && className.length() > 0) {
				IType type = JdtUtils.getJavaType(getConfigFile()
						.getProject(), className);
				if (type != null) {
					SpringUIUtils.openInEditor(type);
				}
			}
		}
	}

	private String guessType(ITextEditor editor) {
		String selectedText = SpringUIUtils.getSelectedText(editor);
		if (!isJavaType(selectedText)) {

			// try to search around caret
			int caretPosition = SpringUIUtils.getCaretOffset(editor);
			IDocument doc = editor.getDocumentProvider().getDocument(
					editor.getEditorInput());
			try {
				IRegion line = doc.getLineInformation(doc
						.getLineOfOffset(caretPosition));
				String lineText = doc.get(line.getOffset(), line.getLength());
				selectedText = findJavaType(lineText, caretPosition
						- line.getOffset());
			}
			catch (BadLocationException e) {
				selectedText = null;
			}
		}
		return selectedText;
	}

	private boolean isJavaType(String type) {
		if (type == null || type.length() == 0) {
			return false;
		}
		if (Character.isJavaIdentifierStart(type.charAt(0))) {
			for (int i = 1; i < type.length(); i++) {
				char c = type.charAt(i);
				if (!Character.isJavaIdentifierPart(c) && c != '.') {
					return false;
				}
			}
		}
		return true;
	}

	private String findJavaType(String line, int offset) {

		// search for nearest invalid java characters in both directions 
		int start = offset;
		while (start > 0) {
			char c = line.charAt(start - 1);
			if (!Character.isJavaIdentifierPart(c) && c != '.') {
				break;
			}
			start--;
		}

		int length = offset - start;
		int max = line.length();
		while (offset < max) {
			char c = line.charAt(offset);
			if (!Character.isJavaIdentifierPart(c) && c != '.') {
				break;
			}
			offset++;
			length++;
		}

		if (length == max) {
			return line;
		}
		else if (length > 0) {
			return line.substring(start, start + length);
		}
		return null;
	}
}
