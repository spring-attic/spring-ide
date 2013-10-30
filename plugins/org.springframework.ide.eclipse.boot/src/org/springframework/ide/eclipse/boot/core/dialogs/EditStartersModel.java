/*******************************************************************************
 * Copyright (c) 2013 GoPivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.core.dialogs;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.springframework.ide.eclipse.boot.core.BootActivator;
import org.springframework.ide.eclipse.boot.core.ISpringBootProject;
import org.springframework.ide.eclipse.boot.core.SpringBootCore;
import org.springframework.ide.eclipse.boot.core.SpringBootStarter;
import org.springsource.ide.eclipse.commons.core.util.ExceptionUtil;
import org.springsource.ide.eclipse.commons.livexp.core.LiveSet;
import org.springsource.ide.eclipse.commons.livexp.ui.OkButtonHandler;

public class EditStartersModel implements OkButtonHandler {

	private final ISpringBootProject project;
	public final LiveSet<SpringBootStarter> starters;

	/**
	 * Create EditStarters dialog model and initialize it based on a project selection.
	 */
	public EditStartersModel(IProject selectedProject) throws CoreException {
		this.project = SpringBootCore.create(selectedProject);
		this.starters = new LiveSet<SpringBootStarter>();
		starters.addAll(project.getBootStarters());
	}

	public SpringBootStarter[] getAvailableStarters() throws CoreException {
		return project.getKnownStarters().toArray(new SpringBootStarter[0]);
	}

	public String getProjectName() {
		return project.getProject().getName();
	}

	public void performOk() {
		Job job = new Job("Modifying starters for "+getProjectName()) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					project.setStarters(starters.getValues());
					return Status.OK_STATUS;
				} catch (CoreException e) {
					BootActivator.log(e);
					return ExceptionUtil.status(e);
				}
			}
		};
		job.setRule(ResourcesPlugin.getWorkspace().getRuleFactory().buildRule());
		job.schedule();
	}

}
