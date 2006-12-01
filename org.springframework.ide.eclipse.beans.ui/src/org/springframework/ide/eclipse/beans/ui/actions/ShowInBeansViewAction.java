/*
 * Copyright 2002-2006 the original author or authors.
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

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.cheatsheets.ICheatSheetAction;
import org.eclipse.ui.cheatsheets.ICheatSheetManager;
import org.eclipse.ui.texteditor.ITextEditor;
import org.springframework.ide.eclipse.beans.ui.views.BeansViewLocation;
import org.springframework.ide.eclipse.ui.SpringUIUtils;

/**
 * Shows the currently selected bean or property in Spring beans view.
 * @author Torsten Juergeleit
 */
public class ShowInBeansViewAction extends AbstractBeansConfigEditorAction
												 implements ICheatSheetAction {
	public void run(IAction action) {
		if (getTextEditor() != null && getConfigFile() != null) {
			BeansViewLocation location = guessBeansViewLocation(
											 getTextEditor(), getConfigFile());
			if (location != null) {
				location.show();
			}
		}
	}

	public void run(String[] params, ICheatSheetManager manager) {
		if (params != null) {
			BeansViewLocation location = new BeansViewLocation();
			if (params.length > 1) {
				location.setProjectName(params[0]);
			}
			if (params.length > 2) {
				location.setConfigName(params[1]);
			}
			if (params.length > 3) {
				location.setBeanName(params[2]);
			}
			if (params.length > 4) {
				location.setPropertyName(params[3]);
			}
			location.show();
		}
	}

	private BeansViewLocation guessBeansViewLocation(ITextEditor editor,
													 IFile file) {
		BeansViewLocation location = new BeansViewLocation();
		location.setProjectName(file.getProject().getName());
		location.setConfigName(file.getProjectRelativePath().toString());

		int caretOffset = SpringUIUtils.getCaretOffset(editor);
		IDocument doc = editor.getDocumentProvider().getDocument(
													   editor.getEditorInput());
		BeansViewLocationGuesser guesser = new BeansViewLocationGuesser(doc,
																   caretOffset);
		if (guesser.hasBeanName()) {
			location.setBeanName(guesser.getBeanName());
			if (guesser.hasPropertyName()) {
				location.setPropertyName(guesser.getPropertyName());
			}
		}
		return location;
	}

	private class BeansViewLocationGuesser {

		private IDocument doc;
		private int caretOffset;
		private int nameOffset;
		private String name;
		private int beanOffset;
		private Set<String> beanTokens;
		private String beanName;
		private int propertyOffset;
		private Set<String> propertyTokens;
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
			Set<String> beanTokens = null;
			if (nameOffset != -1) {
				try {
					beanOffset = searchBackward(doc, nameOffset, "<bean");
					if (beanOffset != -1) {
						String beanText = doc.get(beanOffset, nameOffset
								- beanOffset - 1);
						String[] tokens = beanText.split("[=\\s]");
						beanTokens = new LinkedHashSet<String>(Arrays
								.asList(tokens));

						// remove empty tokens from list
						Iterator iter = beanTokens.iterator();
						while (iter.hasNext()) {
							String token = (String) iter.next();
							if (token.length() == 0) {
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
							if (token.length() > 1 && token.charAt(0) == '"') {
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
			Set<String> propertyTokens = null;
			if (nameOffset != -1) {
				try {
					propertyOffset = searchBackward(doc, nameOffset,
							"<property");
					if (propertyOffset > beanOffset) {
						String propertyText = doc.get(propertyOffset,
								nameOffset - propertyOffset - 1);
						String[] tokens = propertyText.split("[=\\s]");
						propertyTokens = new LinkedHashSet<String>(Arrays
								.asList(tokens));

						// remove empty tokens from list
						Iterator iter = propertyTokens.iterator();
						while (iter.hasNext()) {
							String token = (String) iter.next();
							if (token.length() == 0) {
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
							if (token.length() > 1 && token.charAt(0) == '"') {
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
