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

package org.springframework.ide.eclipse.beans.core.internal.model;

import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.beans.factory.support.ChildBeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.core.model.IModelSourceLocation;
import org.springframework.ide.eclipse.core.model.ModelUtils;
import org.springframework.util.ObjectUtils;

/**
 * {@link BeanNameGenerator} which creates a bean name which is unique within
 * the beans core model. This name consists of the bean class or parent name or
 * object identity, the project name, the config file name and the start line
 * number delimited by '#'.
 * 
 * @author Torsten Juergeleit
 * @since 2.0
 */
public class UniqueBeanNameGenerator implements BeanNameGenerator {

	private IBeansConfig config;

	public UniqueBeanNameGenerator(IBeansConfig config) {
		this.config = config;
	}

	public String generateBeanName(BeanDefinition definition,
			BeanDefinitionRegistry registry) {
		return generateBeanName(definition, config);
	}

	public static String generateBeanName(BeanDefinition definition,
			IBeansConfig config) {
		StringBuffer name = new StringBuffer();
		if (definition instanceof RootBeanDefinition) {
			name.append(((RootBeanDefinition) definition).getBeanClassName());
		}
		else if (definition instanceof ChildBeanDefinition) {
			name.append('<');
			name.append(((ChildBeanDefinition) definition).getParentName());
			name.append('>');
		} else {
			name.append(BeanFactoryUtils.GENERATED_BEAN_NAME_SEPARATOR);
			ObjectUtils.getIdentityHexString(definition);
		}
		IModelSourceLocation location = ModelUtils
				.getSourceLocation(definition);
		if (location != null) {
			name.append(BeanFactoryUtils.GENERATED_BEAN_NAME_SEPARATOR);
			name.append(config.getElementParent().getElementName());
			name.append(BeanFactoryUtils.GENERATED_BEAN_NAME_SEPARATOR);
			name.append(config.getElementName());
			name.append(BeanFactoryUtils.GENERATED_BEAN_NAME_SEPARATOR);
			name.append(location.getStartLine());
		}
		return name.toString();
	}
}
