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
package org.springframework.ide.eclipse.beans.core.internal.model;

import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.core.model.IModelSourceLocation;
import org.springframework.ide.eclipse.core.model.ModelUtils;

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
		if (definition.getBeanClassName() != null) {
			name.append(definition.getBeanClassName());
		}
		else {
			if (definition.getParentName() != null) {
				name.append(definition.getParentName());
				name.append("$child");
			}
			else if (definition.getFactoryBeanName() != null) {
				name.append(definition
						.getFactoryBeanName());
				name.append("$created");
			} else {
				name.append("!!!invalid name!!!");
			}
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
