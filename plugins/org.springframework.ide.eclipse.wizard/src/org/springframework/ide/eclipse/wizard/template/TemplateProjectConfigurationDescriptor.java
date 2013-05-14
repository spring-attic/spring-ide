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
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.springframework.ide.eclipse.wizard.template.infrastructure.Template;

public class TemplateProjectConfigurationDescriptor implements IProjectConfigurationDescriptor {

	private final Template template;

	private final URI projectLocationURI;

	private final String[] topLevelPackageTokens;

	private final String projectName;

	private final Map<String, Object> collectedInput;

	private final Map<String, String> inputKinds;

	public TemplateProjectConfigurationDescriptor(String projectName, String[] topLevelPackageTokens,
			Template template, URI projectLocationURI) {
		this(projectName, topLevelPackageTokens, template, projectLocationURI, null, null);
	}

	public TemplateProjectConfigurationDescriptor(String projectName, String[] topLevelPackageTokens,
			Template template, URI projectLocationURI, Map<String, Object> collectedInput,
			Map<String, String> inputKinds) {
		this.template = template;
		this.topLevelPackageTokens = topLevelPackageTokens;
		this.projectName = projectName;
		this.projectLocationURI = projectLocationURI;
		this.collectedInput = collectedInput;
		this.inputKinds = inputKinds;
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

	/**
	 * This may be null. A null collection means that the template values have
	 * not yet been processed by the time this descriptor is created, perhaps
	 * because the template data has not yet been downloaded. This is in
	 * contrast to an empty collection, which means the values were processed,
	 * but no values were present.
	 * @return Possibly null collection of input containing template values.
	 */
	public Map<String, Object> getCollectedInput() {
		return collectedInput;
	}

	/**
	 * This may be null. A null list of attribute types indicates that the
	 * template data has not yet been downloaded at the time that this
	 * descriptor was created. An empty list of attributes means that template
	 * data has already been downloaded, but it contains no attributes that have
	 * values.
	 * @return Possibly null types of attributes defined in the template.
	 */
	public Map<String, String> getInputKinds() {
		return inputKinds;
	}

	public IPath getProjectLocationPath() {
		if (projectLocationURI != null) {
			return new Path(projectLocationURI.toString());
		}
		return null;
	}

}
