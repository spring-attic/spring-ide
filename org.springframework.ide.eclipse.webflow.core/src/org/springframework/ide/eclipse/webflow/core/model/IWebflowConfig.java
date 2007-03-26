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

import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;

/**
 * 
 */
public interface IWebflowConfig extends IWebflowModelElement {

	/**
	 * 
	 * 
	 * @param file
	 */
	void setResource(IFile file);

	/**
	 * 
	 * 
	 * @return
	 */
	IFile getResource();

	/**
	 * 
	 * 
	 * @return
	 */
	Set<IBeansConfig> getBeansConfigs();

	/**
	 * 
	 * 
	 * @param beansConfigs
	 */
	void setBeansConfigs(Set<IBeansConfig> beansConfigs);

	/**
	 * 
	 * 
	 * @param beansConfigs
	 */
	void setBeansConfigsElementIds(Set<String> beansConfigs);

	/**
	 * 
	 * 
	 * @param id
	 */
	void addBeansConfigElementId(String id);

	IWebflowProject getProject();

	void setName(String name);

	String getName();

}
