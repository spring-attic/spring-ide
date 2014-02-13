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
package org.springframework.ide.eclipse.beans.ui.livegraph.actions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.springframework.ide.eclipse.beans.ui.livegraph.LiveGraphUiPlugin;
import org.springframework.ide.eclipse.beans.ui.livegraph.model.LiveBeansModel;
import org.springframework.ide.eclipse.beans.ui.livegraph.model.LiveBeansModelGenerator;
import org.springframework.ide.eclipse.beans.ui.livegraph.views.LiveBeansGraphView;
import org.springsource.ide.eclipse.commons.core.StatusHandler;

/**
 * @author Leo Dos Santos
 */
public class ConnectToApplicationAction extends Action {

	private final LiveBeansGraphView view;

	private final ConnectToApplicationDialog dialog;

	public ConnectToApplicationAction(LiveBeansGraphView view) {
		super("Connect to Application...");
		this.view = view;
		this.dialog = new ConnectToApplicationDialog(view.getSite().getShell());
	}

	@Override
	public void run() {
		if (dialog.open() == IDialogConstants.OK_ID) {
			try {
				LiveBeansModel model = LiveBeansModelGenerator.connectToModel(dialog.getServiceUrl(),
						dialog.getUsername(), dialog.getPassword(), dialog.getApplicationName(), /*project*/null);
				view.setInput(model);
			}
			catch (CoreException e) {
				Status status = new Status(IStatus.INFO, LiveGraphUiPlugin.PLUGIN_ID, e.getMessage(), e);
				ErrorDialog
						.openError(
								dialog.getShell(),
								"Connection Failed",
								"Could not connect to the given server or application.\n\n"
										+ "Please ensure that the server is configured for JMX access and that the host name and port are correct. "
										+ "If the server requires authentication, please provide a username and password. "
										+ "This feature is only supported for applications on Spring Framework 3.2 or greater.\n\n"
										+ "See the Error Log for more details.", status);
				StatusHandler.log(status);
			}
		}
	}

}
