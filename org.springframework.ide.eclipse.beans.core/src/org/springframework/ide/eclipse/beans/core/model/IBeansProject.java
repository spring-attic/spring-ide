/*******************************************************************************
 * Copyright (c) 2005, 2008 Spring IDE Developers
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
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.core.model.IResourceModelElement;

/**
 * This interface provides information for a Spring Beans project.
 * 
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 */
public interface IBeansProject extends IBeansModelElement,
		IResourceModelElement, IBeanClassAware {

	/** File name of the Spring Beans project description */
	String DESCRIPTION_FILE = ".settings/" + BeansCorePlugin.PLUGIN_ID + ".xml";

	/** File name of the Spring Beans project description pre 2.5.2 */
	String DESCRIPTION_FILE_OLD = ".springBeans";

	/** Default file extension for beans config files */
	String DEFAULT_CONFIG_SUFFIX = "xml";
	
	/** Default specifying if imports should be processed */
	boolean DEFAULT_IMPORTS_ENABLED = false;

	/**
	 * Returns corresponding Eclipse project.
	 */
	IProject getProject();

	/**
	 * Returns a list of file suffixes for <code>IBeansConfig</code> files.
	 * @since 2.0.2
	 */
	Set<String> getConfigSuffixes();

	/**
	 * Returns a list of file extensions for <code>IBeansConfig</code> files.
	 * @deprecated use {@link #getConfigSuffixes()} instead.
	 */
	@Deprecated
	Set<String> getConfigExtensions();

	/**
	 * Returns true if given config suffix belongs to the list of Spring bean
	 * config file suffixes which are stored in the project description.
	 * @since 2.0.2
	 */
	boolean hasConfigSuffix(String suffix);

	/**
	 * Returns true if given config extension belongs to the list of Spring bean
	 * config file extensions which are stored in the project description.
	 * @deprecated use {@link #hasConfigSuffix(String)} instead.
	 */
	@Deprecated
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
	 * Returns <code>IBeansConfig</code> for the given config file. Includes
	 * imported {@link IImportedBeansConfig} in the search if
	 * <code>includeImported</code> is true.
	 * @since 2.0.3
	 */
	IBeansConfig getConfig(IFile configFile, boolean includeImported);
	
	/**
	 * Returns all <code>IBeansConfig</code> for the given config file. Includes
	 * imported {@link IImportedBeansConfig} in the search if
	 * <code>includeImported</code> is true.
	 * @since 2.0.3
	 */
	Set<IBeansConfig> getConfigs(IFile configFile, boolean includeImported);

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

	/**
	 * Returns true of this project should process imports in
	 * {@link IBeansConfig}.
	 * @return true if imports should be processed for this project
	 * @since 2.0.3
	 */
	boolean isImportsEnabled();
	
	/**
	 * Returns true if this project's settings can be changed 
	 * @return true if this project can be changed.
	 * @since 2.0.3
	 * @deprecated this is now correctly handled internally using the team provider API
	 */
	@Deprecated
	boolean isUpdatable();

}
