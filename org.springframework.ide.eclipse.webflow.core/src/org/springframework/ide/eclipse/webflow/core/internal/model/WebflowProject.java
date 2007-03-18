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

package org.springframework.ide.eclipse.webflow.core.internal.model;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.springframework.ide.eclipse.webflow.core.internal.model.project.WebflowProjectDescription;
import org.springframework.ide.eclipse.webflow.core.internal.model.project.WebflowProjectDescriptionReader;
import org.springframework.ide.eclipse.webflow.core.internal.model.project.WebflowProjectDescriptionWriter;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowConfig;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModel;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowProject;

/**
 * 
 * 
 * @author Christian Dupuis
 * @since 2.0
 */
public class WebflowProject implements IWebflowProject {

	/**
	 * 
	 */
	private final IProject project;

	private final IWebflowModel model;

	/**
	 * 
	 */
	private WebflowProjectDescription description;

	/**
	 * 
	 * 
	 * @param project
	 */
	public WebflowProject(IProject project, IWebflowModel model) {
		this.project = project;
		this.model = model;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.webflow.core.model.IWebflowProject#getConfigs()
	 */
	public List<IWebflowConfig> getConfigs() {
		return getDescription().getConfigs();
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.webflow.core.model.IWebflowProject#getProject()
	 */
	public IProject getProject() {
		return this.project;
	}

	/**
	 * Returns lazily loaded project description. <b>This nature's project has
	 * to be set first!!! </b>
	 * 
	 * @return
	 */
	private WebflowProjectDescription getDescription() {
		if (description == null) {
			description = WebflowProjectDescriptionReader.read(this);
		}
		return description;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.webflow.core.model.IWebflowProject#setConfigs(java.util.List)
	 */
	public void setConfigs(List<IWebflowConfig> configs) {
		WebflowProjectDescription description = getDescription();
		description.setConfigs(configs);
		WebflowProjectDescriptionWriter.write(project, description);

		model.fireModelChangedEvent(this);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.webflow.core.model.IWebflowProject#getConfig(org.eclipse.core.resources.IFile)
	 */
	public IWebflowConfig getConfig(IFile file) {
		List<IWebflowConfig> configs = getDescription().getConfigs();
		if (configs != null) {
			for (IWebflowConfig config : configs) {
				if (config.getResource().equals(file)) {
					return config;
				}
			}
		}
		return null;
	}
}