/*******************************************************************************
 * Copyright (c) 2013 VMWare, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * VMWare, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.gettingstarted.guides.wizard;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;
import org.springframework.ide.gettingstarted.guides.GettingStartedGuide;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;

/**
 * @author Kris De Volder
 */
public class GuideImportWizard extends Wizard implements IImportWizard {

	private GuideImportWizardModel model = new GuideImportWizardModel();
	private GuideImportWizardPageOne pageOne;
//	private IWorkbench workbench;
	
	public GuideImportWizard() {
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
//		this.workbench = workbench;
//		super.init(workbench, selection);
	}
	
	public void addPages() {
		super.addPages();
		addPage(getPageOne());
	}

	private GuideImportWizardPageOne getPageOne() {
		if (pageOne==null) {
			pageOne = new GuideImportWizardPageOne(model);
		}
		return pageOne;
	}
	
//	public GradleImportOperation createOperation() {
//		return getPageOne().createOperation();
//	}

	@Override
	public boolean performFinish() {
//		getPageOne().wizardAboutToFinish();
//		final GradleImportOperation operation = createOperation();
//		JobUtil.userJob(new GradleRunnable("Import Gradle projects") {
//			@Override
//			public void doit(IProgressMonitor monitor) throws OperationCanceledException, CoreException {
//				ErrorHandler eh = ErrorHandler.forImportWizard();
//				operation.perform(eh, monitor);
//				eh.rethrowAsCore();
//			}
//		});
		return true;
	}

	/**
	 * Open the wizard and block until it is closed by the user. Returns the exit code of
	 * the wizard (e.g. indicating OK or CANCEL).
	 */
	public static int open(Shell shell, GettingStartedGuide guide) {
		GuideImportWizard wiz = new GuideImportWizard();
		wiz.setGuide(guide);
		WizardDialog dialog = new WizardDialog(shell, wiz);
		dialog.setBlockOnOpen(true);
		return dialog.open(); 
	}

	/**
	 * Sets the default selection for the guide that is going to be imported. 
	 */
	public void setGuide(GettingStartedGuide guide) {
		this.model.setGuide(guide);
	}
	
}
