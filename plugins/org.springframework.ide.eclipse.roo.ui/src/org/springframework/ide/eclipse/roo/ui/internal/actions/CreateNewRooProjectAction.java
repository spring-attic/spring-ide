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
package org.springframework.ide.eclipse.roo.ui.internal.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.PlatformUI;
import org.springframework.ide.eclipse.roo.ui.RooUiActivator;
import org.springframework.ide.eclipse.roo.ui.internal.wizard.NewRooProjectWizard;


/**
 * {@link Action} implementation for launching the Roo project creation wizard.
 * @author Christian Dupuis
 * @since 2.3.0
 */
public class CreateNewRooProjectAction extends Action {
	
	private final Shell shell;
	
	public CreateNewRooProjectAction(Shell shell) {
		super("Create New Roo Project", RooUiActivator.getImageDescriptor("icons/full/obj16/new_project_obj.png"));
		this.shell = shell;
	}
	
	@Override
	public void run() {
		INewWizard wizard = new NewRooProjectWizard();
		wizard.init(PlatformUI.getWorkbench(), new StructuredSelection());
		WizardDialog dialog = new WizardDialog(shell, wizard);
		dialog.open();
	}
	
}
