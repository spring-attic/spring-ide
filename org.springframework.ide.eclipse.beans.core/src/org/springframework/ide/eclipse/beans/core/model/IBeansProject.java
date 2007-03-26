/*******************************************************************************
 * Copyright (c) 2005, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
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
