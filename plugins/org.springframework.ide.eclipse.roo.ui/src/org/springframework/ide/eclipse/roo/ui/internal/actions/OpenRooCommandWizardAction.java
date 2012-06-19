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
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.progress.UIJob;
import org.springframework.ide.eclipse.roo.ui.RooUiActivator;
import org.springframework.ide.eclipse.roo.ui.internal.RooShellView;


/**
 * {@link Action} implementation that opens up the Roo Command Wizard.
 * @author Christian Dupuis
 * @since 2.5.0
 */
public class OpenRooCommandWizardAction extends Action {
	
	private final RooShellView view;

	public OpenRooCommandWizardAction(RooShellView view) {
		super("Open Roo Command Wizard", RooUiActivator.getImageDescriptor("icons/full/obj16/command_obj.png"));
		this.view = view;
		setEnabled(false);
	}

	@Override
	public void run() {
		IProject project = view.getActiveProject();
		if (project != null) {
			RooCommandWizardActionDelegate delegate = new RooCommandWizardActionDelegate();
			delegate.selectionChanged(this, new StructuredSelection(project));
			delegate.run(this);
		}
	}
	
	public void update() {
		UIJob job = new UIJob("") { //$NON-NLS-1$
			public IStatus runInUIThread(IProgressMonitor monitor) {
				setEnabled(isEnabled());
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		job.setPriority(Job.INTERACTIVE);
		job.schedule();
	}
	
	@Override
	public boolean isEnabled() {
		return view.getActiveProject() != null;
	}
}
