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

import org.springframework.ide.eclipse.wizard.template.infrastructure.Template;

/**
 * Describes the type of Spring project that the New Spring Project wizard
 * should create.
 * 
 */
public class ProjectWizardDescriptor {

	private final Template template;

	public ProjectWizardDescriptor(Template template) {
		this.template = template;
	}

	public Template getTemplate() {
		return template;
	}
}
