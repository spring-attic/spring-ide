/*******************************************************************************
 * Copyright (c) 2015 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.views;

import java.net.URL;

import org.apache.commons.lang.RandomStringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudDashElement;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.DevtoolsUtil;
import org.springframework.ide.eclipse.boot.dash.livexp.MultiSelection;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.BootDashViewModel;
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel.ElementStateListener;

/**
 * Action for starting/restarting Remove DevTools Client application
 *
 * @author Alex Boyko
 *
 */
public class RestartRemoteDevClientAction extends AbstractBootDashElementsAction {

	private BootDashViewModel model;
	private ElementStateListener stateListener;

	public RestartRemoteDevClientAction(BootDashViewModel model, MultiSelection<BootDashElement> selection, UserInteractions ui) {
		super(selection, ui);
		this.model = model;
		this.setText("(Re)start Remote DevTools Client");
		this.setToolTipText("Restarts the Remote DevTools Client in RUN mode.");
		URL url = FileLocator.find(Platform.getBundle("org.springframework.ide.eclipse.boot"), new Path("resources/icons/boot-devtools-icon.png"), null);
		if (url != null) {
			this.setImageDescriptor(ImageDescriptor.createFromURL(url));
		}
		if (model != null) {
			model.addElementStateListener(stateListener = new ElementStateListener() {
				public void stateChanged(BootDashElement e) {
					if (getSelectedElements().contains(e)) {
						Display.getDefault().asyncExec(new Runnable() {
							public void run() {
								updateEnablement();
							}
						});
					}
				}
			});
		}
	}

	@Override
	public void updateEnablement() {
		boolean enable = false;
		if (!getSelectedElements().isEmpty()) {
			enable = true;
			for (BootDashElement e : getSelectedElements()) {
				if (!(e instanceof CloudDashElement) || e.getProject() == null || e.getRunState() != RunState.RUNNING || e.getLiveHost() == null) {
					enable = false;
				}
			}
		}
		this.setEnabled(enable);
	}

	@Override
	public void run() {
		for (BootDashElement e : getSelectedElements()) {
			if (e instanceof CloudDashElement) {
				final CloudDashElement cde = (CloudDashElement) e;
				Job job = new Job("Restarting Remote DevTools Client for '" + cde.getName() +"'") {
					@Override
					protected IStatus run(IProgressMonitor monitor) {
						if (DevtoolsUtil.isDevClientAttached(cde, null)) {
							DevtoolsUtil.disconnectDevtoolsClientsFor(cde);
						}
						try {
							DevtoolsUtil.launchDevtools(cde, RandomStringUtils.randomAlphabetic(20), ILaunchManager.RUN_MODE, monitor);
						} catch (CoreException e) {
							return e.getStatus();
						}
						return Status.OK_STATUS;
					}

				};
				job.schedule();
			}
		}
	}

	@Override
	public void dispose() {
		if (model != null && stateListener != null) {
			model.removeElementStateListener(stateListener);
			stateListener = null;
		}
		super.dispose();
	}

}
