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
