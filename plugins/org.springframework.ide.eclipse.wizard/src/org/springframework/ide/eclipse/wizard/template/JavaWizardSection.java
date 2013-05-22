/*******************************************************************************
 *  Copyright (c) 2012, 2013 VMware, Inc.
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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.ui.wizards.NewJavaProjectWizardPageTwo;
import org.eclipse.jface.wizard.IWizardPage;
import org.springframework.ide.eclipse.wizard.template.infrastructure.Template;
import org.springframework.ide.eclipse.wizard.template.infrastructure.ui.WizardUIInfo;

/**
 * Contributes the creation of a Java project with Spring configuration to the
 * New Spring Project Wizard. This only handles the case of a user selecting to
 * creation a simple Java project as the base for their Spring project using
 * Java builds. Maven projects are not handled by this section provider. Also
 * note that Simple Java projects are an exception to how template projects are
 * configured. Whereas a regular template project is configured entirely based
 * on the information of a template, the Java project is a hybrid between a
 * template project and a regular Java project, and therefore obtains
 * configuration from both a Java template and a Java project.
 * 
 */
public class JavaWizardSection extends SpringProjectWizardSection {

	private final NewJavaProjectWizardPageTwo javaPageTwo;

	private final NewSpringProjectCreationPage springCreationPage;

	public JavaWizardSection(NewSpringProjectWizard wizard) {
		super(wizard);
		springCreationPage = new NewSpringProjectCreationPage();
		springCreationPage.setWizard(getWizard());

		javaPageTwo = new NewJavaProjectWizardPageTwo(springCreationPage);
		javaPageTwo.setWizard(getWizard());
	}

	public IJavaElement getCreatedElement() {
		return javaPageTwo.getJavaProject();
	}

	@Override
	public boolean canFinish() {
		boolean canFinish = super.canFinish();

		if (canFinish) {
			// Set the current project name and location URI in the Java
			// specific
			// pages before checking if the Java pages are complete
			refreshProjectValues();
			canFinish = springCreationPage.isPageComplete() && javaPageTwo.isPageComplete();
		}
		return canFinish;
	}

	protected void refreshProjectValues() {
		// Note to prevent infinite event triggering, do not set values if no
		// changes have occurred.
		if (springCreationPage != null) {
			String projectName = getWizard().getMainPage().getProjectName();
			URI projectLocationURI = getWizard().getMainPage().getProjectLocationURI();
			String projectNameInSpringPage = springCreationPage.getProjectName();
			URI projectLocationInSpringURI = springCreationPage.getProjectLocationURI();

			if (!areEqual(projectLocationURI, projectLocationInSpringURI, projectName, projectNameInSpringPage)) {
				springCreationPage.setProjectLocationURI(projectLocationURI);
				springCreationPage.setProjectName(projectName);
			}
		}
	}

	protected boolean areEqual(URI actualURI, URI uriInSpringPage, String actualName, String inSpringPageName) {
		boolean equals = false;
		if (actualName == inSpringPageName) {
			equals = true;
		}
		else {
			equals = (actualName != null) ? actualName.equals(inSpringPageName) : false;
		}

		if (equals) {
			if (actualURI == uriInSpringPage) {
				equals = true;
			}
			else {
				equals = (actualURI != null) ? actualURI.equals(uriInSpringPage) : false;
			}
		}

		return equals;
	}

	@Override
	public boolean canProvide(ProjectWizardDescriptor descriptor) {
		return descriptor != null && descriptor.getTemplate() != null
				&& TemplateConstants.SIMPLE_JAVA_TEMPLATE_ID.equals(descriptor.getTemplate().getItem().getId());
	}

	@Override
	public boolean hasNextPage(IWizardPage currentPage) {
		return currentPage == getWizard().getMainPage() || currentPage == springCreationPage;
	}

	@Override
	public IWizardPage getNextPage(IWizardPage page) {

		if (page == springCreationPage) {
			return javaPageTwo;
		}
		else if (page == getWizard().getMainPage()) {
			// Download the Simple Java template if going to the next page
			// Download the template contents for templates that contribute
			// additional UI pages, as the
			// content will determine the UI controls of the additional
			// pages.
			try {
				Template template = getWizard().getMainPage().getSelectedTemplate();
				TemplateUtils.downloadTemplateData(template, getWizard().getShell());
			}
			catch (CoreException ce) {
				handleError(ce.getStatus());
				return null;
			}

			springCreationPage.refreshProjectValues();
			return springCreationPage;
		}
		else {
			return null;
		}
	}

	@Override
	public void cancel() {
		javaPageTwo.performCancel();
	}

	@Override
	public ProjectConfiguration getProjectConfiguration() throws CoreException {

		if (springCreationPage != null) {
			// Handle the template portion
			Template template = getWizard().getMainPage().getSelectedTemplate();

			// For simple projects download the template data,
			// whether they contribute additional
			// wizard pages or not, as a user can click "Finish" for
			// simple projects from the first page, as opposed to other
			// templates
			// where the
			// template data gets downloaded when a user clicks "Next"
			if (template instanceof SimpleProject) {
				TemplateUtils.downloadTemplateData(template, getWizard().getShell());
			}

			final WizardUIInfo uiInfo = getUIInfo(template);

			// Empty List as there is no input to be entered for Java template.
			List<TemplateInputCollector> inputHandlers = new ArrayList<TemplateInputCollector>(0);

			TemplateProjectConfigurationDescriptor templateDescriptor = new TemplateProjectConfigurationDescriptor(
					getWizard().getMainPage().getProjectName(), uiInfo.getTopLevelPackageTokens(), template,
					getWizard().getMainPage().getProjectLocationURI(), inputHandlers, getWizard().getMainPage()
							.getVersion());

			JavaProjectConfigurationDescriptor javaDescriptor = new JavaProjectConfigurationDescriptor(
					springCreationPage.getConfigSuffixes(), springCreationPage.enableImports(),
					springCreationPage.enableProjectFacets(), springCreationPage.ignoreMissingNamespaceHandlers(),
					springCreationPage.disableNamespaceCaching(), springCreationPage.useHighestXsdVersion(),
					springCreationPage.useProjectSettings(), springCreationPage.loadHandlerFromClasspath());

			return new JavaProjectConfiguration(javaDescriptor, templateDescriptor, getWizard().getShell()) {
				// Commented out for now, to see if creating the Java project through the Java project wizard pages is necessary
				// instead of delegating to the template project creation.
				// @Override
				// protected IProject create(IProgressMonitor monitor) throws
				// CoreException {
				//
				// try {
				// springCreationPage.performFinish();
				// javaPageTwo.performFinish(monitor);
				// }
				// catch (InterruptedException e) {
				// throw new CoreException(new Status(IStatus.ERROR,
				// WizardPlugin.PLUGIN_ID, e.getMessage(), e));
				// }
				// return ((IJavaProject) getCreatedElement()).getProject();
				// }
			};
		}
		else {
			return null;
		}

	}
}
