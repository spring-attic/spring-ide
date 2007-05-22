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

public interface IWebflowProject extends IPersistableWebflowModelElement {

	String DESCRIPTION_FILE = ".springWebflow";

	List<IWebflowConfig> getConfigs();

	void setConfigs(List<IWebflowConfig> configs);

	IProject getProject();

	IWebflowConfig getConfig(IFile file);

}
