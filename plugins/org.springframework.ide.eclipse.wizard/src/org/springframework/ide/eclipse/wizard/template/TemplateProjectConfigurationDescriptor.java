/*******************************************************************************
 *  Copyright (c) 2013 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.wizard.template;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.springframework.ide.eclipse.wizard.template.infrastructure.Template;

public class TemplateProjectConfigurationDescriptor {

	private final Template template;

	private final URI projectLocationURI;

	private final String[] topLevelPackageTokens;

	private final String projectName;

	private final List<TemplateInputCollector> inputHandlers;

	private final SpringVersion springVersion;

	public TemplateProjectConfigurationDescriptor(String projectName, String[] topLevelPackageTokens,
			Template template, URI projectLocationURI, List<TemplateInputCollector> inputHandlers,
			SpringVersion springVersion) {
		this.template = template;
		this.topLevelPackageTokens = topLevelPackageTokens;
		this.projectName = projectName;
		this.projectLocationURI = projectLocationURI;
		this.inputHandlers = inputHandlers;
		this.springVersion = springVersion;
	}

	public String[] getTopLevelPackageTokens() {
		return topLevelPackageTokens;
	}

	public String getProjectName() {
		return projectName;
	}

	public Template getTemplate() {
		return template;
	}

	public SpringVersion getSpringVersion() {
		return springVersion;
	}

	/**
	 * 
	 * @return Non-null list of input handlers for templates that require user
	 * input for template variables. Return empty list if template does not have
	 * template variables.
	 */
	public List<TemplateInputCollector> getInputHandlers() {
		return inputHandlers != null ? inputHandlers : new ArrayList<TemplateInputCollector>(0);
	}

	public URI getProjectLocationPath() {
		return projectLocationURI;
	}

}
