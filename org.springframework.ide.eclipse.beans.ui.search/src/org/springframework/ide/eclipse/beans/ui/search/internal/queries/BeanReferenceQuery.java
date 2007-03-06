/*
 * Copyright 2002-2007 the original author or authors.
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
import org.eclipse.search.ui.ISearchQuery;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.LookupOverride;
import org.springframework.beans.factory.support.MethodOverride;
import org.springframework.beans.factory.support.ReplaceOverride;
import org.springframework.ide.eclipse.beans.core.internal.model.Bean;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeanAlias;
import org.springframework.ide.eclipse.beans.core.model.IBeanProperty;
import org.springframework.ide.eclipse.beans.core.model.IBeanReference;
import org.springframework.ide.eclipse.beans.core.model.IBeansList;
import org.springframework.ide.eclipse.beans.core.model.IBeansMap;
import org.springframework.ide.eclipse.beans.core.model.IBeansMapEntry;
import org.springframework.ide.eclipse.beans.core.model.IBeansSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansTypedString;
import org.springframework.ide.eclipse.beans.core.model.IBeansValueHolder;
import org.springframework.ide.eclipse.beans.ui.search.internal.BeansSearchMessages;
import org.springframework.ide.eclipse.beans.ui.search.internal.BeansSearchScope;
import org.springframework.ide.eclipse.core.MessageUtils;
import org.springframework.ide.eclipse.core.model.IModelElement;

/**
 * This {@link ISearchQuery} looks for all {@link IBeanAlias}es or
 * {@link IBean}s which are referencing a given bean.
 * 
 * @author Torsten Juergeleit
 */
public class BeanReferenceQuery extends AbstractBeansQuery {

	public static final String PROXY_FACTORY_CLASS_NAME =
			"org.springframework.aop.framework.ProxyFactoryBean";

	public BeanReferenceQuery(BeansSearchScope scope, String pattern,
			boolean isCaseSensitive, boolean isRegexSearch) {
		super(scope, pattern, isCaseSensitive, isRegexSearch);
	}

	public String getLabel() {
		Object[] args = new Object[] { getPattern(),
				getScope().getDescription() };
		return MessageUtils.format(
				BeansSearchMessages.SearchQuery_searchFor_reference, args);
	}

	protected boolean doesMatch(IModelElement element, Pattern pattern,
			IProgressMonitor monitor) {
		if (element instanceof IBeanAlias) {
			IBeanAlias alias = (IBeanAlias) element;
			if (pattern.matcher(alias.getBeanName()).matches()) {
				return true;
			}
		}
		else if (element instanceof IBean) {
			IBean bean = (IBean) element;

			// Compare reference with parent bean
			if (bean.isChildBean()
					&& pattern.matcher(bean.getParentName()).matches()) {
				return true;
			}
			AbstractBeanDefinition bd = (AbstractBeanDefinition)
					((Bean) element).getBeanDefinition();

			// Compare reference with factory bean
			String factoryBeanName = bd.getFactoryBeanName();
			if (factoryBeanName != null
					&& pattern.matcher(factoryBeanName).matches()) {
				return true;
			}

			// Compare reference with depends-on beans
			String dependsOnBeanNames[] = bd.getDependsOn();
			if (dependsOnBeanNames != null) {
				for (int i = 0; i < dependsOnBeanNames.length; i++) {
					String name = dependsOnBeanNames[i];
					if (pattern.matcher(name).matches()) {
						return true;
					}
				}
			}

			// Compare reference with method-override beans
			if (!bd.getMethodOverrides().isEmpty()) {
				Iterator methodsOverrides = bd.getMethodOverrides()
						.getOverrides().iterator();
				while (methodsOverrides.hasNext()) {
					MethodOverride methodOverride =
							(MethodOverride) methodsOverrides.next();
					if (methodOverride instanceof LookupOverride) {
						String name = ((LookupOverride) methodOverride)
								.getBeanName();
						if (pattern.matcher(name).matches()) {
							return true;
						}
					}
					else if (methodOverride instanceof ReplaceOverride) {
						String name = ((ReplaceOverride) methodOverride)
								.getMethodReplacerBeanName();
						if (pattern.matcher(name).matches()) {
							return true;
						}
					}
				}
			}
		}
		else if (element instanceof IBeansValueHolder) {
			return doesValueMatch(element, ((IBeansValueHolder) element)
					.getValue(), pattern);
		}
		return false;
	}

	private boolean doesValueMatch(IModelElement element, Object value,
			Pattern pattern) {
		if (value instanceof IBeanReference) {
			String name = ((IBeanReference) value).getBeanName();
			if (pattern.matcher(name).matches()) {
				return true;
			}
		}
		else if (value instanceof IBeansList) {

			// Compare reference with bean property's interceptors
			if (element instanceof IBeanProperty
					&& element.getElementName().equals("interceptorNames")) {
				String beanClass = BeansModelUtils.getBeanClass((IBean) element
						.getElementParent(), null);
				if (PROXY_FACTORY_CLASS_NAME.equals(beanClass)) {
					for (IModelElement child : ((IBeansList) value)
							.getElementChildren()) {
						if (child instanceof IBeansTypedString) {
							if (pattern.matcher(((IBeansTypedString) child)
									.getString()).matches()) {
								return true;
							}
						}
					}
				}
			}
			else {
				for (IModelElement child : ((IBeansList) value)
						.getElementChildren()) {
					if (doesValueMatch(element, child, pattern)) {
						return true;
					}
				}
			}
		}
		else if (value instanceof IBeansSet) {
			for (IModelElement child : ((IBeansSet) value)
					.getElementChildren()) {
				if (doesValueMatch(element, child, pattern)) {
					return true;
				}
			}
		}
		else if (value instanceof IBeansMap) {
			for (IModelElement child : ((IBeansMap) value)
					.getElementChildren()) {
				if (child instanceof IBeansMapEntry) {
					if (doesValueMatch(element, ((IBeansMapEntry) child)
							.getKey(), pattern)
							|| doesValueMatch(element, ((IBeansMapEntry) child)
									.getValue(), pattern)) {
						return true;
					}
				}
			}
		}
		return false;
	}
}
