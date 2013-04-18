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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.widgets.Control;

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

	/**
	 * Optional. Control that should be added to the main page with project
	 * creation and configuration UI specific to the project type handled by
	 * this section.
	 * @return Control for main page in New Spring Project wizard, or null;
	 */
	public Control getMainPageControl() {
		return null;
	}

	/**
	 * 
	 * Optional implementation. If further wizard UI is needed to create and
	 * configure a project, return the next page of the wizard, or null if no
	 * additional pages are needed. Subclasses must ensure that pages are added
	 * to the wizard via IWizardPage.setWizard(..).
	 * @return next page in the wizard. The page MUST have already been added to
	 * the wizard prior to returning it.
	 */
	public IWizardPage getNextPage(IWizardPage page) {
		return null;
	}

	/**
	 * Creates a project. This is may be invoked by the New Spring Project
	 * wizard framework as a workspace job.
	 * @param monitor
	 * @return created project, or null if project was not created
	 * @throws CoreException with any errors that resulted in failure to create
	 * a project
	 */
	public abstract IProject createProject(IProgressMonitor monitor) throws CoreException;

	/**
	 * Configure a project after it has been successfully created. The
	 * configuration implementation can launch jobs to configure the project as
	 * it is only invoked when the wizard finishes.
	 * @param project
	 * @return true if configuration succeeded, or configuration is deferred
	 * after wizard finishes. False if configuration failed and the wizard
	 * should remain open.
	 */
	public abstract boolean configureProject(IProject project);

	/**
	 * Determine if this section can create a project and optionally provide UI
	 * for the project creation and configuration.
	 * @param descriptor with the project and build types that was selected in
	 * the New Spring Project wizard.
	 * @return true if it can provide, or false otherwise
	 */
	public abstract boolean canProvide(ProjectWizardDescriptor descriptor);

	/**
	 * If this section contributes additional UI, iterate through the pages and
	 * additional UI to tell the wizard whether it can finish or not.
	 * @return true if it can finish. False otherwise.
	 */
	public boolean canFinish() {
		return true;
	}

}
