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

/**
 * 
 * Creates and configures a project. The Spring project creation process may run
 * the configuration in a non-UI thread, therefore the configuration should not
 * contain dialogs and other UI. Configuration of the project is performed on
 * information carried by a configuration descriptor, which decouples the UI
 * from the project configuration. Instead of reading values directly from UI
 * into the configuration, use the descriptor to pass the necessary information
 * to the configuration.
 * 
 */
public abstract class ProjectConfiguration {
	private IProjectConfigurationDescriptor projectConfigurationDescriptor;

	/**
	 * 
	 * @param projectConfigurationDescriptor
	 */
	protected ProjectConfiguration(IProjectConfigurationDescriptor projectConfigurationDescriptor) {
		this.projectConfigurationDescriptor = projectConfigurationDescriptor;
	}

	protected IProjectConfigurationDescriptor getConfigurationDescriptor() {
		return projectConfigurationDescriptor;
	}

	protected void setConfigurationDescriptor(IProjectConfigurationDescriptor descriptor) {
		this.projectConfigurationDescriptor = descriptor;
	}

	/**
	 * Creates a project. This is invoked in the UI thread, in case UI control
	 * references are needed. Creating a project should be lighter weight than
	 * configuring a project, which is performed after a project is created.
	 * @param monitor
	 * @return created project, or null if project was not created
	 * @throws CoreException with any errors that resulted in failure to create
	 * a project
	 */
	public abstract IProject createProject(IProgressMonitor monitor) throws CoreException;

	/**
	 * Configures a project after a project has been created. This is run in a
	 * non-UI thread, and is intended to contain longer running configurations
	 * of a project that may involve a project build.
	 * @param monitor
	 * @throws CoreException with errors that resulted during project
	 * configuration
	 */
	public abstract void configureProject(IProgressMonitor monitor) throws CoreException;

}
