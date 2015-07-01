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
package org.springframework.ide.eclipse.boot.dash.cloudfoundry;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.livexp.MultiSelection;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springframework.ide.eclipse.boot.dash.views.AbstractBootDashAction;

public class AddCloudFoundryTargetAction extends AbstractBootDashAction {

	
	private final Shell shell;
	
	public AddCloudFoundryTargetAction(MultiSelection<BootDashElement> selection, UserInteractions ui, Shell shell) {
		super(selection, ui);
		this.shell = shell;
		this.setText("Add Cloud Foundry Target");
		this.setToolTipText("Add Cloud Foundry Target");
		this.setImageDescriptor(BootDashActivator.getImageDescriptor("icons/cloud_obj.png"));
	}

	@Override
	public void run() {

		Display.getCurrent().asyncExec(new Runnable() {

			@Override
			public void run() {
				CloudFoundryTargetWizard wizard = new CloudFoundryTargetWizard();
				WizardDialog dialog = new WizardDialog(shell, wizard);
				if (dialog.open() == Dialog.OK) {
					CloudFoundryTargetProperties targetProperties = wizard.getTargetProperties();
				}
			}
		});
	}
}
