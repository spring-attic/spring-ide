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

	private final String projectNameToken;

	private final String actualProjectName;

	private final List<TemplateInputCollector> inputHandlers;

	private final SpringVersion springVersion;

	/**
	 * 
	 * @param projectNameToken this is the token for the default name of a
	 * project as defined in a template, NOT the actual project name entered by
	 * the user
	 * @param template
	 * @param projectLocationURI
	 * @param inputHandlers
	 * @param springVersion
	 */
	public TemplateProjectConfigurationDescriptor(String projectNameToken, String actualProjectName,
			String[] topLevelPackageTokens, Template template, URI projectLocationURI,
			List<TemplateInputCollector> inputHandlers, SpringVersion springVersion) {
		this.template = template;
		this.topLevelPackageTokens = topLevelPackageTokens;
		this.projectNameToken = projectNameToken;
		this.actualProjectName = actualProjectName;
		this.projectLocationURI = projectLocationURI;
		this.inputHandlers = inputHandlers;
		this.springVersion = springVersion;
	}

	public String[] getTopLevelPackageTokens() {
		return topLevelPackageTokens;
	}

	/**
	 * This is NOT the actual project name as entered by a user, but rather the
	 * original project name defined in the template, which is obtained from the
	 * template's wizard json file.
	 * @return
	 */
	public String getProjectNameToken() {
		return projectNameToken;
	}

	/**
	 * This is the actual project name that gets created, and that substitutes
	 * the project name token during project configuration.
	 * @return
	 */
	public String getActualProjectName() {
		return actualProjectName;
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
