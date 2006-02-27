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

package org.springframework.ide.eclipse.beans.ui.search.internal.queries;

import java.util.Iterator;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeanProperty;
import org.springframework.ide.eclipse.beans.ui.search.internal.BeansSearchMessages;
import org.springframework.ide.eclipse.beans.ui.search.internal.BeansSearchScope;
import org.springframework.ide.eclipse.core.MessageUtils;
import org.springframework.ide.eclipse.core.model.IModelElement;

/**
 * This implementation of <code>ISearchQuery</code> looks for all
 * <code>IBean</code>s which have a property of given name.
 *
 * @see org.eclipse.search.ui.ISearchQuery
 * @see org.springframework.ide.eclipse.beans.core.model.IBean
 *
 * @author Torsten Juergeleit
 */
public class BeanPropertyQuery extends AbstractBeansQuery {

	public BeanPropertyQuery(BeansSearchScope scope, String pattern,
						  boolean isCaseSensitive, boolean isRegexSearch) {
		super(scope, pattern, isCaseSensitive, isRegexSearch);
	}

	public String getLabel() {
		Object[] args = new Object[] { getPattern(),
									   getScope().getDescription() };
		return MessageUtils.format(
						BeansSearchMessages.SearchQuery_searchFor_class, args);
	}

	protected boolean doesMatch(IModelElement element, Pattern pattern,
								IProgressMonitor monitor) {
		if (element instanceof IBean) {
			IBean bean = (IBean) element;
			Iterator properties = bean.getProperties().iterator();
			while (properties.hasNext()) {
				IBeanProperty property = (IBeanProperty) properties.next();
				
				// Compare given property name with bean's one
				if (pattern.matcher(property.getElementName()).matches()) {
					return true;
				}
			}
		}
		return false;
	}
}
