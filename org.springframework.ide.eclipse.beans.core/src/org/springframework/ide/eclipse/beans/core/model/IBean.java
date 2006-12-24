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

import org.springframework.ide.eclipse.core.model.ISourceModelElement;

/**
 * Holds all data of a Spring bean.
 * 
 * @author Torsten Juergeleit
 */
public interface IBean extends ISourceModelElement {

	/**
	 * Returns the name of the parent bean (in case of a child bean) or null
	 * (in case of a root bean).
	 */
	String getParentName();

	String getClassName();

	public boolean isRootBean();

	public boolean isChildBean();

	public boolean isSingleton();

	public boolean isAbstract();

	public boolean isLazyInit();

	public boolean isFactory();

	String[] getAliases();

	Set<IBeanConstructorArgument> getConstructorArguments();

	IBeanProperty getProperty(String name);

	Set<IBeanProperty> getProperties();

	Set<IBean> getInnerBeans();
}
