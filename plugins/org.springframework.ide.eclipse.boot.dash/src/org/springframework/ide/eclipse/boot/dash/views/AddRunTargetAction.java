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

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryUiUtil;
import org.springframework.ide.eclipse.boot.dash.livexp.MultiSelection;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.RunTarget;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springsource.ide.eclipse.commons.livexp.core.LiveSet;

public class AddRunTargetAction extends AbstractBootDashAction {

	private LiveSet<RunTarget> targets;

	public AddRunTargetAction(LiveSet<RunTarget> targets, MultiSelection<BootDashElement> selection, UserInteractions ui) {
		super(selection, ui);
		this.targets = targets;
		this.setText("Add a Run Target");
		this.setToolTipText("Add a Run Target");
		this.setImageDescriptor(BootDashActivator.getImageDescriptor("icons/add_target.png"));
	}

	@Override
	public void run() {

		Display.getCurrent().asyncExec(new Runnable() {

			@Override
			public void run() {
				RunTargetWizard wizard = new RunTargetWizard(targets);
				Shell shell = CloudFoundryUiUtil.getShell();
				if (shell != null) {
					WizardDialog dialog = new WizardDialog(shell, wizard);
					if (dialog.open() == Dialog.OK) {
						RunTarget target = wizard.getRunTarget();
						if (target != null) {
							targets.add(target);
						}
					}
				}
			}
		});
	}

	@Override
	public void updateEnablement() {
		// Always enable
		this.setEnabled(true);
	}

}
