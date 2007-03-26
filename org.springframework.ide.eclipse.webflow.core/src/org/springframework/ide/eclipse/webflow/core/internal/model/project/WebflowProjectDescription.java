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
package org.springframework.ide.eclipse.webflow.core.internal.model.project;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.springframework.ide.eclipse.webflow.core.internal.model.WebflowConfig;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowConfig;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowProject;

/**
 * 
 */
public class WebflowProjectDescription {

	/**
	 * 
	 */
	private IWebflowProject project;

	/**
	 * 
	 */
	private List<IWebflowConfig> configs;

	/**
	 * 
	 * 
	 * @param project
	 */
	public WebflowProjectDescription(IWebflowProject project) {
		this.project = project;
		this.configs = new ArrayList<IWebflowConfig>();
	}

	/**
	 * 
	 * 
	 * @param file
	 * 
	 * @return
	 */
	public IWebflowConfig addConfig(IFile file) {
		IWebflowConfig config = new WebflowConfig(project);
		config.setResource(file);
		configs.add(config);
		return config;
	}

	/**
	 * 
	 * 
	 * @param file
	 */
	public void add(IWebflowConfig file) {
		configs.add(file);
	}

	/**
	 * 
	 * 
	 * @return
	 */
	public List<IWebflowConfig> getConfigs() {
		return configs;
	}

	/**
	 * 
	 * 
	 * @param file
	 */
	public void removeConfig(IFile file) {
		IWebflowConfig config = null;
		for (IWebflowConfig c : configs) {
			if (c.getResource().equals(file)) {
				config = c;
			}
		}
		if (config != null) {
			configs.remove(config);
		}
	}

	/**
	 * 
	 * 
	 * @param configs
	 */
	public void setConfigs(List<IWebflowConfig> configs) {
		this.configs = configs;
	}
}
