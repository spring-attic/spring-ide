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
import org.eclipse.core.resources.IProject;

/**
 * This interface holds information for a Spring Beans project.
 */
public interface IBeansProject extends IBeansModelElement, IBeanClassAware {

	/** File name of the Spring Beans project description */
	public static final String DESCRIPTION_FILE = ".springBeansProject";

	/**
	 * Returns corresponding Eclipse project. 
	 */
	IProject getProject();

	/**
	 * Retruns a list of config names.
	 */
	Collection getConfigNames();

	/**
	 * Returns true if given file belongs to the list of Spring bean config
	 * files which are stored in the project description. 
	 */
	boolean hasConfig(IFile file);

	/**
	 * Returns true if given config name belongs to the list of Spring bean
	 * config files which are stored in the project description. 
	 */
	boolean hasConfig(String configName);

	/**
	 * Returns <code>IBeansConfig</code> for given config file. 
	 */
	IBeansConfig getConfig(IFile configFile);

	/**
	 * Returns <code>IBeansConfig</code> of given name. 
	 */
	IBeansConfig getConfig(String configName);

	/**
	 * Returns a collection of all <code>IBeansConfig</code>s defined in this
	 * project.
	 * @see org.springframework.ide.eclipse.beans.core.model.IBeansConfig
	 */
	Collection getConfigs();

	/**
	 * Returns a list of <code>IBeansConfigSet</code> instances.
	 * @see org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet
	 */
	Collection getConfigSets();

	/**
	 * Returns true if a config set with the given name is defined within this
	 * project.
	 * @param name
	 * @return
	 */
	boolean hasConfigSet(String configSetName);
}
