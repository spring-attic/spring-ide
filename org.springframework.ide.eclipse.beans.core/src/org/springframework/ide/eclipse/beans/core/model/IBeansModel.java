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
import org.springframework.ide.eclipse.core.model.IModel;

/**
 * The <code>IBeansModel</code> manages instances of
 * <code>IBeansProject</code>s. <code>IBeansModelChangedListener</code>s
 * register with the <code>IBeansModel</code> and receive
 * <code>BeansModelChangedEvent</code>s for all changes.
 * <p>
 * The single instance of <code>IBeansModel</code> is available from the
 * static method <code>BeansCorePlugin.getModel()</code>.
 * 
 * @author Torsten Juergeleit
 */
public interface IBeansModel extends IModel {

	String ELEMENT_NAME = "BeansModel";

	/**
	 * Returns the beans project for given project.
	 * @param project  the project a beans project is requested for
	 */
	IBeansProject getProject(IProject project);

	/**
	 * Returns the beans project for given project name or full-qualified
	 * config file name (including project name).
	 * <br>
	 * External config files (with a leading '/') are handled too.  
	 * @param name  the name of a project or a full-qualified config file name
	 */
	IBeansProject getProject(String name);

	/**
	 * Returns a collection of all projects defined in this model.
	 */
	Set<IBeansProject> getProjects();

	/**
	 * Returns the beans config for given config file.
	 * @param configFile  the config file a beans config is requested for
	 */
	IBeansConfig getConfig(IFile configFile);

	/**
	 * Returns the beans config for given full-qualified config file name
	 * (including project name).
	 * <br>
	 * External config files (with a leading '/') are handled too.  
	 * @param name  the name of a config file a beans config is requested for
	 */
	IBeansConfig getConfig(String configName);

	/**
	 * Returns a list of all configs which contain a bean with given bean
	 * class.
	 */
	Set<IBeansConfig> getConfigs(String className);
}
