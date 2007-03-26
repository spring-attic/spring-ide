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
package org.springframework.ide.eclipse.beans.ui.search.internal.queries;

import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.ui.search.internal.BeansSearchMessages;
import org.springframework.ide.eclipse.beans.ui.search.internal.BeansSearchScope;
import org.springframework.ide.eclipse.core.MessageUtils;
import org.springframework.ide.eclipse.core.model.IModelElement;

/**
 * This {@link ISearchQuery} looks for all {@link IBean}s which are a child of
 * a parent with given name.
 * 
 * @author Torsten Juergeleit
 */
public class BeanChildQuery extends AbstractBeansQuery {

	public BeanChildQuery(BeansSearchScope scope, String pattern,
			boolean isCaseSensitive, boolean isRegexSearch) {
		super(scope, pattern, isCaseSensitive, isRegexSearch);
	}

	public String getLabel() {
		Object[] args = new Object[] { getPattern(),
				getScope().getDescription() };
		return MessageUtils.format(
				BeansSearchMessages.SearchQuery_searchFor_child, args);
	}

	@Override
	protected boolean doesMatch(IModelElement element, Pattern pattern,
			IProgressMonitor monitor) {
		if (element instanceof IBean) {
			IBean bean = (IBean) element;
			if (bean.isChildBean()) {

				// Compare given parent bean's name with bean's one
				if (pattern.matcher(bean.getParentName()).matches()) {
					return true;
				}
			}
		}
		return false;
	}
}
