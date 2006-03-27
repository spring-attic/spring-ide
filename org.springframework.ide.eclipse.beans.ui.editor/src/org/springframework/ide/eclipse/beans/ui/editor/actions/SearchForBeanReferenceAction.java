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

package org.springframework.ide.eclipse.beans.ui.editor.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.texteditor.ITextEditor;
import org.springframework.ide.eclipse.beans.ui.search.internal.BeansSearchScope;
import org.springframework.ide.eclipse.beans.ui.search.internal.queries.BeanReferenceQuery;
import org.springframework.util.StringUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;

/**
 * Implementation of <code>IEditorActionDelegate</code> thats starts a search for 
 * bean references.
 * @see org.springframework.ide.eclipse.beans.ui.search.internal.queries.BeanReferenceQuery 
 * 
 * @author Christian Dupuis
 *
 */
public class SearchForBeanReferenceAction implements IEditorActionDelegate, 
	IWorkbenchWindowActionDelegate {
	
	private IEditorPart fEditor;

	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		fEditor = targetEditor;
	}

	/**
	 * Helper to return the ITextEditor
	 * @return the textEditor instance
	 */
	private ITextEditor getTextEditor() {
		ITextEditor textEditor = null;
		if (fEditor instanceof ITextEditor) {
			textEditor = (ITextEditor) fEditor;
		} else {
			if (fEditor != null) {
				Object o = fEditor.getAdapter(ITextEditor.class);
				if (o instanceof ITextEditor)
					textEditor = (ITextEditor) o;
			}
		}
		return textEditor;
	}
	
	/**
	 * The impementation of the action
	 */
	public void run(IAction action) {
		ITextEditor textEditor = getTextEditor();
		if (textEditor != null) {
			// get current selection
			ISelection selection = textEditor.getSelectionProvider()
					.getSelection();
			String beanId = this.extractBeanId(selection);
			if (beanId != null) {
				NewSearchUI.activateSearchResultView();
				NewSearchUI.runQueryInBackground(new BeanReferenceQuery(
						BeansSearchScope.newSearchScope(),
						beanId, true, true));
			}
		}
	}

	/**
	 * Called during change of selection
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		action.setEnabled((this.extractBeanId(selection) != null));
	}

	/**
	 * Extracts an regexp expression form the bean tag for the search 
	 * @param selection the current text selection
	 * @return a string containing the pattern
	 */
	private String extractBeanId(ISelection selection) {
		String beanId = null;
		if (selection instanceof IStructuredSelection) {

			IStructuredSelection structSelection = (IStructuredSelection) selection;
			Object obj = structSelection.getFirstElement();
			if (obj instanceof Element) {
				Element elem = (Element) obj;
				String tagName = elem.getTagName();

				if ("bean".equals(tagName)) {
					Attr attribute = elem.getAttributeNode("id");
					if (attribute != null && attribute.getValue() != null) {
						beanId = attribute.getValue();
					}
					attribute = elem.getAttributeNode("name");
					if (attribute != null && attribute.getValue() != null) {
						if (beanId != null) {
							beanId += "|";
						}
						String[] tokens = StringUtils.tokenizeToStringArray(attribute.getValue(), ",; ");
						for (int i = 0; i < tokens.length; i++) {
							beanId += tokens[i];
							if ((i + 1) < tokens.length) {
								beanId += "|";
							}
						}
					}
				}
			}
		}
		return beanId;
	}

	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	public void init(IWorkbenchWindow window) {
		IEditorPart editor;
		IWorkbenchPage page = window.getActivePage();
		if (page != null) {
			editor = page.getActiveEditor();
		} else {
			editor = null;
		}
		setActiveEditor(null, editor);		
	}

}
