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
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.NewSearchUI;
import org.springframework.ide.eclipse.beans.ui.actions.AbstractBeansConfigEditorAction;
import org.springframework.ide.eclipse.beans.ui.search.internal.BeansSearchScope;
import org.springframework.ide.eclipse.beans.ui.search.internal.queries.BeanReferenceQuery;
import org.springframework.util.StringUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;

/**
 * Starts a search for bean references with bean in current selection.
 * @see org.springframework.ide.eclipse.beans.ui.search.internal.queries.BeanReferenceQuery
 * @author Christian Dupuis
 */
public class SearchBeanReferencesAction extends AbstractBeansConfigEditorAction {
	public void run(IAction action) {
		if (getTextEditor() != null) {
			ISelection selection = getTextEditor().getSelectionProvider()
					.getSelection();
			String beanId = extractBeanId(selection);
			if (beanId != null) {
				ISearchQuery query = new BeanReferenceQuery(BeansSearchScope
						.newSearchScope(), beanId, true, true);
				NewSearchUI.activateSearchResultView();
				NewSearchUI.runQueryInBackground(query);
			}
		}
	}

	/**
	 * Extracts a regexp expression with the bean ID from the bean tag in the
	 * given selection's first element.
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

					// At first retrieve the bean's ID to start the reg exp
					Attr attribute = elem.getAttributeNode("id");
					if (attribute != null && attribute.getValue() != null) {
						beanId = attribute.getValue();
					}

					// Now retrieve the bean's alias names and add it to the
					// reg exp
					attribute = elem.getAttributeNode("name");
					if (attribute != null && attribute.getValue() != null) {
						String[] tokens = StringUtils.tokenizeToStringArray(
								attribute.getValue(), ",; ");
						for (int i = 0; i < tokens.length; i++) {
							if (beanId != null) {
								beanId += "|" + tokens[i];
							}
							else {
								beanId = tokens[i];
							}
						}
					}
				}
			}
		}
		return beanId;
	}
}
