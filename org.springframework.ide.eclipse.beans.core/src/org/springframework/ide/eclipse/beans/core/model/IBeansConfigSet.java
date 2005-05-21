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

import org.eclipse.core.resources.IFile;
import org.springframework.ide.eclipse.core.model.IModelElement;

/**
 * This interface provides information for a Spring beans config set (a list of
 * beans config <b>names</b>).
 */
public interface IBeansConfigSet extends IModelElement, IBeanClassAware {

	boolean isAllowBeanDefinitionOverriding();

	boolean isIncomplete();

	boolean hasConfig(String configName);

	/**
	 * Checks if the given file is registered within this config set.  
	 * <b>NOTE:</b> the file must be in the same project that this
	 * config set is defined in.  If it is not use the String
	 * variant of this method and use a workspace relative path.
	 * 
	 * @param file
	 * @return
	 */
	boolean hasConfig(IFile file);

	/**
	 * Returns a list of all config <b>names</b> defined in this config set. 
	 */
	Collection getConfigs();

	boolean hasBean(String name);

	IBean getBean(String name);

	/**
	 * Returns a list of all <code>IBean</code>s defined in this config set. 
	 */
	public Collection getBeans();	

	/**
     * Replace an existing config file with a new one.  
     * Strings are used rather than <code>IFile<code>'s as 
     * this allows the caller to determine whether they
     * should be using project or workspace relative paths.
     *
     * @param origFileName
     * @param newFileName
     */
    void replaceConfig(String origFileName, String newFileName);
}
