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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;
import org.springframework.ide.eclipse.wizard.WizardPlugin;
import org.springframework.ide.eclipse.wizard.template.infrastructure.Template;
import org.springframework.ide.eclipse.wizard.template.infrastructure.ui.WizardUIInfo;
import org.springframework.ide.eclipse.wizard.template.infrastructure.ui.WizardUIInfoLoader;
import org.springsource.ide.eclipse.commons.ui.UiStatusHandler;

import com.thoughtworks.xstream.XStreamException;

/**
 * Section responsible for creating a Spring project from a template. Note that
 * the actual template contents are not downloaded until a user clicks the
 * "Next" button in the wizard, which in turn requests this section for
 * additional pages. The "Next" button is enabled/disabled by a lighter weight
 * calculation that does not require to download the contents of the template to
 * determine if additional pages are present or not. Other metadata related to
 * the template determines if the "Next" button in the wizard should be enabled.
 * See the Template API.
 * 
 */
public class TemplateWizardSection extends SpringProjectWizardSection {

	private ITemplateWizardPage firstTemplatePage = null;

	private Map<String, Object> collectedInput;

	private Map<String, String> inputKinds;

	public TemplateWizardSection(NewSpringProjectWizard wizard) {
		super(wizard);
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

			if (template instanceof SimpleProject) {
				return ((SimpleProject) template).hasWizardPages();
			}
			else {
				// Assume it does have wizard pages, and that the wizard pages
				// will be populated once the
				// template content is downloaded
				return true;
			}
		}
		return false;
	}

	/**
	 * 
	 * @param template
	 * @return non-null Wizard ui info, or throws exception if an error occurred
	 * while resolving the info.
	 * @throws CoreException
	 */
	protected WizardUIInfo getUIInfo(Template template) throws CoreException {
		if (template == null) {
			return null;
		}
		URL jsonWizardUIDescriptor = template.getTemplateLocation();

		if (jsonWizardUIDescriptor == null) {
			return null;
		}

		WizardUIInfo info;
		try {
			WizardUIInfoLoader infoLoader = new WizardUIInfoLoader();
			InputStream jsonDescriptionInputStream = jsonWizardUIDescriptor.openStream();
			info = infoLoader.load(jsonDescriptionInputStream);
		}
		catch (IOException ex) {
			throw new CoreException(new Status(Status.ERROR, WizardPlugin.PLUGIN_ID,
					"Failed to load json descriptor for wizard page"));

		}
		catch (XStreamException ex) {
			throw new CoreException(new Status(Status.ERROR, WizardPlugin.PLUGIN_ID,
					"Failed to load json descriptor for wizard page"));
		}

		if (info == null) {
			throw new CoreException(new Status(Status.ERROR, WizardPlugin.PLUGIN_ID,
					"Unable to find template project location"));
		}

		return info;
	}

	@Override
	public IWizardPage getNextPage(IWizardPage page) {

		if (page == getWizard().getMainPage()) {

			Template template = getWizard().getMainPage().getSelectedTemplate();

			if (template == null) {
				// no need to inform the wizard that a template was not
				// selected, template validation occurs in the part
				// itself.
				return null;
			}
			else if (template instanceof SimpleProject) {
				// Handle the special case templates with no pages
				SimpleProject simpleProject = (SimpleProject) template;
				if (!simpleProject.hasWizardPages()) {
					return null;
				}
			}
			else {
				// Download the template contents
				try {
					getWizard().getMainPage().downloadTemplateContent();
				}
				catch (CoreException ce) {
					handleError(ce.getStatus());
					return null;
				}

				WizardUIInfo info;
				try {
					info = getUIInfo(template);
				}
				catch (CoreException e1) {
					handleError(e1.getStatus());
					return null;
				}

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
							"Failed to load wizard page for project template for " + template.getName(), e));
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

		Template template = getWizard().getMainPage().getSelectedTemplate();
		if (template == null) {
			return false;
		}
		else if (!(template instanceof SimpleProject)) {
			// Non-simple project templates should always have a template page,
			// therefore inquire finish state from the template page
			return firstTemplatePage != null && firstTemplatePage.isPageComplete();
		}
		else {
			// if it is a simple project, let the simple project pages determine
			// if the wizard can complete.
			return true;
		}

	}

	protected void handleError(IStatus status) {
		UiStatusHandler.logAndDisplay(status);
	}

	@Override
	public boolean canProvide(ProjectWizardDescriptor descriptor) {
		return descriptor != null && descriptor.getTemplate() != null
				&& !TemplateConstants.SIMPLE_JAVA_TEMPLATE_ID.equals(descriptor.getTemplate().getItem().getId());
	}

	public void selectAndReveal(final IResource newResource) {
		getWizard().getContainer().getShell().getDisplay().syncExec(new Runnable() {

			public void run() {
				BasicNewResourceWizard.selectAndReveal(newResource, getWizard().getWorkbench()
						.getActiveWorkbenchWindow());
			}
		});
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
	public IProject createProject(IProgressMonitor monitor) throws CoreException {

		collectedInput = new HashMap<String, Object>();
		inputKinds = new HashMap<String, String>();
		collectInput(collectedInput, inputKinds);

		String projectName = getWizard().getMainPage().getProjectName();
		return ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
	}

	@Override
	public ProjectConfiguration getProjectConfiguration() throws CoreException {
		Template template = getWizard().getMainPage().getSelectedTemplate();
		WizardUIInfo uiInfo = getUIInfo(template);

		TemplateProjectConfigurationDescriptor descriptor = new TemplateProjectConfigurationDescriptor(uiInfo,
				template, collectedInput, inputKinds, getWizard().getMainPage().getProjectLocationURI());
		return new TemplateProjectConfiguration(descriptor, getWizard().getShell());
	}
}
