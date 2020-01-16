/*******************************************************************************
 * Copyright (c) 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.wizard.starters;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.PlatformUI;
import org.springframework.ide.eclipse.boot.core.BootActivator;
import org.springframework.ide.eclipse.boot.core.SpringBootCore;
import org.springframework.ide.eclipse.boot.core.initializr.InitializrService;
import org.springframework.ide.eclipse.boot.wizard.BootWizardActivator;
import org.springframework.ide.eclipse.boot.wizard.InitializrFactoryModel;
import org.springframework.ide.eclipse.boot.wizard.StartersWizardUtil;
import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;

public class AddStartersWizard extends Wizard implements IWorkbenchWizard {

	private InitializrFactoryModel<AddStartersModel> fmodel;

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		try {
			IProject project = StartersWizardUtil.getProject(selection);
			if (project != null) {
				IPreferenceStore preferenceStore = BootActivator.getDefault().getPreferenceStore();
				fmodel = new InitializrFactoryModel<>((url) -> {
					if (url!=null) {
						InitializrService initializr = InitializrService.create(BootActivator.getUrlConnectionFactory(), () -> url);
						SpringBootCore core = new SpringBootCore(initializr);
						return new AddStartersModel(
								project,
								core,
								preferenceStore
						);
					}
					return null;
				});
			}
		} catch (Exception e) {
			MessageDialog.openError(workbench.getActiveWorkbenchWindow().getShell(), "Error opening the wizard",
					ExceptionUtil.getMessage(e)+"\n\n"+
					"Note that this wizard uses a webservice and needs internet access.\n"+
					"A more detailed error message may be found in the Eclipse error log."
			);
			//Ensure exception is logged. (Eclipse UI may not log it!).
			BootWizardActivator.log(e);
			throw new Error(e);
		}
	}

	@Override
	public boolean performFinish() {
		return true;
	}

	@Override
	public void addPages() {
		super.addPages();
		addPage(new DependencyPage(fmodel));
		addPage(new CompareGeneratedAndCurrentPage(fmodel));
	}

	public static void openFor(Shell shell, IStructuredSelection selection) throws CoreException {
		IProject project = StartersWizardUtil.getProject(selection);
		if (project != null) {
			int promptResult = StartersWizardUtil.promptIfPomFileDirty(project, shell);
			if (promptResult == MessageDialog.OK) {
				AddStartersWizard addStartersWizard = new AddStartersWizard();
				addStartersWizard.init(PlatformUI.getWorkbench(), selection);
				new WizardDialog(shell, addStartersWizard).open();
			}
		}
	}
}
