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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.io.Resource;

/**
 * Implementation of <code>BeanDefinitionRegistry</code> that reads bean
 * definitions from an XML file. Delegates to
 * <code>EventBeanDefinitionReader</code> underneath.
 *
 * @see EventBeanDefinitionReader
 */
public class EventBeanFactory extends AbstractBeanFactory
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
	private final List beanDefinitionNames = new LinkedList();

	private EventBeanDefinitionReader reader;

	public EventBeanFactory(IBeanDefinitionEvents eventHandler) {
		this(eventHandler, null);
	}

	public EventBeanFactory(IBeanDefinitionEvents eventHandler,
							BeanFactory parentBeanFactory) {
		super(parentBeanFactory);
		reader = new EventBeanDefinitionReader(this, eventHandler);
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

	/**
	 * Load bean definitions from the specified XML file.
	 * @param resource the resource descriptor for the XML file
	 * @throws BeansException in case of loading or parsing errors
	 */
	public int loadBeanDefinitions(Resource resource) throws BeansException {
		return reader.loadBeanDefinitions(resource);
	}

	public int getBeanDefinitionCount() {
		return beanDefinitionMap.size();
	}

	public String[] getBeanDefinitionNames() {
		return (String[]) beanDefinitionNames.toArray(
									   new String[beanDefinitionNames.size()]);
	}

	public boolean containsBeanDefinition(String beanName) {
		return beanDefinitionMap.containsKey(beanName);
	}

	public BeanDefinition getBeanDefinition(String beanName)
														throws BeansException {
		BeanDefinition bd = (BeanDefinition) beanDefinitionMap.get(beanName);
		if (bd == null) {
			throw new NoSuchBeanDefinitionException(beanName, toString());
		}
		return bd;
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
		} catch (BeanDefinitionStoreException e) {
			if (!allowAliasOverriding) {
				throw e;
			}
		}
	}

	public Object createBean(String beanName,
							 RootBeanDefinition mergedBeanDefinition,
							 Object[] args) throws BeansException {
		return null;
	}
}
