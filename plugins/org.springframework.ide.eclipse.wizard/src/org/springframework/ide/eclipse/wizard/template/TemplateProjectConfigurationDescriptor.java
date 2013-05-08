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
import org.springframework.ide.eclipse.wizard.template.infrastructure.ui.WizardUIInfo;

public class TemplateProjectConfigurationDescriptor implements IProjectConfigurationDescriptor {

	private final WizardUIInfo uiInfo;

	private final Template template;

	private final Map<String, Object> collectedInput;

	private final Map<String, String> inputKinds;

	private final URI projectLocationURI;

	public TemplateProjectConfigurationDescriptor(WizardUIInfo uiInfo, Template template,
			Map<String, Object> collectedInput, Map<String, String> inputKinds, URI projectLocationURI) {
		this.template = template;
		this.uiInfo = uiInfo;
		this.collectedInput = collectedInput;
		this.inputKinds = inputKinds;
		this.projectLocationURI = projectLocationURI;
	}

	public WizardUIInfo getUiInfo() {
		return uiInfo;
	}

	public Template getTemplate() {
		return template;
	}

	public Map<String, Object> getCollectedInput() {
		return collectedInput;
	}

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
