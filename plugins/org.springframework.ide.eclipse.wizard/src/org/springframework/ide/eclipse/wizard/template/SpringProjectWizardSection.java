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

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.wizard.IWizardPage;
import org.springframework.ide.eclipse.wizard.template.infrastructure.Template;
import org.springframework.ide.eclipse.wizard.template.infrastructure.ui.WizardUIInfo;
import org.springframework.ide.eclipse.wizard.template.infrastructure.ui.WizardUIInfoLoader;
import org.springsource.ide.eclipse.commons.ui.UiStatusHandler;

/**
 * Provides a section to the New Spring Project Wizard, with two primary
 * purposes:
 * 
 * <p/>
 * 1. Create a project
 * <p/>
 * 2. Configure the project after it has been created.
 * 
 * <p/>
 * 
 * In addition, the section can also contribute additional UI to both the main
 * page of the New Spring Project Wizard or additional pages to the wizard.
 * 
 * <p/>
 * 
 * The project creation and configuration are separated, as they may occur at
 * different stages in the lifecycle of the New Spring Project wizard.
 * 
 */
public abstract class SpringProjectWizardSection {

	private final NewSpringProjectWizard wizard;

	public SpringProjectWizardSection(NewSpringProjectWizard wizard) {
		this.wizard = wizard;
	}

	protected NewSpringProjectWizard getWizard() {
		return wizard;
	}

	public void cancel() {
		// Default is to do nothing
	}

	protected WizardUIInfo getUIInfo(Template template) {
		try {
			return new WizardUIInfoLoader().getUIInfo(template);
		}
		catch (CoreException e1) {
			handleError(e1.getStatus());
			return null;
		}

	}

	protected void handleError(IStatus status) {
		// Display error in a separate dialogue as some error messages can get
		// long.
		UiStatusHandler.logAndDisplay(status);
	}

	/**
	 * True if the section contributes more pages after the current one. False
	 * otherwise. This is meant to be a light-weight alternative to actually
	 * creating a page given a current page.
	 */
	public boolean hasNextPage(IWizardPage currentPage) {

		return false;
	}

	/**
	 * Optional. Sections can also precreate the page controls prior to having
	 * the wizard making them visible. This may be necessary for some sections
	 * that require the controls to be created for other pages, in order to read
	 * control values if finishing the wizard before those pages are shown. The
	 * pages are then added to the wizard when the wizard is created.
	 * @param parent
	 */
	public List<IWizardPage> loadPages() {
		return null;
	}

	/**
	 * Page controls get created when a request is made for the next page, if it
	 * already hasn't been created.
	 * @param page current page
	 * @return next page in the wizard, or null if not contributing further
	 * pages
	 */
	public abstract IWizardPage getNextPage(IWizardPage page);

	/**
	 * Determine if this section can create a project and optionally provide UI
	 * for the project creation and configuration.
	 * @param descriptor with the project and build types that was selected in
	 * the New Spring Project wizard.
	 * @return true if it can provide, or false otherwise
	 */
	public abstract boolean canProvide(ProjectWizardDescriptor descriptor);

	/**
	 * Creates and configures a project. Note that the project creation and
	 * configuration may occur as separate steps. The project creation may run
	 * in the UI thread, and is intended to be light-weight, while the project
	 * configuration occurs after the project is created, and runs in a non-UI
	 * thread, intended for long running configurations.
	 * @return A project configuration that creates and configures a project.
	 * Must not be null.
	 * @throws CoreException if error occurs during configuration
	 */
	public abstract ProjectConfiguration getProjectConfiguration() throws CoreException;

	/**
	 * If this section contributes additional pages, tell the wizard whether it
	 * can finish or not based on the state of the pages. Note that this only
	 * pertains to the state of the pages, not whether the project was
	 * successfully created or configured.
	 * @return true if pages contributed by this section can finish. False
	 * otherwise.
	 */
	public boolean canFinish() {

		return getWizard().getMainPage().getSelectedTemplate() != null;

	}

}
