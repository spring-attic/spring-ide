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
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PlatformUI;
import org.springframework.ide.eclipse.wizard.WizardPlugin;

/**
 * Configures an existing project based on a template selection, and if
 * necessary, adds the project to a working set. Can be run from a non-UI
 * thread.
 */
public class ProjectConfigurationOperation {

	private final IWorkingSet[] workingSetsToAdd;

	private final IProject project;

	private final ProjectConfiguration projectConfiguration;

	public ProjectConfigurationOperation(IProject project, ProjectConfiguration projectConfiguration,
			IWorkingSet[] workingSetsToAdd) {
		this.project = project;
		this.workingSetsToAdd = workingSetsToAdd;
		this.projectConfiguration = projectConfiguration;
	}

	public void run(IProgressMonitor monitor) throws CoreException {
		if (project == null) {
			throw new CoreException(new Status(IStatus.ERROR, WizardPlugin.PLUGIN_ID,
					"No project was created and configured"));
		}
		if (projectConfiguration != null) {
			projectConfiguration.configureProject(project, monitor);
		}
		if (workingSetsToAdd != null && workingSetsToAdd.length > 0) {
			PlatformUI.getWorkbench().getWorkingSetManager().addToWorkingSets(project, workingSetsToAdd);
		}
	}
}
