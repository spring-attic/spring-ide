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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.springframework.ide.eclipse.beans.ui.livegraph.model.LiveBeansModel;
import org.springframework.ide.eclipse.beans.ui.livegraph.model.LiveBeansModelGenerator;
import org.springframework.ide.eclipse.beans.ui.livegraph.views.LiveBeansGraphView;

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
			LiveBeansModel model = LiveBeansModelGenerator.connectToModel(dialog.getServiceUrl(), dialog.getUsername(),
					dialog.getPassword(), dialog.getApplicationName());
			view.setInput(model);
		}
	}

}
