/*******************************************************************************
 * Copyright (c) 2013 GoPivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.gettingstarted.wizard;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;
import org.springframework.ide.eclipse.gettingstarted.GettingStartedActivator;
import org.springframework.ide.eclipse.gettingstarted.guides.GettingStartedGuide;
import org.springframework.ide.eclipse.wizard.WizardImages;
import org.springsource.ide.eclipse.commons.livexp.ui.WizardPageSection;
import org.springsource.ide.eclipse.commons.livexp.ui.WizardPageWithSections;
import org.springsource.ide.eclipse.gradle.core.util.ExceptionUtil;

/**
 * Generic import wizard for different types of getting started content.
 * <p>
 * The idea is that all content is shown together in single tree viewer,
 * grouped by type.
 * <p>
 * This is a generalization of the GuideImportWizard. 
 * 
 * @author Kris De Volder
 */
public class GSImportWizard extends Wizard implements IImportWizard {

	private GSImportWizardModel model = new GSImportWizardModel();
	
	public GSImportWizard() {
		setNeedsProgressMonitor(true);
		setDefaultPageImageDescriptor(WizardImages.TEMPLATE_WIZARD_ICON);
	}
	
	private static final ImageDescriptor IMAGE = WizardImages.TEMPLATE_WIZARD_ICON;
	   //TODO: Get our own icon for GSG wizard
	
	private PageOne pageOne = new PageOne(model);
	
	public class PageOne extends WizardPageWithSections {

		private GSImportWizardModel model;

		protected PageOne(GSImportWizardModel model) {
			super("Page One", "Import Getting Started Guide", IMAGE);
			this.model = model;
		}
		
		@Override
		protected List<WizardPageSection> createSections() {
			List<WizardPageSection> sections = new ArrayList<WizardPageSection>();

			sections.add(new ChooseTypedContentSection(this, model.getGuideSelectionModel()));
			sections.add(new DescriptionSection(this, model.description));
			sections.add(new BuildTypeRadiosSection(this, model.getBuildTypeModel()));
			sections.add(new CodeSetCheckBoxesSection(this, model.validCodesetNames, model.getCodeSetModel()));
			sections.add(new OpenUrlSection(this, "Home Page", model.homePage, model.getEnableOpenHomePage()));
			
			return sections;
		}
	}

//	private PageTwo pageTwo = new PageTwo(model);
//	
//	public class PageTwo extends WizardPageWithSections {
//
//		private GuideImportWizardModel model;
//
//		protected PageTwo(GuideImportWizardModel model) {
//			super("Page Two", "Import Getting Started Guide", IMAGE);
//			this.model = model;
//		}
//		
//		@Override
//		protected List<WizardPageSection> createSections() {
//			List<WizardPageSection> sections = new ArrayList<WizardPageSection>();
//
//			return sections;
//		}
//	}
	
	public void init(IWorkbench workbench, IStructuredSelection selection) {
//		this.workbench = workbench;
//		super.init(workbench, selection);
	}
	
	public void addPages() {
		super.addPages();
		addPage(pageOne);
//		addPage(pageTwo);
	}

	@Override
	public boolean performFinish() {
		try {
			//IMPORTANT: fork must be true or invalid thread access will ensue for gradle imports.
			// This is because the gradle import will send progress events from worker threads.
			// and the non-forked run will pass a UI-direct progress monitor to the runnable!
			getContainer().run(true, false, new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor mon) throws InvocationTargetException, InterruptedException {
					model.performFinish(mon);
							//new UIThreadProgressAdapter(getShell().getDisplay(), mon));
				}
			});
			return true;
		} catch (InterruptedException e) {
			GettingStartedActivator.log(e);
		} catch (InvocationTargetException e) {
			String msg = ExceptionUtil.getMessage(e);
			if (msg!=null && !"".equals(msg)) {
				MessageDialog.openError(getShell(), "Error performing the import", msg);
			}
			GettingStartedActivator.log(e);
		}
		return false;
	}

	/**
	 * Open the wizard and block until it is closed by the user. Returns the exit code of
	 * the wizard (e.g. indicating OK or CANCEL).
	 */
	public static int open(Shell shell, GettingStartedGuide guide) {
		GSImportWizard wiz = new GSImportWizard();
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
