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
 * Configures a project after it has been created. The framework may run the
 * configuration in a non-UI thread, therefore the configuration should not
 * contain dialogs and other UI. Configuration of the project is performed on
 * information carried by a configuration descriptor, which decouples the UI
 * from the project configuration. Instead of reading values directly from UI
 * into the configuration, use the descriptor to pass the necessary information
 * to the configuration.
 * 
 */
public abstract class ProjectConfiguration {

	private final IProjectConfigurationDescriptor projectConfigurationDescriptor;

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

	public abstract void configureProject(IProject project, IProgressMonitor monitor) throws CoreException;

}
