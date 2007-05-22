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
package org.springframework.ide.eclipse.webflow.core.internal.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.springframework.ide.eclipse.webflow.core.internal.model.project.WebflowProjectDescription;
import org.springframework.ide.eclipse.webflow.core.internal.model.project.WebflowProjectDescriptionReader;
import org.springframework.ide.eclipse.webflow.core.internal.model.project.WebflowProjectDescriptionWriter;
import org.springframework.ide.eclipse.webflow.core.model.IPersistableWebflowModelElement;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowConfig;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModel;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowProject;

/**
 * @author Christian Dupuis
 * @since 2.0
 */
public class WebflowProject extends AbstractPersistableWebflowModelElement
		implements IWebflowProject {

	private final IProject project;

	private final IWebflowModel model;

	private WebflowProjectDescription description;

	public WebflowProject(IProject project, IWebflowModel model) {
		this.project = project;
		this.model = model;
	}

	public List<IWebflowConfig> getConfigs() {
		return getDescription().getConfigs();
	}

	public IProject getProject() {
		return this.project;
	}

	private WebflowProjectDescription getDescription() {
		if (description == null) {
			description = WebflowProjectDescriptionReader.read(this);
		}
		return description;
	}

	public void setConfigs(List<IWebflowConfig> configs) {
		WebflowProjectDescription description = getDescription();
		description.setConfigs(configs);
		WebflowProjectDescriptionWriter.write(project, description);

		model.fireModelChangedEvent(this);
	}

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

	public String getElementName() {
		return this.project.getName();
	}

	public int getElementType() {
		return PROJECT;
	}

	public IPersistableWebflowModelElement getPersistableElementParent() {
		return this.model;
	}

	public Set<IPersistableWebflowModelElement> getElementChildren() {
		Set<IPersistableWebflowModelElement> children = new HashSet<IPersistableWebflowModelElement>();
		children.addAll(this.getConfigs());
		return children;
	}

	public Object getAdapter(Class adapter) {
		return null;
	}
}
