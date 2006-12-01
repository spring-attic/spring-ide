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

package org.springframework.ide.eclipse.beans.core.model;

import java.util.Set;

/**
 * Interface to be implemented by model elements that know about
 * <code>IBean</code>s and their full-qualified class name.
 * @author Torsten Juergeleit
 */
public interface IBeanClassAware {

	/**
	 * Returns <code>true</code> if given full qualified class name is a bean
	 * class used within this Beans model element. 
	 */
	boolean isBeanClass(String className);

	Set<String> getBeanClasses();

	/**
	 * Returns a list of beans which are using the given class as their bean
	 * class.
	 * @param className  full qualified name of bean class
	 */
	Set<IBean> getBeans(String className);
}