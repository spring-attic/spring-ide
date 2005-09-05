/*
 * Copyright 2002-2005 the original author or authors.
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

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.io.Resource;
import org.springframework.ide.eclipse.beans.core.DefaultBeanDefinitionRegistry;

/**
 * Implementation of <code>BeanDefinitionRegistry</code> that reads bean
 * definitions from an XML file. Delegates to
 * <code>EventBeanDefinitionReader</code> underneath.
 *
 * @see EventBeanDefinitionReader
 */
public class EventBeanDefinitionRegistry extends DefaultBeanDefinitionRegistry {

	private EventBeanDefinitionReader reader;

	public EventBeanDefinitionRegistry(IBeanDefinitionEvents eventHandler) {
		this(eventHandler, null);
	}

	public EventBeanDefinitionRegistry(IBeanDefinitionEvents eventHandler,
									   BeanFactory parentBeanFactory) {
		super(parentBeanFactory);
		reader = new EventBeanDefinitionReader(this, eventHandler);
	}

	/**
	 * Load bean definitions from the specified XML file.
	 * @param resource the resource descriptor for the XML file
	 * @throws BeansException in case of loading or parsing errors
	 */
	public int loadBeanDefinitions(Resource resource) throws BeansException {
		return reader.loadBeanDefinitions(resource);
	}

	/**
	 * Load bean definitions from the specified resources.
	 * @param resources the resource descriptors
	 * @return the number of bean definitions found
	 * @throws BeansException in case of loading or parsing errors
	 */
	public int loadBeanDefinitions(Resource[] resources) throws BeansException {
		return reader.loadBeanDefinitions(resources);
	}
}
