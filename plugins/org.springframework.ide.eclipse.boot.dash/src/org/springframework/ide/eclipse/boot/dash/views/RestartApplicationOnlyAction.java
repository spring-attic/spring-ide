/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.views;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudDashElement;
import org.springframework.ide.eclipse.boot.dash.livexp.MultiSelection;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;

public class RestartApplicationOnlyAction extends AbstractBootDashElementsAction {

	public RestartApplicationOnlyAction(MultiSelection<BootDashElement> selection, UserInteractions ui) {
		super(selection, ui);
		Assert.isNotNull(ui);
		this.setText("Restart Only");
		this.setToolTipText("Restarts the application without uploading changes.");
		this.setImageDescriptor(BootDashActivator.getImageDescriptor("icons/restart.gif"));
	}

	@Override
	public void run() {
		Job job = new Job("Restarting apps") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				for (BootDashElement el : getSelectedElements()) {

					if (el instanceof CloudDashElement) {
						try {
							((CloudDashElement) el).restartOnly(RunState.RUNNING, ui);
						} catch (Exception e) {
							ui.errorPopup("Error restarting application", e.getMessage());
						}
					}
				}
				return Status.OK_STATUS;
			}

		};
		job.schedule();
	}

	@Override
	public void updateEnablement() {
		boolean enable = false;
		if (!getSelectedElements().isEmpty()) {
			enable = true;
			for (BootDashElement e : getSelectedElements()) {
				if (!(e instanceof CloudDashElement)) {
					enable = false;
				}
			}
		}

		this.setEnabled(enable);
	}

}
