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

package org.springframework.ide.eclipse.core.beans;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.DefaultXmlBeanDefinitionParser;

/**
 * Default implementation of the <code>BeanDefinitionRegistry</code>
 * interface: a full-fledged bean factory based on bean definitions.
 *
 * <p>Typical usage is registering all bean definitions first (possibly read
 * from a bean definition file), before accessing beans. Bean definition lookup
 * is therefore an inexpensive operation in a local bean definition table.
 *
 * <p>Can be used as a standalone bean factory, or as a superclass for custom
 * bean factories. Note that readers for specific bean definition formats are
 * typically implemented separately rather than as bean factory subclasses.
 *
 * <p><b>Creation of bean instances is not supported!!!</b>
 *
 * @see org.springframework.ide.eclipse.beans.core.internal.parser.EventBeanDefinitionRegistry 
 * @see org.springframework.ide.eclipse.beans.core.internal.model.validator.BeansConfigValidator
 */
public class DefaultBeanDefinitionRegistry extends AbstractBeanFactory
											implements BeanDefinitionRegistry {
	/** Whether to allow re-registration of a different definition with the
	 *  same name */
	private boolean allowBeanDefinitionOverriding = true;

	/** Whether to allow re-registration of a different alias with the same
	 *  name */
	private boolean allowAliasOverriding = true;

	/** Map of bean definition objects, keyed by bean name */
	private final Map beanDefinitionMap = new HashMap();

	/** List of bean definition names, in registration order */
	private final List beanDefinitionNames = new ArrayList();

	/**
	 * Create a new DefaultBeanDefinitionRegistry.
	 */
	public DefaultBeanDefinitionRegistry() {
	}

	/**
	 * Create a new DefaultBeanDefinitionRegistry with the given parent.
	 * @param parentBeanFactory parent bean factory, or null if none
	 * @see #getBean
	 */
	public DefaultBeanDefinitionRegistry(BeanFactory parentBeanFactory) {
		super(parentBeanFactory);
	}

	/**
	 * Set if it should be allowed to override bean definitions by registering a
	 * different definition with the same name, automatically replacing the
	 * former. If not, an exception will be thrown. Default is true.
	 */
	public void setAllowBeanDefinitionOverriding(
										boolean allowBeanDefinitionOverriding) {
		this.allowBeanDefinitionOverriding = allowBeanDefinitionOverriding;
	}

	/**
	 * Set if it should be allowed to override aliases by registering a
	 * different alias with the same name, automatically replacing the
	 * former. If not, an exception will be thrown. Default is true.
	 */
	public void setAllowAliasOverriding(boolean allowAliasOverriding) {
		this.allowAliasOverriding = allowAliasOverriding;
	}

	public int getBeanDefinitionCount() {
		return beanDefinitionMap.size();
	}

	public String[] getBeanDefinitionNames() {
		return (String[]) beanDefinitionNames.toArray(
									   new String[beanDefinitionNames.size()]);
	}

	public boolean containsBeanDefinition(String beanName) {
		String transformedBeanName = transformedBeanName(beanName);
		return beanDefinitionMap.containsKey(transformedBeanName);
	}

	/**
	 * Return a RootBeanDefinition for the given bean name, by merging with the
	 * parent if the given original bean definition is a child bean definition.
	 * @param beanName the name of the bean definition
	 * @return a merged RootBeanDefinition with overridden properties
	 */
	public BeanDefinition getBeanDefinition(String beanName)
														throws BeansException {
		String transformedBeanName = transformedBeanName(beanName);
		BeanDefinition bd = (BeanDefinition)
									beanDefinitionMap.get(transformedBeanName);
		if (bd == null) {
			throw new NoSuchBeanDefinitionException(beanName, toString());
		}
		return getMergedBeanDefinition(transformedBeanName, bd);
	}

	public void registerBeanDefinition(String beanName,
			BeanDefinition beanDefinition) throws BeanDefinitionStoreException {
		Object oldBeanDefinition = beanDefinitionMap.get(beanName);
		if (oldBeanDefinition != null) {
			if (!allowBeanDefinitionOverriding) {
				throw new BeanDefinitionStoreException("Overrides bean '" +
													   oldBeanDefinition + "'");
			}
		} else {
			beanDefinitionNames.add(beanName);
		}
		beanDefinitionMap.put(beanName, beanDefinition);
	}

	public void registerAlias(String beanName, String alias)
										  throws BeanDefinitionStoreException {
		try {
			super.registerAlias(beanName, alias);
		} catch (IllegalArgumentException e) {
			throw new BeanDefinitionStoreException(
								  DefaultXmlBeanDefinitionParser.ALIAS_ELEMENT,
								  beanName, e.getMessage());
		} catch (BeanDefinitionStoreException e) {
			if (!allowAliasOverriding) {
				throw e;
			}
		}
	}

	public Object createBean(String beanName,
							 RootBeanDefinition mergedBeanDefinition,
							 Object[] args) throws BeansException {
		throw new BeanCreationException(beanName, "Not implemented");
	}

	public String toString() {
		StringBuffer text = new StringBuffer();
		Iterator bdNames = beanDefinitionNames.iterator();
		while (bdNames.hasNext()) {
			text.append(bdNames.next());
			if (bdNames.hasNext()) {
				text.append(", ");
			}
		}
		return text.toString();
	}
}
