/*******************************************************************************
 * Copyright (c) 2013, 2020 GoPivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.wizard;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.springframework.ide.eclipse.boot.wizard.starters.AddStartersWizard;
import org.springsource.ide.eclipse.commons.frameworks.ui.internal.utils.ProjectFilter;
import org.springsource.ide.eclipse.commons.frameworks.ui.internal.utils.SelectionUtils;
import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;
import org.springsource.ide.eclipse.commons.livexp.util.Log;

public class EditBootStartersHandler extends AbstractHandler {

	/**
	 * the command has been executed, so extract extract the needed information
	 * from the application context.
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Shell activeShell = HandlerUtil.getActiveShell(event);
		try {
			IStructuredSelection selection = HandlerUtil.getCurrentStructuredSelection(event);
			if (selection!=null) {
				// PT 169994346 - "Secret" way to open alternate Add Starters wizard.
				if (event.getTrigger() instanceof Event) {
					Event e = (Event) event.getTrigger();
					if ((e.stateMask & SWT.ALT) != 0) {
						AddStartersWizard.openFor(activeShell, selection);
						return null;
					}
				}

				IProject project = StartersWizardUtil.getProject(selection);
				if (project != null) {
					EditStartersDialog.openFor(project, activeShell);
				}
			}
		} catch (Throwable e) {
			Log.log(e);
			MessageDialog.openError(activeShell, "Error", ExceptionUtil.getMessage(e));
		}
		return null;
	}

}
