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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.texteditor.ITextEditor;
import org.springframework.ide.eclipse.beans.ui.BeansUIUtils;
import org.springframework.ide.eclipse.beans.ui.views.BeansViewLocation;

public class ShowInView extends Action implements IEditorActionDelegate,
												IWorkbenchWindowActionDelegate {
    private ITextEditor editor;
    private IFile file;

	public void init(IWorkbenchWindow window) {
		IEditorPart editor;
		IWorkbenchPage page = window.getActivePage();
		if (page != null) {
			editor = page.getActiveEditor();
		} else {
			editor = null;
		}
		setActiveEditor(this, editor);
	}

	public void dispose() {
		// unused
	}

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
		setActiveEditor(action, BeansUIUtils.getActiveEditor());
	}

	public void run(IAction action) {
		if (editor != null && file != null) {
			BeansViewLocation location = guessBeansViewLocation();
			if (location != null) {
				location.show();
			}
		}
	}

	private BeansViewLocation guessBeansViewLocation() {
		int caretOffset = BeansUIUtils.getCaretOffset(editor);
		IDocument doc = editor.getDocumentProvider().getDocument(
													   editor.getEditorInput());
		BeansViewLocationGuesser guesser = new BeansViewLocationGuesser(doc,
																   caretOffset);
		if (guesser.hasBeanName()) {
			BeansViewLocation location = new BeansViewLocation();
			location.setProjectName(file.getProject().getName());
			location.setConfigName(file.getProjectRelativePath().toString());
			location.setBeanName(guesser.getBeanName());
			if (guesser.hasPropertyName()) {
				location.setPropertyName(guesser.getPropertyName());
			}
			return location;
		}
		return null;
	}

	private class BeansViewLocationGuesser {

		private IDocument doc;
		private int caretOffset;
		private int nameOffset;
		private String name;
		private int beanOffset;
		private List beanTokens;
		private String beanName;
		private int propertyOffset;
		private List propertyTokens;
		private String propertyName;

		public BeansViewLocationGuesser(IDocument doc, int caretOffset) {
			this.doc = doc;
			this.caretOffset = caretOffset;
			guessName();
			createBeanTokens();
			guessBeanName();
			createPropertyTokens();
			guessPropertyName();
		}

		private void guessName() {
			String name = null;
			int nameOffset = -1;

			// search for attribute value delimiter (") in both directions 
			try {
				int startOffset = caretOffset;
				while (startOffset > 0) {
					char c = doc.getChar(startOffset - 1);
					if (c == '"' || c == '<' || c == '>' || c == '/') {
						break;
					}
					startOffset--;
				}

				int endOffset = caretOffset;
				int length = endOffset - startOffset;
				int max = doc.getLength() - startOffset;
				while (length < max) {
					char c = doc.getChar(endOffset);
					if (c == '"' || c == '<' || c == '>' || c == '/') {
						break;
					}
					endOffset++;
					length++;
				}

				if (startOffset > 0 && length < max) {
					int offset = startOffset - 1;

					// check for leading and trailing quote
					if (doc.getChar(offset) == '"' &&
												doc.getChar(endOffset) == '"') {
						// check for '='
						while (offset > 0 &&
								 Character.isWhitespace(doc.getChar(--offset)));
						if (doc.getChar(offset) == '=') {
							name = doc.get(startOffset, length);
							nameOffset = startOffset;
						}
					}
				}
			} catch (BadLocationException e) {
			}
			this.name = name;
			this.nameOffset = nameOffset;
		}

		private void createBeanTokens() {
			List beanTokens = null;
			if (nameOffset != -1) {
				try {
					beanOffset = searchBackward(doc, nameOffset, "<bean");
					if (beanOffset != -1) {
						String beanText = doc.get(beanOffset,
												  nameOffset - beanOffset - 1);
						String[] tokens = beanText.split("[=\\s]");
						beanTokens = new ArrayList(Arrays.asList(tokens));

						// remove empty tokens from list
						Iterator iter = beanTokens.iterator();
						while (iter.hasNext()) {
							String token = (String) iter.next();
							if (token.length() == 0){
								iter.remove();
							}
						}
					}
				} catch (BadLocationException e) {
				}
			}
			this.beanTokens = beanTokens;
		}

		private void guessBeanName() {
			String beanName = null;
			if (beanTokens != null) {
				Iterator iter = beanTokens.iterator();
				while (iter.hasNext()) {
					String token = (String) iter.next();
					if (token.equals("id") || token.equals("name")) {
						if (iter.hasNext()) {
							token = (String) iter.next();

							// remove leading and trailing quote
							if (token.length() > 1 &&
											   token.charAt(0) == '"') {
								token = token.substring(1);
							}
							int pos = token.indexOf('"');
							if (pos != -1) {
								token = token.substring(0, pos);
							}
							beanName = token;
						} else {
							beanName = this.name;
						}
						break;
					}
				}
			}
			this.beanName = beanName;
		}

		private void createPropertyTokens() {
			List propertyTokens = null;
			if (nameOffset != -1) {
				try {
					propertyOffset = searchBackward(doc, nameOffset,
													"<property");
					if (propertyOffset > beanOffset) {
						String propertyText = doc.get(propertyOffset,
											   nameOffset - propertyOffset - 1);
						String[] tokens = propertyText.split("[=\\s]");
						propertyTokens = new ArrayList(Arrays.asList(tokens));

						// remove empty tokens from list
						Iterator iter = propertyTokens.iterator();
						while (iter.hasNext()) {
							String token = (String) iter.next();
							if (token.length() == 0){
								iter.remove();
							}
						}
					}
				} catch (BadLocationException e) {
				}
			}
			this.propertyTokens = propertyTokens;
		}

		private void guessPropertyName() {
			String propertyName = null;
			if (propertyTokens != null && !beanName.equals(name)) {
				Iterator iter = propertyTokens.iterator();
				while (iter.hasNext()) {
					String token = (String) iter.next();
					if (token.equals("name")) {
						if (iter.hasNext()) {
							token = (String) iter.next();

							// remove leading and trailing quote
							if (token.length() > 1 &&
											   token.charAt(0) == '"') {
								token = token.substring(1);
							}
							int pos = token.indexOf('"');
							if (pos != -1) {
								token = token.substring(0, pos);
							}
							propertyName = token;
						} else {
							propertyName = this.name;
						}
						break;
					}
				}
			}
			this.propertyName = propertyName;
		}

		public int searchBackward(IDocument doc, int startPosition,
								String findString) throws BadLocationException {
			if (findString == null || findString.length() == 0) {
				return -1;
			}

			if (startPosition < -1 || startPosition > doc.getLength()) {
				throw new BadLocationException();
			}

			char[] fs = new char[findString.length()];
			findString.getChars(0, fs.length, fs, 0);		

			// search backward
			if (startPosition == -1) {
				startPosition = doc.getLength();
			}

			return lastIndexOf(doc, fs, startPosition);
		}

		private int lastIndexOf(IDocument doc, char[] str, int fromIndex)
												   throws BadLocationException {
			if (fromIndex < 0) {
			    return -1;
			}

	   		int count = doc.getLength();
	   		int len = str.length;
			int rightIndex = count - len;

			if (fromIndex > rightIndex) {
			    fromIndex = rightIndex;
			}

			if (len == 0) {		// empty string always matches
			    return fromIndex;
			}

			int lastIndex = len - 1;
			char lastChar = str[lastIndex];
			int min = len - 1;
			int i = min + fromIndex;

		  restart:
			while (true) {

			    // Look for the last character
				while (i >= min && doc.getChar(i) != lastChar) {
					i--;
				}
	    
			    if (i < min) {
					return -1;
			    }

			    // Found last character
			    int j = i - 1;
			    int start = j - (len - 1);
			    int k = lastIndex - 1;

			    while (j > start) {
			        if (doc.getChar(j--) != str[k--]) {
				    	i--;
				    	continue restart;
					}
			    }

			    return start + 1;    /* Found whole string. */
			}
		}

		public String getBeanName() {
			return beanName;
		}

		public boolean hasBeanName() {
			return beanName != null;
		}

		public String getPropertyName() {
			return propertyName;
		}

		public boolean hasPropertyName() {
			return propertyName != null;
		}

		public String toString() {
			return "NodeGuesser: name=" + name + ", beanName=" + beanName +
				   ", propertyName=" + propertyName;
		}
	}
}
