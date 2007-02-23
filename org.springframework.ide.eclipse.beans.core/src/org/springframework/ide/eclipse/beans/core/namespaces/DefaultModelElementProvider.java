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

package org.springframework.ide.eclipse.beans.core.namespaces;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.parsing.ComponentDefinition;
import org.springframework.beans.factory.parsing.CompositeComponentDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.ide.eclipse.beans.core.internal.model.Bean;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansComponent;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansComponent;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.core.model.ISourceModelElement;

/**
 * This class is an {@link IModelElementProvider} which converts a given
 * {@link CompositeComponentDefinition} into a single {@link IBean} or an
 * {@link IBeansComponent} containing {@link IBean}(s) or
 * {@link IBeansComponent}(s).
 * 
 * @author Torsten Juergeleit
 */
public class DefaultModelElementProvider implements IModelElementProvider {

	public ISourceModelElement getElement(IBeansConfig config,
			ComponentDefinition definition) {
		if (definition instanceof CompositeComponentDefinition
				|| definition.getBeanDefinitions().length > 1) {
			return createComponent(config, definition);
		}
		return createBean(config, definition);
	}

	private IBeansComponent createComponent(IBeansConfig config,
			ComponentDefinition definition) {
		BeansComponent component = new BeansComponent(config, definition);

		// Create beans from wrapped bean definitions
		for (BeanDefinition beanDef : definition.getBeanDefinitions()) {
			if (beanDef instanceof AbstractBeanDefinition && beanDef
					.getRole() != BeanDefinition.ROLE_INFRASTRUCTURE) {
				String beanName = BeanDefinitionReaderUtils
						.generateBeanName((AbstractBeanDefinition) beanDef,
								null, true);
				IBean bean = new Bean(component, beanName, null, beanDef);
				component.addBean(bean);
			}
		}

		// Create components or beans component definitions
		if (definition instanceof CompositeComponentDefinition) {
			for (ComponentDefinition compDef : ((CompositeComponentDefinition)
					definition).getNestedComponents()) {
				if (compDef instanceof CompositeComponentDefinition
						|| compDef.getBeanDefinitions().length > 1) {
					component.addComponent(createComponent(config, compDef));
				} else {
					IBean bean = createBean(config, compDef);
					if (bean != null) {
						component.addBean(bean);
					}
				}
			}
		}

		// Handle inner beans
		for (IBean bean : component.getBeans()) {
			component.addInnerBeans(bean.getInnerBeans());
		}
		for (IBeansComponent comp : component.getComponents()) {
			component.addInnerBeans(comp.getInnerBeans());
		}
		return component;
	}

	private IBean createBean(IBeansConfig config,
			ComponentDefinition definition) {
		BeanDefinition[] beanDefs = definition.getBeanDefinitions();
		if (beanDefs.length > 0 && beanDefs[0].getRole()
				!= BeanDefinition.ROLE_INFRASTRUCTURE) {
			BeanDefinitionHolder holder;
			if (definition instanceof BeanComponentDefinition) {
				holder = (BeanComponentDefinition) definition;
			} else {
				holder = new BeanDefinitionHolder(definition
						.getBeanDefinitions()[0], definition.getName());
			}
			return new Bean(config, holder);
		}
		return null;
	}
}
