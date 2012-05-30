/*******************************************************************************
 *  Copyright (c) 2012 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.roo.ui.internal.actions;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.springframework.ide.eclipse.roo.ui.internal.RooShellView;


public class OpenShellJob extends Job {

	private final IProject project;

	public OpenShellJob(IProject project) {
		super(String.format("Opening Roo shell for project '%s'", project.getName()));
		this.project = project;
		setPriority(Job.INTERACTIVE);
		setSystem(true);
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		Display.getDefault().asyncExec(new Runnable() {

			public void run() {
				try {
					RooShellView view = (RooShellView) PlatformUI.getWorkbench().getActiveWorkbenchWindow()
							.getActivePage().showView(RooShellView.VIEW_ID, null,
									IWorkbenchPage.VIEW_ACTIVATE);
					view.openShell(project);
				}
				catch (Exception e) {
				}
			}
		});
		return Status.OK_STATUS;
	}

}
