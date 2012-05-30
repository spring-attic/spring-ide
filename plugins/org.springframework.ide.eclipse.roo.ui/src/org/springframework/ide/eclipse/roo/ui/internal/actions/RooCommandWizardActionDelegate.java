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

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.springframework.ide.eclipse.roo.ui.internal.RooShellTab;
import org.springframework.ide.eclipse.roo.ui.internal.RooShellView;
import org.springframework.ide.eclipse.roo.ui.internal.wizard.RooCommandWizard;
import org.springsource.ide.eclipse.commons.frameworks.core.internal.commands.ICommandListener;
import org.springsource.ide.eclipse.commons.frameworks.core.internal.commands.IFrameworkCommand;
import org.springsource.ide.eclipse.commons.frameworks.ui.internal.actions.AbstractCommandActionDelegate;
import org.springsource.ide.eclipse.commons.frameworks.ui.internal.wizard.GenericCommandWizard;


/**
 * Action to open the Roo Command Wizard
 * @author Christian Dupuis
 * @since 2.5.0
 */
@SuppressWarnings("restriction")
public class RooCommandWizardActionDelegate extends AbstractCommandActionDelegate {

	private RooShellTab tab;

	protected void addCommands(ICommandListener listener) {
		if (listener != null && tab != null) {
			tab.addCommands(listener);
		}
	}

	protected GenericCommandWizard getCommandWizard(Collection<IProject> projects, IFrameworkCommand command) {
		IProject project = getSelectedProjects().get(0);

		try {
			IWorkbenchPart workbench = JavaPlugin.getActiveWorkbenchWindow().getActivePage().getActivePart();
			RooShellView view = (RooShellView) workbench.getSite().getPage()
					.showView(RooShellView.VIEW_ID, null, IWorkbenchPage.VIEW_ACTIVATE);
			tab = view.openShell(project);
		}
		catch (PartInitException e) {
			// TODO CD what to do here
		}

		projects = new ArrayList<IProject>();
		projects.add(project);
		RooCommandWizard wizard = new RooCommandWizard(projects, command, tab);
		addCommands(wizard);

		return wizard;
	}

}
