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

	public IWizardPage getNextPage(IWizardPage currentPage) {
		return null;
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
	 * Creates a project. This is may be invoked by the New Spring Project
	 * wizard framework as a workspace job.
	 * @param monitor
	 * @return created project, or null if project was not created
	 * @throws CoreException with any errors that resulted in failure to create
	 * a project
	 */
	public abstract IProject createProject(IProgressMonitor monitor) throws CoreException;

	/**
	 * Determine if this section can create a project and optionally provide UI
	 * for the project creation and configuration.
	 * @param descriptor with the project and build types that was selected in
	 * the New Spring Project wizard.
	 * @return true if it can provide, or false otherwise
	 */
	public abstract boolean canProvide(ProjectWizardDescriptor descriptor);

	/**
	 * Configuration occurs after a project has been created, and it may not
	 * occur right after the project has been created. Therefore the reason that
	 * the project creation and configuration are separate.
	 * @return A project configuration, if necessary. The configuration only
	 * occurs after a project has been created.
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
	public abstract boolean canFinish();

}
