/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 

package org.springframework.ide.eclipse.beans.ui.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.ITextEditor;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.beans.ui.BeansUIPlugin;
import org.springframework.ide.eclipse.beans.ui.BeansUIUtils;

public class OpenJavaType extends Action implements IEditorActionDelegate {

    private ITextEditor editor;
    private IFile file;

	public void setActiveEditor(IAction action, IEditorPart editor) {
		if (editor instanceof ITextEditor) {
			this.editor = (ITextEditor) editor;
			this.file = BeansUIUtils.getConfigFile(editor);
		} else {
			this.editor = null;
			this.file = null;
		}

		// Disable action if not a Spring config file is currently edited 
		action.setEnabled(this.file != null);
	}

	public void selectionChanged(IAction action, ISelection selection) {
	}

	public void run(IAction action) {
		String className = guessType();
		if (className != null && className.length() > 0) {
			IBeansProject project = BeansCorePlugin.getModel().getProject(
															 file.getProject());
			IType type = project.getJavaType(className);
			if (type != null) {
				BeansUIUtils.openInEditor(type);
				return;
			}
		}
	}

	private String guessType() {
		String selectedText = BeansUIUtils.getSelectedText(editor);
		if (!isJavaType(selectedText)) {

			// try to search around caret
			int caretPosition = BeansUIUtils.getCaretOffset(editor);
			IDocument doc =
				editor.getDocumentProvider().getDocument(
					editor.getEditorInput());
			try {
				IRegion line = doc.getLineInformation(doc.getLineOfOffset(
																caretPosition));
				String lineText = doc.get(line.getOffset(), line.getLength());
				selectedText = findJavaType(lineText,
											caretPosition - line.getOffset());
			} catch (BadLocationException e) {
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
		while (length < max) {
			char c = line.charAt(offset);
			if (!Character.isJavaIdentifierPart(c) && c != '.') {
				break;
			}
			offset++;
			length++;
		}

		if (length == max) {
			return line;
		} else if (length > 0) {
			return line.substring(start, start + length);
		}
		return null;
	}
}
