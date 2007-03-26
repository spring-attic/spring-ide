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
package org.springframework.ide.eclipse.webflow.core.model;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;

/**
 * 
 */
public interface IWebflowProject {

	/**
	 * 
	 */
	String DESCRIPTION_FILE = ".springWebflow";

	/**
	 * 
	 * 
	 * @return
	 */
	List<IWebflowConfig> getConfigs();

	/**
	 * 
	 * 
	 * @param configs
	 */
	void setConfigs(List<IWebflowConfig> configs);

	/**
	 * 
	 * 
	 * @return
	 */
	IProject getProject();

	/**
	 * 
	 * 
	 * @param file
	 * 
	 * @return
	 */
	IWebflowConfig getConfig(IFile file);

}
