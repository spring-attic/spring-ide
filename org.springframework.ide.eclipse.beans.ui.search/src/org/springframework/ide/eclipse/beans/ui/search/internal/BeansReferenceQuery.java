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

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.LookupOverride;
import org.springframework.beans.factory.support.MethodOverride;
import org.springframework.beans.factory.support.ReplaceOverride;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeanConstructorArgument;
import org.springframework.ide.eclipse.beans.core.model.IBeanProperty;
import org.springframework.ide.eclipse.beans.ui.search.BeansSearchPlugin;
import org.springframework.ide.eclipse.core.model.IModelElement;

/**
 * @author Torsten Juergeleit
 */
public class BeansReferenceQuery extends AbstractBeansQuery {

	private String reference;

	public BeansReferenceQuery(BeansSearchScope scope, String reference) {
		super(scope);
		this.reference = reference;
	}

	public String getReference() {
		return reference;
	}

	protected boolean doesMatch(IModelElement element,
								IProgressMonitor monitor) {
		if (element instanceof IBean) {
			IBean bean = (IBean) element;

			// Compare reference with parent bean
			if (!bean.isRootBean() && reference.equals(bean.getParentName())) {
				return true;
			}
			AbstractBeanDefinition bd = (AbstractBeanDefinition)
									   BeansModelUtils.getBeanDefinition(bean);
			// Compare reference with factory bean
			if (reference.equals(bd.getFactoryBeanName())) {
				return true;
			}

			// Compare reference with depends-on beans
			String dependsOnBeanNames[] = bd.getDependsOn();
			if (dependsOnBeanNames != null) {
				for (int i = 0; i < dependsOnBeanNames.length; i++) {
					String beanName = dependsOnBeanNames[i];
					if (reference.equals(beanName)) {
						return true;
					}
				}
			}

			// Compare reference with method-override beans
			if (!bd.getMethodOverrides().isEmpty()) {
				Iterator methodsOverrides =
							 bd.getMethodOverrides().getOverrides().iterator();
				while (methodsOverrides.hasNext()) {
					MethodOverride methodOverride = (MethodOverride)
													   methodsOverrides.next();
					if (methodOverride instanceof LookupOverride) {
						String beanName = ((LookupOverride)
												 methodOverride).getBeanName();
						if (reference.equals(beanName)) {
							return true;
						}
					} else if (methodOverride instanceof ReplaceOverride) {
						String beanName = ((ReplaceOverride)
								 methodOverride).getMethodReplacerBeanName();
						if (reference.equals(beanName)) {
							return true;
						}
					}
				}
			}
		} else if (element instanceof IBeanConstructorArgument) {
			return doesValueMatch(element, ((IBeanConstructorArgument)
														  element).getValue());
		} else if (element instanceof IBeanProperty) {
			return doesValueMatch(element, ((IBeanProperty)
														  element).getValue());
		}
		return false;
	}

	private boolean doesValueMatch(IModelElement element, Object value) {
		if (value instanceof RuntimeBeanReference) {
			String beanName = ((RuntimeBeanReference) value).getBeanName();
			if (reference.equals(beanName)) {
				return true;
			}
		} else if (value instanceof List) {

			// Compare reference with bean property's interceptors
			if (element instanceof IBeanProperty &&
						 element.getElementName().equals("interceptorNames")) {
				String beanClass = BeansModelUtils.getBeanClass((IBean)
											 element.getElementParent(), null);
				if ("org.springframework.aop.framework.ProxyFactoryBean".
														   equals(beanClass)) {
					Iterator names = ((List) value).iterator();
					while (names.hasNext()) {
						Object name = (Object) names.next();
						if (name instanceof String) {
							if (reference.equals(name)) {
								return true;
							}
						}
					}
				}
			} else {
				List list = (List) value;
				for (int i = 0; i < list.size(); i++) {
					return doesValueMatch(element, list.get(i));
				}
			}
		} else if (value instanceof Set) {
			Set set = (Set) value;
			for (Iterator iter = set.iterator(); iter.hasNext(); ) {
				return doesValueMatch(element, iter.next());
			}
		} else if (value instanceof Map) {
			Map map = (Map) value;
			for (Iterator iter = map.keySet().iterator(); iter.hasNext(); ) {
				return doesValueMatch(element, map.get(iter.next()));
			}
		}
		return false;
	}

	public String getLabel() {
		Object[] args = new Object[] { reference,
									   getSearchScope().getDescription() };
		return BeansSearchPlugin.getFormattedMessage("ReferenceSearch.label",
													 args);
	}
}
