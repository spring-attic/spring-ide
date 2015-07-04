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
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;

public class RefreshRunTargetAction extends AbstractBootDashAction {

	private BootDashModel model;

	public RefreshRunTargetAction(BootDashModel model, MultiSelection<BootDashElement> selection, UserInteractions ui) {
		super(selection, ui);
		this.model = model;
		this.setText("Refresh");
		this.setToolTipText("Manually refresh contents of the section");
		this.setImageDescriptor(BootDashActivator.getImageDescriptor("icons/refresh.gif"));
	}

	@Override
	public void run() {
		model.refresh();
	}

	@Override
	public void updateEnablement() {
		// Always enable
		this.setEnabled(true);
	}

}
