/*******************************************************************************
 * Copyright (c) 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.wizard.starters;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.PlatformUI;
import org.springframework.ide.eclipse.boot.core.BootActivator;
import org.springframework.ide.eclipse.boot.core.BootPreferences;
import org.springframework.ide.eclipse.boot.core.SpringBootCore;
import org.springframework.ide.eclipse.boot.core.initializr.InitializrProjectDownloader;
import org.springframework.ide.eclipse.boot.core.initializr.InitializrService;
import org.springframework.ide.eclipse.boot.core.initializr.InitializrUrlBuilders;
import org.springframework.ide.eclipse.boot.wizard.InitializrFactoryModel;
import org.springframework.ide.eclipse.boot.wizard.StartersWizardUtil;
import org.springsource.ide.eclipse.commons.frameworks.core.downloadmanager.URLConnectionFactory;
import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;
import org.springsource.ide.eclipse.commons.livexp.util.Log;

public class AddStartersWizard extends Wizard implements IWorkbenchWizard {

	private InitializrFactoryModel<AddStartersModel> fmodel;
	private CompareGeneratedAndCurrentPage comparePage;

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		try {
			IProject project = StartersWizardUtil.getProject(selection);
			if (project != null) {
				IPreferenceStore preferenceStore = BootActivator.getDefault().getPreferenceStore();
				fmodel = new InitializrFactoryModel<>((url) -> {
					if (url!=null) {
						URLConnectionFactory urlConnectionFactory = BootActivator.getUrlConnectionFactory();
						String initializrUrl = BootPreferences.getInitializrUrl();

						InitializrService initializr = InitializrService.create(urlConnectionFactory, () -> url);
						SpringBootCore core = new SpringBootCore(initializr);

						InitializrUrlBuilders urlBuilders = new InitializrUrlBuilders();
						InitializrProjectDownloader projectDownloader = new InitializrProjectDownloader(
								urlConnectionFactory, initializrUrl, urlBuilders);

						return new AddStartersModel(projectDownloader, project, core, preferenceStore);
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
			Log.log(e);
			throw new Error(e);
		}
	}

	@Override
	public boolean canFinish() {
		if (this.comparePage != null) {
			return this.comparePage.isPageComplete();
		}
		return super.canFinish();
	}

	@Override
	public boolean performFinish() {
		fmodel.getModel().getValue().performOk();
		return true;
	}

	@Override
	public void addPages() {
		super.addPages();
		addPage(new DependencyPage(fmodel));
		this.comparePage = new CompareGeneratedAndCurrentPage(fmodel);
		addPage(comparePage);
	}

	public static void openFor(Shell shell, IStructuredSelection selection) throws CoreException {
		IProject project = StartersWizardUtil.getProject(selection);
		if (project != null) {
			int promptResult = StartersWizardUtil.promptIfPomFileDirty(project, shell);
			if (promptResult == MessageDialog.OK) {
				AddStartersWizard addStartersWizard = new AddStartersWizard();
				addStartersWizard.init(PlatformUI.getWorkbench(), selection);
				new WizardDialog(shell, addStartersWizard) {

					@Override
					public void run(boolean fork, boolean cancelable, IRunnableWithProgress runnable)
							throws InvocationTargetException, InterruptedException {
						super.run(fork, cancelable, runnable);
						// The above restores the UI state to what it was before executing the runnable
						// If after the execution UI state updates (i.e. buttons enabled) restore state
						// following the execution of the runnable would wipe out the correct UI state.
						// Therefore update the UI after the execution to minimize the effect of
						// restoreUiState
						updateButtons();
					}

				}.open();
			}
		}
	}

	@Override
	public boolean needsProgressMonitor() {
		return true;
	}


}
