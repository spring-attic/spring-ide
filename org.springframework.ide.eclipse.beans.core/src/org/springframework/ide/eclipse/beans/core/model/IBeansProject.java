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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.springframework.ide.eclipse.core.model.IResourceModelElement;

/**
 * This interface provides information for a Spring Beans project.
 * 
 * @author Torsten Juergeleit
 */
public interface IBeansProject extends IResourceModelElement, IBeanClassAware {

	/** File name of the Spring Beans project description */
	public static final String DESCRIPTION_FILE = ".springBeans";

	/** Default file extension for beans config files */
	public static final String DEFAULT_CONFIG_EXTENSION = "xml";

	/**
	 * Returns corresponding Eclipse project. 
	 */
	IProject getProject();

	/**
	 * Returns a list of file extensions for <code>IBeansConfig</code> files. 
	 */
	Set<String> getConfigExtensions();

	/**
	 * Returns true if given config extension belongs to the list of Spring
	 * bean config file extensions which are stored in the project description. 
	 */
	boolean hasConfigExtension(String extension);

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
	 * Returns a collection of all configs defined in this project.
	 */
	Set<IBeansConfig> getConfigs();

	/**
	 * Returns true if a config set with the given name is defined within this
	 * project.
	 */
	boolean hasConfigSet(String configSetName);

	/**
	 * Returns <code>IBeansConfigSet</code> of given name.
	 */
	public IBeansConfigSet getConfigSet(String configSetName);

	/**
	 * Returns a list of config sets defined within this project.
	 */
	Set<IBeansConfigSet> getConfigSets();
}
