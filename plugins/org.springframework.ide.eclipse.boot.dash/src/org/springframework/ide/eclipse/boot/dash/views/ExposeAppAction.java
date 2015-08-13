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

import java.util.Collection;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.springframework.ide.eclipse.boot.core.BootActivator;
import org.springframework.ide.eclipse.boot.dash.livexp.MultiSelection;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.BootDashViewModel;
import org.springframework.ide.eclipse.boot.dash.model.BootProjectDashElement;
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.RunTargetTypes;
import org.springframework.ide.eclipse.boot.dash.ngrok.NGROKClient;
import org.springframework.ide.eclipse.boot.dash.ngrok.NGROKInstallManager;

/**
 * @author Martin Lippert
 */
public class ExposeAppAction extends RunStateAction {

	private Process process;
	private NGROKInstallManager ngrokManager;

	public ExposeAppAction(BootDashViewModel model, MultiSelection<BootDashElement> selection,
			UserInteractions ui, RunState goalState, NGROKInstallManager ngrokManager) {
		super(model, selection, ui, goalState);
		Assert.isLegal(goalState == RunState.RUNNING || goalState == RunState.DEBUGGING);

		this.ngrokManager = ngrokManager;
	}

	@Override
	protected boolean appliesToElement(BootDashElement bootDashElement) {
		return bootDashElement.getTarget().getType().equals(RunTargetTypes.LOCAL);
	}

	@Override
	protected Job createJob() {
		final Collection<BootDashElement> selecteds = getSelectedElements();
		if (!selecteds.isEmpty()) {
			String ngrokInstall = this.ngrokManager.getDefaultInstall();
			if (ngrokInstall == null) {
				ngrokInstall = ui.chooseFile("ngrok installation", null);
				if (ngrokInstall != null) {
					this.ngrokManager.addInstall(ngrokInstall);
					this.ngrokManager.setDefaultInstall(ngrokInstall);
					this.ngrokManager.save();
				}
			}

			if (ngrokInstall != null) {
				final NGROKClient ngrokClient = new NGROKClient(ngrokInstall);

				return new Job("Restarting " + selecteds.size() + " Dash Elements") {
					@Override
					public IStatus run(IProgressMonitor monitor) {
						monitor.beginTask("Restart Boot Dash Elements", selecteds.size());
						try {
							for (BootDashElement el : selecteds) {
								if (el instanceof BootProjectDashElement) {
									monitor.subTask("Restarting: " + el.getName());
									try {
										BootProjectDashElement localDashProject = (BootProjectDashElement) el;
										localDashProject.restartAndExpose(ngrokClient, ui);
									} catch (Exception e) {
										return BootActivator.createErrorStatus(e);
									}
								}
								monitor.worked(1);
							}
							return Status.OK_STATUS;
						} finally {
							monitor.done();
						}
					}

				};
			}
		}
		return null;
	}

	@Override
	public void dispose() {
		if (process != null) {
			process.destroy();
		}
		super.dispose();
	}

}
