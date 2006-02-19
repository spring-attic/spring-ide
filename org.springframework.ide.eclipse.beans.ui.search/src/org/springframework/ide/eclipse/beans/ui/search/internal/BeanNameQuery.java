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

package org.springframework.ide.eclipse.beans.ui.search.internal;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.util.Assert;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.ide.eclipse.beans.core.internal.model.Bean;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.ui.search.BeansSearchPlugin;
import org.springframework.ide.eclipse.core.model.IModelElement;

/**
 * This implementation of <code>ISearchQuery</code> looks for all
 * <code>IBean</code>s which ID or alias names match a given name.
 *
 * @see org.eclipse.search.ui.ISearchQuery
 * @see org.springframework.ide.eclipse.beans.core.model.IBean
 *
 * @author Torsten Juergeleit
 */
public class BeanNameQuery extends AbstractBeansQuery {

	private String beanName;

	public BeanNameQuery(BeansSearchScope scope, String beanName) {
		super(scope);
		Assert.isNotNull(beanName);
		this.beanName = beanName;
	}

	public String getLabel() {
		Object[] args = new Object[] { beanName,
									   getSearchScope().getDescription() };
		return BeansSearchPlugin.getFormattedMessage("NameSearch.label",
													 args);
	}

	protected boolean doesMatch(IModelElement element,
								IProgressMonitor monitor) {
		if (element instanceof IBean) {
			Bean bean = (Bean) element;
			BeanDefinitionHolder bdh = bean.getBeanDefinitionHolder();

			// Compare bean name first
			if (beanName.equals(bdh.getBeanName())) {
				return true;
			}

			// Now compare aliases
			String[] aliases = bdh.getAliases();
			if (aliases != null) {
				for (int i = 0; i < aliases.length; i++) {
					if (beanName.equals(aliases[i])) {
						return true;
					}
					
				}
			}
		}
		return false;
	}
}
