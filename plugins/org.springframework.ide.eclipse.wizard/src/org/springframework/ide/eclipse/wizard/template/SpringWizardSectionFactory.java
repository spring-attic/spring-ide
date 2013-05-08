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

import java.util.ArrayList;
import java.util.List;

/**
 * Creates a New Spring Project wizard section based on the project description
 * that was selected in the wizard. For example, if a Simple Java project should
 * be created, this factory will create a wizard section that can create and
 * configure a Simple Java project, assuming such a section is registered.
 * 
 */
public class SpringWizardSectionFactory {

	private final NewSpringProjectWizard wizard;

	private List<SpringProjectWizardSection> sections;

	public SpringWizardSectionFactory(NewSpringProjectWizard wizard) {
		this.wizard = wizard;
	}

	public SpringProjectWizardSection getSection(ProjectWizardDescriptor descriptor) {
		if (sections == null) {
			loadSections();
		}

		for (SpringProjectWizardSection section : sections) {
			if (section.canProvide(descriptor)) {
				return section;
			}
		}
		return null;
	}

	protected void loadSections() {
		sections = new ArrayList<SpringProjectWizardSection>();
		sections.add(new JavaWizardSection(wizard));
		sections.add(new TemplateWizardSection(wizard));

	}
}
