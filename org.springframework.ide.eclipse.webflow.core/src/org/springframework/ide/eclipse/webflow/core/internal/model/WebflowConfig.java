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

import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansModel;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowConfig;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElementVisitor;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowProject;

/**
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public class WebflowConfig implements IWebflowConfig {

	/**
	 * 
	 */
	private Set<String> beansConfigs = new HashSet<String>();

	/**
	 * 
	 */
	private IFile resource;

	private String name;

	/**
	 * 
	 */
	@SuppressWarnings("unused")
	private IWebflowProject project;

	public IWebflowProject getProject() {
		return project;
	}

	/**
	 * @param project
	 */
	public WebflowConfig(IWebflowProject project) {
		this.project = project;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.webflow.core.model.IWebflowConfig#getBeansConfigs()
	 */
	public java.util.Set<IBeansConfig> getBeansConfigs() {
		IBeansModel model = BeansCorePlugin.getModel();

		java.util.Set<IBeansConfig> configs = new HashSet<IBeansConfig>();
		if (beansConfigs != null) {
			for (String configName : this.beansConfigs) {
				IBeansConfig config = (IBeansConfig) model
						.getElement(configName);
				if (config != null) {
					configs.add(config);
				}
			}

		}
		return configs;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.webflow.core.model.IWebflowConfig#getResource()
	 */
	public IFile getResource() {
		return this.resource;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.webflow.core.model.IWebflowConfig#setBeansConfigs(java.util.List)
	 */
	public void setBeansConfigs(java.util.Set<IBeansConfig> beansConfigs) {
		this.beansConfigs = new HashSet<String>();
		if (beansConfigs != null) {
			for (IBeansConfig config : beansConfigs) {
				if (BeansModelUtils.getProject(config) != null
						&& BeansModelUtils.getProject(config).getProject()
								.equals(project.getProject())) {
					this.beansConfigs.add(config.getElementID());
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.webflow.core.model.IWebflowConfig#setResource(org.eclipse.core.resources.IFile)
	 */
	public void setResource(IFile file) {
		this.resource = file;

		if (this.name == null && this.resource != null) {
			int i = this.resource.getName().lastIndexOf('.');
			if (i > 0) {
				this.name = this.resource.getName().substring(0, i);
			}
			else {
				this.name = this.resource.getName();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.webflow.core.model.IWebflowConfig#setBeansConfigsElementIds(java.util.List)
	 */
	public void setBeansConfigsElementIds(java.util.Set<String> beansConfigs) {
		this.beansConfigs = beansConfigs;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.webflow.core.model.IWebflowConfig#addBeansConfigElementId(java.lang.String)
	 */
	public void addBeansConfigElementId(String id) {
		this.beansConfigs.add(id);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/** */
	public void accept(IWebflowModelElementVisitor visitor,
			IProgressMonitor monitor) {

	}

	public void addPropertyChangeListener(PropertyChangeListener l) {
	}


	public void fireStructureChange(String prop, Object child) {
	}

	
	public IWebflowModelElement getElementParent() {
		return null;
	}


	public int getElementStartLine() {
		return 0;
	}

	public IDOMNode getNode() {
		return null;
	}
	

	public void init(IDOMNode node, IWebflowModelElement parent) {
	}

	public void removePropertyChangeListener(PropertyChangeListener l) {
	}


	public void setElementParent(IWebflowModelElement parent) {
	}
}
