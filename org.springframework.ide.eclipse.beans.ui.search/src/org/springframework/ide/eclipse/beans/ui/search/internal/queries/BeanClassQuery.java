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
 * This {@link ISearchQuery} looks for all {@link IBean}s which class name
 * matches the given class name.
 * 
 * @author Torsten Juergeleit
 */
public class BeanClassQuery extends AbstractBeansQuery {

	public BeanClassQuery(BeansSearchScope scope, String pattern,
			boolean isCaseSensitive, boolean isRegexSearch) {
		super(scope, pattern, isCaseSensitive, isRegexSearch);
	}

	public String getLabel() {
		Object[] args = new Object[] { getPattern(),
				getScope().getDescription() };
		return MessageUtils.format(
				BeansSearchMessages.SearchQuery_searchFor_class, args);
	}

	@Override
	protected boolean doesMatch(IModelElement element, Pattern pattern,
			IProgressMonitor monitor) {
		if (element instanceof IBean) {
			String className = ((IBean) element).getClassName();
			if (className != null) {

				// Compare given class name with bean's one
				if (pattern.matcher(className).matches()) {
					return true;
				}
			}
		}
		return false;
	}
}
