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
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
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
 * Section responsible for creating a Spring project from a template.
 * 
 */
public class TemplateWizardSection extends SpringProjectWizardSection {

	private ITemplateWizardPage firstTemplatePage = null;

	private Map<String, Object> collectedInput;

	private Map<String, String> inputKinds;

	public TemplateWizardSection(NewSpringProjectWizard wizard) {
		super(wizard);
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
		URL jsonWizardUIDescriptor;
		jsonWizardUIDescriptor = template.getTemplateLocation();

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

			try {
				for (int i = 0; i < info.getPageCount(); i++) {

					templatePage = new NewTemplateWizardPage(info.getPage(i).getDescription(),
							info.getElementsForPage(i), template.getIcon());
					templatePage.setWizard(getWizard());

					if (firstTemplatePage == null) {
						firstTemplatePage = templatePage;
					}

					if (previousPage != null) {
						previousPage.setNextPage(templatePage);
					}

					previousPage = templatePage;
				}

				return firstTemplatePage;
			}
			catch (Exception e) {
				handleError(new Status(Status.ERROR, WizardPlugin.PLUGIN_ID,
						"Failed to load wizard page for project template for " + info.getProjectNameToken(), e));
				return null;
			}
		}
		return null;
	}

	@Override
	public boolean canFinish() {
		if (firstTemplatePage != null) {
			return firstTemplatePage.isPageComplete();
		}
		// Not all templates contribute pages to the wizard, therefore by
		// default assume the wizard can be completed if no template page is
		// set.
		return true;
	}

	protected void handleError(IStatus status) {
		UiStatusHandler.logAndDisplay(status);
	}

	protected IPath getLocationPath() {
		URI uri = getWizard().getMainPage().getProjectLocationURI();
		if (uri != null) {
			return new Path(uri.toString());
		}
		return null;
	}

	@Override
	public boolean canProvide(ProjectWizardDescriptor descriptor) {
		return descriptor != null && descriptor.getTemplate() != null
				&& !"Simple Java".equals(descriptor.getTemplate().getName());
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
