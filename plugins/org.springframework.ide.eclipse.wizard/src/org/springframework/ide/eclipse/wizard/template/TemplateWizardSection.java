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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.ui.PlatformUI;
import org.springframework.ide.eclipse.wizard.WizardPlugin;
import org.springframework.ide.eclipse.wizard.template.infrastructure.SimpleProjectFactory;
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
 * pages are present or not.
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
				&& !SimpleProjectFactory.SIMPLE_JAVA_TEMPLATE_ID.equals(descriptor.getTemplate().getItem().getId());
	}

	public List<TemplateInputCollector> getTemplateInputHandlers() {
		List<TemplateInputCollector> handlers = new ArrayList<TemplateInputCollector>();
		IWizardPage page = firstTemplatePage;
		while (page != null) {
			if (page instanceof NewTemplateWizardPage) {
				TemplateInputCollector handler = ((NewTemplateWizardPage) page).getInputHandler();
				if (handler != null) {
					handlers.add(handler);
				}
			}
			page = page.getNextPage();
		}
		return handlers;
	}

	@Override
	public IWizardPage getNextPage(IWizardPage page) {

		if (page == getWizard().getMainPage()) {

			final Template template = getWizard().getMainPage().getSelectedTemplate();

			// First, clear the cached first template page, as to not show the
			// wrong page
			firstTemplatePage = null;

			if (template == null) {
				// no need to log an error that a template was not
				// selected, template validation occurs in the template
				// selection part
				// itself.
				return null;
			}
			else if (template instanceof SimpleProject && !((SimpleProject) template).hasWizardPages()) {
				// Handle the special case simple projects with no pages
				return null;
			}
			else {
				// Download the template contents for templates that contribute
				// additional UI pages, as the
				// content will determine the UI controls of the additional
				// pages.
				final IStatus[] errors = new IStatus[1];
				PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {

					public void run() {

						try {
							getWizard().getContainer().run(true, true,
									new TemplateDataUIJob(template, getWizard().getShell()));

						}
						catch (InvocationTargetException ce) {
							String errorMessage = ErrorUtils.getErrorMessage(ce);
							errors[0] = new Status(IStatus.ERROR, WizardPlugin.PLUGIN_ID, errorMessage, ce);
						}
						catch (InterruptedException ie) {
							errors[0] = new Status(IStatus.ERROR, WizardPlugin.PLUGIN_ID,
									"Interrupt exception while downloading data for " + template.getName());
						}

					}
				});

				if (errors[0] != null && !errors[0].isOK()) {
					handleError(errors[0]);
					return null;
				}

				WizardUIInfo info = getUIInfo(template);

				if (!hasTemplateWizardPages()) {
					// Update the buttons so that the "Next" button is disabled.
					// This is done
					// indirectly by the wizard which calls hasNext().. once
					// again to determine the button states based on data
					// already downloaded.
					getWizard().getContainer().updateButtons();
					return null;
				}

				ITemplateWizardPage previousPage = null;

				ITemplateWizardPage templatePage = null;

				ITemplateWizardPage firstPage = null;

				try {
					for (int i = 0; i < info.getPageCount(); i++) {

						templatePage = new NewTemplateWizardPage(info.getPage(i).getDescription(),
								new TemplateInputCollector(info.getElementsForPage(i)), template.getIcon());
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
					String error = ErrorUtils.getErrorMessage("Failed to load wizard page for project template for "
							+ template.getName(), e);
					handleError(new Status(Status.ERROR, WizardPlugin.PLUGIN_ID, error, e));
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
			// For now, any simple template project can complete from the first
			// wizard page.
			if (!(template instanceof SimpleProject)) {
				// Non-simple project templates should already be downloaded by
				// now, and therefore
				// either have a template wizard page that can be checked for
				// completeness or indicate it did not provide pages.

				if (TemplateUtils.hasBeenDownloaded(template)) {
					WizardUIInfo info = getUIInfo(template);

					if (info == null) {
						canFinish = false;
					}
					else {
						canFinish = info.getPageCount() == 0 || info.getElementsForPage(0) == null
								|| info.getElementsForPage(0).isEmpty()
								|| (firstTemplatePage != null && firstTemplatePage.isPageComplete());
					}

				}
				else {
					canFinish = false;
				}

			}

		}

		return canFinish;

	}

	@Override
	public boolean hasNextPage(IWizardPage currentPage) {
		// This check is performed to enable/disable the Next button without
		// having to download the contents of the template
		// Only check if there is one more page after the main page. Any
		// subsequent pages after the second page added by the second page.
		if (currentPage == getWizard().getMainPage()) {
			Template template = getWizard().getMainPage().getSelectedTemplate();
			if (template == null) {
				return false;
			}
			else if (template instanceof SimpleProject) {
				return ((SimpleProject) template).hasWizardPages();
			}
			else {
				// Otherwise, determine if the template has been downloaded and
				// information can be
				// determined from the wizard ui info.
				return hasTemplateWizardPages();
			}
		}
		return false;
	}

	/**
	 * Determines if a non-Simple Project template contributes wizard pages
	 * based entire on the downloaded wizard UI info. If no data has been
	 * downloaded yet, and a template is selected, it will assume it does
	 * contribute pages. This does not handle Simple Projects, which should not
	 * be contributing wizard pages through the wizard UI info.
	 */
	protected boolean hasTemplateWizardPages() {
		Template template = getWizard().getMainPage().getSelectedTemplate();
		if (template == null || template instanceof SimpleProject) {
			return false;
		}
		// If it hasn't been download, there is no way to know if the template
		// contributes pages or not, so
		// by default assume it does. Later, after its been downloaded, if it
		// turns out that the template did
		// not contribute any pages, the wizard buttons will be updated
		// accordingly.
		if (TemplateUtils.hasBeenDownloaded(template)) {
			WizardUIInfo info = getUIInfo(template);

			return info != null && info.getPageCount() > 0 && info.getElementsForPage(0) != null
					&& !info.getElementsForPage(0).isEmpty();
		}

		return true;
	}

	@Override
	public ProjectConfiguration getProjectConfiguration() throws CoreException {

		Template template = getWizard().getMainPage().getSelectedTemplate();

		final WizardUIInfo uiInfo = getUIInfo(template);

		TemplateProjectConfigurationDescriptor descriptor = new TemplateProjectConfigurationDescriptor(getWizard()
				.getMainPage().getProjectName(), uiInfo.getTopLevelPackageTokens(), template, getWizard().getMainPage()
				.getProjectLocationURI(), getTemplateInputHandlers(), getWizard().getMainPage().getVersion());

		return new TemplateProjectConfiguration(descriptor, getWizard().getShell());
	}

}
