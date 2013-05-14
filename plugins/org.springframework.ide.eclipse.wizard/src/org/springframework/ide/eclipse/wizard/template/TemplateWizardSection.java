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

import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.wizard.IWizardPage;
import org.springframework.ide.eclipse.wizard.WizardPlugin;
import org.springframework.ide.eclipse.wizard.template.infrastructure.Template;
import org.springframework.ide.eclipse.wizard.template.infrastructure.ui.WizardUIInfo;

/**
 * Section responsible for creating a Spring project from a template. Note that
 * the actual template contents are not downloaded until a user clicks the
 * "Next" button in the wizard, which in turn requests this section for
 * additional pages. However, in some simple template cases that do not
 * contribute additional wizard pages, the template contents are downloaded only
 * when a user clicks "Finish" and a project is about to be created. The "Next"
 * button is enabled/disabled by a lighter weight calculation that does not
 * require to download the contents of the template to determine if additional
 * pages are present or not. Other metadata related to the template determines
 * if the "Next" button in the wizard should be enabled. See the Template API.
 * 
 */
public class TemplateWizardSection extends SpringProjectWizardSection {

	protected ITemplateWizardPage firstTemplatePage = null;

	public TemplateWizardSection(NewSpringProjectWizard wizard) {
		super(wizard);
	}

	@Override
	public boolean canProvide(ProjectWizardDescriptor descriptor) {
		return descriptor != null && descriptor.getTemplate() != null
				&& !TemplateConstants.SIMPLE_JAVA_TEMPLATE_ID.equals(descriptor.getTemplate().getItem().getId());
	}

	public void collectInput(Map<String, Object> collectedInput, Map<String, String> inputKinds) {
		IWizardPage page = firstTemplatePage;
		while (page != null) {
			if (page instanceof NewTemplateWizardPage) {
				((NewTemplateWizardPage) page).collectInput(collectedInput, inputKinds);
			}
			page = page.getNextPage();
		}
	}

	@Override
	public IWizardPage getNextPage(IWizardPage page) {

		if (page == getWizard().getMainPage()) {

			Template template = getWizard().getMainPage().getSelectedTemplate();

			if (template == null) {
				// no need to inform the wizard that a template was not
				// selected, template validation occurs in the template
				// selection part
				// itself.
				return null;
			}
			else if (template instanceof SimpleProject && !((SimpleProject) template).hasWizardPages()) {
				// Handle the special case templates with no pages
				return null;
			}
			else {
				// Download the template contents for templates that contribute
				// additional UI pages, as the
				// content will determine the UI controls of the additional
				// pages.
				try {
					TemplateUtils.downloadTemplateData(template, getWizard().getShell());
				}
				catch (CoreException ce) {
					handleError(ce.getStatus());
					return null;
				}

				WizardUIInfo info = getUIInfo(template);

				ITemplateWizardPage previousPage = null;

				ITemplateWizardPage templatePage = null;

				ITemplateWizardPage firstPage = null;

				try {
					for (int i = 0; i < info.getPageCount(); i++) {

						templatePage = new NewTemplateWizardPage(info.getPage(i).getDescription(),
								info.getElementsForPage(i), template.getIcon());
						templatePage.setWizard(getWizard());

						// Always set a new first template page, as the template
						// selection may have changed and may
						// have a different associated template page
						if (firstPage == null) {
							firstPage = templatePage;
						}

						if (previousPage != null) {
							previousPage.setNextPage(templatePage);
						}

						previousPage = templatePage;
					}
				}
				catch (Exception e) {
					handleError(new Status(Status.ERROR, WizardPlugin.PLUGIN_ID,
							"Failed to load wizard page for project template for " + template.getName() + " due to "
									+ e.getMessage(), e));
				}

				// Regardless of whether wizard pages where successfully
				// resolved for the given template, update
				// the first page value, even if it is null so that the wizard
				// does not display a previous first page of another
				// template for the current template.
				firstTemplatePage = firstPage;

				return firstTemplatePage;
			}
		}
		return null;
	}

	@Override
	public boolean canFinish() {
		boolean canFinish = super.canFinish();

		if (canFinish) {
			Template template = getWizard().getMainPage().getSelectedTemplate();
			if (!(template instanceof SimpleProject)) {
				// Non-simple project templates should always have a template
				// page,
				// therefore inquire finish state from the template page
				canFinish = firstTemplatePage != null && firstTemplatePage.isPageComplete();
			}
			// For now, any simple template project can complete from the first
			// wizard page.
		}

		return canFinish;

	}

	@Override
	public boolean hasNextPage(IWizardPage currentPage) {
		// This check is performed to enable/disable the Next button without
		// having to download the contents of the template
		// Only check if there is one more page after the main page. Any
		// subsequent pages after the second page are
		// indicated within the implementation of the second page.
		if (currentPage == getWizard().getMainPage()) {
			Template template = getWizard().getMainPage().getSelectedTemplate();
			if (template == null) {
				return false;
			}
			else if (template instanceof SimpleProject) {
				return ((SimpleProject) template).hasWizardPages();
			}
			else {
				// Non-simple template projects should always contribute a new
				// page
				return true;
			}
		}
		return false;
	}

	@Override
	public ProjectConfiguration getProjectConfiguration() throws CoreException {

		return new TemplateProjectConfiguration(getWizard().getShell()) {

			@Override
			protected void collectInput(Map<String, Object> collectedInput, Map<String, String> inputKinds) {
				// Override as collecting input is still coupled to reading
				// directly from UI.
				TemplateProjectConfigurationDescriptor descriptor = (TemplateProjectConfigurationDescriptor) getConfigurationDescriptor();
				Template template = descriptor.getTemplate();

				// Only collect input from pages for templates that contributed
				// wizard pages
				if (!(template instanceof SimpleProject) || ((SimpleProject) template).hasWizardPages()) {
					// This collects input for the template directly from the
					// wizard pages
					TemplateWizardSection.this.collectInput(collectedInput, inputKinds);
				}
			}

			@Override
			protected IProjectConfigurationDescriptor getConfigurationDescriptor() {

				IProjectConfigurationDescriptor descriptor = super.getConfigurationDescriptor();

				if (descriptor == null) {
					try {
						Template template = getWizard().getMainPage().getSelectedTemplate();

						// For simple projects with no additional wizard pages,
						// download the template data, as the template data only
						// gets downloaded on project creation time. Otherwise,
						// the template data should already have
						// be downloaded prior to reaching to this stage
						if (template instanceof SimpleProject && !((SimpleProject) template).hasWizardPages()) {
							TemplateUtils.downloadTemplateData(template, getShell());
						}

						WizardUIInfo uiInfo = getUIInfo(template);

						descriptor = new TemplateProjectConfigurationDescriptor(getWizard().getMainPage()
								.getProjectName(), uiInfo.getTopLevelPackageTokens(), template, getWizard()
								.getMainPage().getProjectLocationURI());
						setConfigurationDescriptor(descriptor);
					}
					catch (CoreException e) {
						TemplateWizardSection.this.handleError(e.getStatus());
					}
				}
				return descriptor;
			}

		};
	}
}
