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

import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.w3c.dom.Element;

/**
 * Defines events for Spring bean definition parser. 
 */
public interface IBeanDefinitionEvents {

	void startBean(Element element, boolean isNestedBean);

	void registerBean(BeanDefinitionHolder bdHolder, boolean isNestedBean);

	void startConstructorArgument(Element element);

	void registerConstructorArgument(int index, Object value, String type);

	void startProperty(Element element);

	void registerProperty(String propertyName, PropertyValues pvs);
}
