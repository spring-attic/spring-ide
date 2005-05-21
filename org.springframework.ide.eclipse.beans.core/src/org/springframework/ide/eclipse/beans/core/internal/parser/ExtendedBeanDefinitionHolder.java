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

package org.springframework.ide.eclipse.beans.core.internal.parser;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.ChildBeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * Wrapper for BeanDefinition holder with our own version of
 * <code>toString()</code>.
 */
public class ExtendedBeanDefinitionHolder extends BeanDefinitionHolder {

	public ExtendedBeanDefinitionHolder(BeanDefinitionHolder bdHolder) {
		super(bdHolder.getBeanDefinition(), bdHolder.getBeanName(),
			  bdHolder.getAliases());
	}

	public ExtendedBeanDefinitionHolder(BeanDefinition beanDefinition,
										String beanName) {
		super(beanDefinition, beanName);
	}

	public ExtendedBeanDefinitionHolder(BeanDefinition beanDefinition,
										String beanName, String[] aliases) {
		super(beanDefinition, beanName, aliases);
	}

	public String toString() {
		StringBuffer text = new StringBuffer();
		if (getBeanName() != null) {
			text.append(getBeanName());
		}
		BeanDefinition bd = getBeanDefinition();
		if (bd instanceof RootBeanDefinition) {
			RootBeanDefinition rootBd = (RootBeanDefinition) bd;
			if (rootBd.getBeanClassName() != null) {
				text.append(" [");
				text.append(rootBd.getBeanClassName());
				text.append(']');
			}
		} else if (bd instanceof ChildBeanDefinition) {
			ChildBeanDefinition childBd = (ChildBeanDefinition) bd;
			if (childBd.getParentName() != null) {
				text.append(" <");
				text.append(childBd.getParentName());
				text.append('>');
			}
		}
		return text.toString();
	}
}
