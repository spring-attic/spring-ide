/*
 * Copyright 2002-2004 the original author or authors.
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

package org.springframework.ide.eclipse.beans.core.internal.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.ChildBeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansModelElement;
import org.springframework.ide.eclipse.beans.core.model.IBeanConstructorArgument;
import org.springframework.ide.eclipse.beans.core.model.IBeanProperty;

/**
 * Parser data for a Spring bean.
 */
public class Bean extends BeansModelElement implements IBean {

	private BeanDefinition beanDefinition;
	private String[] aliases;
	private List constructorArguments;
	private List properties;

	public Bean(IBeansModelElement parent, String name) {
		super(parent, name);
		this.beanDefinition = null;
		this.constructorArguments = new ArrayList();
		this.properties = new ArrayList();
	}

	public int getElementType() {
		return BEAN;
	}

	public IResource getElementResource() {
		return getElementParent().getElementResource();
	}

	public IBeansConfig getConfig() {
		IBeansModelElement parent = getElementParent();
		if (parent instanceof IBeansConfig) {
			return (IBeansConfig) parent;
		} else if (parent instanceof IBean) {
			return ((IBean) parent).getConfig();
		}
		throw new IllegalStateException("Bean can only have a parent of type " +
										"IBeansConfig or IBean");
	}

	public void setBeanDefinition(BeanDefinition beanDefinition) {
		this.beanDefinition = beanDefinition;
	}

	public BeanDefinition getBeanDefinition() {
		return beanDefinition;
	}

	public void setAliases(String[] aliases) {
		this.aliases = aliases;
	}

	public String[] getAliases() {
		return aliases;
	}

	public void addConstructorArgument(IBeanConstructorArgument carg) {
		constructorArguments.add(carg);
	}

	public Collection getConstructorArguments() {
		return constructorArguments;
	}

	public boolean hasConstructorArguments() {
		return !constructorArguments.isEmpty();
	}

	public void addProperty(IBeanProperty property) {
		properties.add(property);
	}

	public boolean hasProperties() {
		return !properties.isEmpty();
	}

	public Collection getProperties() {
		return properties;
	}

	public String getClassName() {
		if (beanDefinition instanceof RootBeanDefinition) {
			return ((RootBeanDefinition) beanDefinition).getBeanClassName();
		}
		return null;
	}

	public String getParentName() {
		if (beanDefinition instanceof ChildBeanDefinition) {
			return ((ChildBeanDefinition) beanDefinition).getParentName();
		}
		return null;
	}

	public boolean isRootBean() {
		return (beanDefinition instanceof RootBeanDefinition);
	}

	public boolean isSingleton() {
		if (beanDefinition != null) {
			if (beanDefinition instanceof RootBeanDefinition) {
				return ((RootBeanDefinition) beanDefinition).isSingleton();
			} else {
				return ((ChildBeanDefinition) beanDefinition).isSingleton();
			}
		}
		return true;
	}

	/**
	 * Returns a collection of all <code>IBean</code>s which are referenced from
	 * within this property's value.
	 */
	public Collection getReferencedBeans() {
		Map refBeans = new HashMap();
		for (Iterator cargs = constructorArguments.iterator(); cargs.hasNext();) {
			IBeanConstructorArgument carg = (IBeanConstructorArgument) cargs.next();
			for (Iterator beans = carg.getReferencedBeans().iterator();
															 beans.hasNext();) {
				IBean bean = (IBean) beans.next();
				refBeans.put(bean.getElementName(), bean);
			}
		}
		for (Iterator props = properties.iterator(); props.hasNext();) {
			IBeanProperty prop = (IBeanProperty) props.next();
			for (Iterator beans = prop.getReferencedBeans().iterator();
															 beans.hasNext();) {
				IBean bean = (IBean) beans.next();
				if (!refBeans.containsKey(bean.getElementName())) {
					refBeans.put(bean.getElementName(), bean);
				}
			}
		}
		return refBeans.values();
	}

	public String toString() {
		StringBuffer text = new StringBuffer();
		text.append(getElementName());
		text.append(" (");
		text.append(getElementStartLine());
		text.append(')');
		if (getClassName() != null) {
			text.append(" [");
			text.append(getClassName());
			text.append(']');
		} else if (getParentName() != null) {
			text.append(" <");
			text.append(getParentName());
			text.append('>');
		}
		return text.toString();
	}
}
