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

import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.livexp.MultiSelection;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.BootDashViewModel;
import org.springframework.ide.eclipse.boot.dash.model.RunTarget;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;

public class RemoveRunTargetAction extends AbstractBootDashElementsAction {

	private BootDashViewModel model;
	private RunTarget runTargetToRemove;

	public RemoveRunTargetAction(RunTarget runTargetToRemove, BootDashViewModel model,
			MultiSelection<BootDashElement> selection, UserInteractions ui) {
		super(selection, ui);
		this.runTargetToRemove = runTargetToRemove;

		this.model = model;
		this.setText("Remove Target");
		this.setToolTipText("Remove the connection to the target and its dashboard section.");
		this.setImageDescriptor(BootDashActivator.getImageDescriptor("icons/remove_target.gif"));
	}

	@Override
	public void run() {
		model.removeTarget(runTargetToRemove, ui);
	}

	@Override
	public void updateEnablement() {
		// Always enable
		this.setEnabled(true);
	}

}
