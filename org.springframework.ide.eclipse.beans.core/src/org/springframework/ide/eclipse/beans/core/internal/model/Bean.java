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
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.ChildBeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeanConstructorArgument;
import org.springframework.ide.eclipse.beans.core.model.IBeanProperty;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansModelElement;

/**
 * Parser data for a Spring bean.
 */
public class Bean extends BeansModelElement implements IBean {

	private BeanDefinitionHolder beanDefinitionHolder;
	private List constructorArguments;
	private List properties;
	private List innerBeans;
	private IBean outerBean;

	public Bean(IBeansConfig config) {
		super(config, null);   // the name we get from the BeanDefinitionHolder
		this.constructorArguments = new ArrayList();
		this.properties = new ArrayList();
		this.innerBeans = new ArrayList();
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
		} else if (parent instanceof IBeanProperty) {
			return ((IBean) parent.getElementParent()).getConfig();
		}
		throw new IllegalStateException("Bean can only have a parent of type " +
			 "IBeansConfig, IBean or (in case of an inner bean) IBeanProperty");
	}

	public void setBeanDefinitionHolder(
									BeanDefinitionHolder beanDefinitionHolder) {
		this.beanDefinitionHolder = beanDefinitionHolder;
		setElementName(beanDefinitionHolder.getBeanName());
	}

	public BeanDefinitionHolder getBeanDefinitionHolder() {
		return beanDefinitionHolder;
	}

	public String[] getAliases() {
		return beanDefinitionHolder.getAliases();
	}

	public void addConstructorArgument(IBeanConstructorArgument carg) {
		constructorArguments.add(carg);
	}

	public Collection getConstructorArguments() {
		return constructorArguments;
	}

	public void addProperty(IBeanProperty property) {
		properties.add(property);
	}

	public Collection getProperties() {
		return properties;
	}

	public void addInnerBean(Bean bean) {
		innerBeans.add(bean);
	}

	public Collection getInnerBeans() {
		return innerBeans;
	}

	public String getClassName() {
		BeanDefinition beanDef = beanDefinitionHolder.getBeanDefinition();
		if (beanDef instanceof AbstractBeanDefinition) {
			return ((AbstractBeanDefinition) beanDef).getBeanClassName();
		}
		return null;
	}

	public String getParentName() {
		BeanDefinition beanDef = beanDefinitionHolder.getBeanDefinition();
		if (beanDef instanceof ChildBeanDefinition) {
			return ((ChildBeanDefinition) beanDef).getParentName();
		}
		return null;
	}

	public boolean isRootBean() {
		return (beanDefinitionHolder.getBeanDefinition() instanceof
															RootBeanDefinition);
	}

	public boolean isSingleton() {
		BeanDefinition beanDef = beanDefinitionHolder.getBeanDefinition();
		if (beanDef instanceof RootBeanDefinition) {
			return ((RootBeanDefinition) beanDef).isSingleton();
		} else if (beanDef instanceof ChildBeanDefinition){
			return ((ChildBeanDefinition) beanDef).isSingleton();
		}
		return true;
	}

	public boolean isAbstract() {
		return beanDefinitionHolder.getBeanDefinition().isAbstract();
	}

	public boolean isLazyInit() {
		return beanDefinitionHolder.getBeanDefinition().isLazyInit();
	}

	public boolean isInnerBean() {
		return (getElementParent() != null);
	}

	/**
	 * Returns a collection with the names of all beans which are referenced
	 * by this bean's parent bean (for child beans only), constructor arguments
	 * or properties.
	 */
	public Collection getReferencedBeans() {
		List beanNames = new ArrayList();

		// For a child bean add the names of all parent beans and all beans
		// which are referenced by the parent beans
		for (IBean bean = this; bean != null && !bean.isRootBean(); ) {
			String parentName = bean.getParentName();
			if (parentName != null) {
				beanNames.add(parentName);
				bean = ((IBeansConfig) getElementParent()).getBean(parentName);
				if (bean != null) {
					BeansModelUtil.addReferencedBeanNamesForBean(bean,
																 beanNames);
				}
			}
		}

		// Add names of referenced beans from constructor arguments
		Iterator cargs = constructorArguments.iterator();
		while (cargs.hasNext()) {
			IBeanConstructorArgument carg = (IBeanConstructorArgument)
																   cargs.next();
			Iterator beans = carg.getReferencedBeans().iterator();
			while (beans.hasNext()) {
				String beanName = (String) beans.next();
				if (!beanNames.contains(beanName)) {
					beanNames.add(beanName);
				}
			}
		}

		// Add referenced beans from properties
		Iterator props = properties.iterator();
		while (props.hasNext()) {
			IBeanProperty prop = (IBeanProperty) props.next();
			Iterator beans = prop.getReferencedBeans().iterator();
			while (beans.hasNext()) {
				String beanName = (String) beans.next();
				if (!beanNames.contains(beanName)) {
					beanNames.add(beanName);
				}
			}
		}
		return beanNames;
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
