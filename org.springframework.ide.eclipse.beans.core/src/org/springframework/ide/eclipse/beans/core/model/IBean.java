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

package org.springframework.ide.eclipse.beans.core.model;

import java.util.Collection;

import org.springframework.beans.factory.config.BeanDefinition;

/**
 * Holds all data of a Spring bean.
 */
public interface IBean extends IBeansModelElement {

	IBeansConfig getConfig();

	BeanDefinition getBeanDefinition();

	String[] getAliases();

	Collection getConstructorArguments();

	boolean hasConstructorArguments();

	boolean hasProperties();

	Collection getProperties();

	String getClassName();

	String getParentName();

	public boolean isRootBean();

	public boolean isSingleton();

	/**
	 * Returns a collection of all <code>IBean</code>s which are referenced from
	 * within this property's value.
	 */
	public Collection getReferencedBeans();
}
