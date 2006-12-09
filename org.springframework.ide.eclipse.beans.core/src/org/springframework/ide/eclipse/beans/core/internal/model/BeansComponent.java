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

package org.springframework.ide.eclipse.beans.core.internal.model;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.parsing.ComponentDefinition;
import org.springframework.beans.factory.parsing.CompositeComponentDefinition;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansComponent;
import org.springframework.ide.eclipse.beans.core.model.IBeansModelElementTypes;
import org.springframework.ide.eclipse.core.model.IModelElement;

/**
 * This class defines a Spring beans component defined via an XML namespace.
 * 
 * @author Torsten Juergeleit
 */
public class BeansComponent extends AbstractBeansModelElement implements
		IBeansComponent {

	/** List of all beans which are defined within this component */
	private Set<IBean> beans;

	/** List of all inner components which are defined within this component */
	private Set<IBeansComponent> components;

	private LinkedHashSet<IBean> innerBeans;

	public BeansComponent(IModelElement parent, ComponentDefinition definition) {
		super(parent, definition.getName(), definition);
		beans = new LinkedHashSet<IBean>();
		for (BeanDefinition beanDef : definition.getBeanDefinitions()) {
			if (beanDef.getRole() != BeanDefinition.ROLE_INFRASTRUCTURE) {
				IBean bean = new Bean(this, beanDef.getBeanClassName(),
						null, beanDef);
				beans.add(bean);
			}
		}

		components = new LinkedHashSet<IBeansComponent>();
		if (definition instanceof CompositeComponentDefinition) {
			for (ComponentDefinition compDef : ((CompositeComponentDefinition)
					definition).getNestedComponents()) {
				if (compDef instanceof BeanComponentDefinition) {
					if (compDef.getBeanDefinitions()[0].getRole()
							!= BeanDefinition.ROLE_INFRASTRUCTURE) {
						IBean bean = new Bean(this,
								(BeanComponentDefinition) compDef);
						beans.add(bean);
					}
				} else {
					IBeansComponent component = new BeansComponent(this,
							(ComponentDefinition) compDef);
					components.add(component);
				}
			}
		}

		innerBeans = new LinkedHashSet<IBean>();
		for (IBean bean : beans) {
			innerBeans.addAll(bean.getInnerBeans());
		}
		for (IBeansComponent comp : components) {
			innerBeans.addAll(comp.getInnerBeans());
		}
	}

	public int getElementType() {
		return IBeansModelElementTypes.COMPONENT_TYPE;
	}

	public IModelElement[] getElementChildren() {
		Set<IModelElement> children = new LinkedHashSet<IModelElement>(beans);
		children.addAll(components);
		return children.toArray(new IModelElement[children.size()]);
	}

	public Set<IBean> getBeans() {
		return Collections.unmodifiableSet(beans);
	}

	public Set<IBeansComponent> getComponents() {
		return Collections.unmodifiableSet(components);
	}

	public Set<IBean> getInnerBeans() {
		return Collections.unmodifiableSet(innerBeans);
	}
}
