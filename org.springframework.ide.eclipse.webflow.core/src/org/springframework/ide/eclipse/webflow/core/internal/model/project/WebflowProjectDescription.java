/*
 * Copyright 2002-2007 the original author or authors.
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