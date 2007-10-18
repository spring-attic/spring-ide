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
package org.springframework.ide.eclipse.beans.core.internal.project;

/**
 * This interface defines the string constants for the XML tags of the
 * Spring Beans project's description file.
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 */
public interface IBeansProjectDescriptionConstants {
	
	String PROJECT_DESCRIPTION = "beansProjectDescription";
	String CONFIG_EXTENSIONS = "configExtensions";
	String CONFIG_EXTENSION = "configExtension";
	String CONFIG_SUFFIXES = "configSuffixes";
	String CONFIG_SUFFIX = "configSuffix";
	String CONFIGS = "configs";
	String CONFIG = "config";
	String FILE = "file";
	String CONFIG_SETS = "configSets";
	String OVERRIDING = "allowBeanDefinitionOverriding";
	String INCOMPLETE = "incomplete";
	String CONFIG_SET = "configSet";
	String NAME = "name";
	
	String PLUGIN_VERSION = "pluginVersion";
	String VERSION = "version";
	String CURRENT_VERSION = "1";
}
