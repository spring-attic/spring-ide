/*******************************************************************************
 *  Copyright (c) 2012 - 2013 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.roo.ui.internal.actions;

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
 * An action to open a Roo Shell tab for a group of projects
 * @author Christian Dupuis
 * @author Leo Dos Santos
 * @since 2.1.0
 */
public class RooAddOnManagerAction extends Action {

	private final RooShellView view;

	public RooAddOnManagerAction(RooShellView view) {
		super("Roo Add-on Manager", RooUiActivator.getImageDescriptor("icons/full/obj16/addon.gif"));
		this.view = view;
		setEnabled(false);
	}

	@Override
	public void run() {
		RooAddOnManagerActionDelegate delegate = new RooAddOnManagerActionDelegate();
		if (view.getActiveProject() != null) {
			delegate.selectionChanged(this, new StructuredSelection(view.getActiveProject()));
		}
		// Currently Felix doesn't allow us to support installing shared addons,
		// so active project must not be null.
		//
		// else {
		// delegate.selectionChanged(this, new
		// StructuredSelection(RooUiUtil.getAllRooProjects()));
		// }
		delegate.run(view);
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
