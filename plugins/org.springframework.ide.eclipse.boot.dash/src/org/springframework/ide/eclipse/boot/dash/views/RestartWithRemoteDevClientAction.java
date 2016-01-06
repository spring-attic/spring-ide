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

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudDashElement;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryBootDashModel;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.ops.ApplicationStartWithRemoteClientOperation;
import org.springframework.ide.eclipse.boot.dash.livexp.MultiSelection;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel.ElementStateListener;
import org.springframework.ide.eclipse.boot.dash.model.BootDashViewModel;
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;

/**
 * Action for starting/restarting Remove DevTools Client application
 *
 * @author Alex Boyko
 *
 */
public class RestartWithRemoteDevClientAction extends AbstractBootDashElementsAction {

	private BootDashViewModel model;
	private ElementStateListener stateListener;

	public RestartWithRemoteDevClientAction(BootDashViewModel model, MultiSelection<BootDashElement> selection, UserInteractions ui) {
		super(selection, ui);
		this.model = model;
		this.setText("(Re)start Remote DevTools Client");
		this.setToolTipText("Restarts application with the Remote DevTools Client attched.");
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
				if (!(e instanceof CloudDashElement) || e.getProject() == null) {
					enable = false;
				}
			}
		}
		this.setEnabled(enable);
	}

	@Override
	public void run() {
		for (BootDashElement _e : getSelectedElements()) {
			if (_e instanceof CloudDashElement && _e.getBootDashModel() instanceof CloudFoundryBootDashModel && _e.getProject() != null) {
				CloudDashElement e = (CloudDashElement) _e;
				CloudFoundryBootDashModel model = (CloudFoundryBootDashModel) e.getBootDashModel();
				String opName = "Restart Remote DevTools Client for application '" + e.getName() + "'";
				model.getOperationsExecution().runOpAsynch(new ApplicationStartWithRemoteClientOperation(opName, e,
						RunState.RUNNING, model.getApplicationDeploymentOperations(), ui));
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
