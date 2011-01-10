/*******************************************************************************
 * Copyright (c) 2006, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
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
 * @author Christian Dupuis
 * @author Torsten Juergeleit
 * @see org.springframework.ide.eclipse.beans.ui.search.internal.queries.BeanReferenceQuery
 */
public class SearchBeanReferencesAction extends AbstractBeansConfigEditorAction {
	@Override
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
						for (String element : tokens) {
							if (beanId != null) {
								beanId += "|" + element;
							}
							else {
								beanId = element;
							}
						}
					}
				}
			}
		}
		return beanId;
	}
}
