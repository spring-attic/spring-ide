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
package org.springframework.ide.eclipse.wizard.gettingstarted.boot;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.springframework.ide.eclipse.wizard.WizardImages;
import org.springframework.ide.eclipse.wizard.WizardPlugin;
import org.springframework.ide.eclipse.wizard.gettingstarted.guides.DescriptionSection;
import org.springsource.ide.eclipse.commons.core.util.ExceptionUtil;
import org.springsource.ide.eclipse.commons.livexp.core.FieldModel;
import org.springsource.ide.eclipse.commons.livexp.ui.GroupSection;
import org.springsource.ide.eclipse.commons.livexp.ui.ProjectLocationSection;
import org.springsource.ide.eclipse.commons.livexp.ui.StringFieldSection;
import org.springsource.ide.eclipse.commons.livexp.ui.WizardPageSection;
import org.springsource.ide.eclipse.commons.livexp.ui.WizardPageWithSections;

public class NewSpringBootWizard extends Wizard implements INewWizard, IImportWizard {

	private static final ImageDescriptor IMAGE = WizardImages.TEMPLATE_WIZARD_ICON;

	private NewSpringBootWizardModel model;

	public NewSpringBootWizard() throws Exception {
		setDefaultPageImageDescriptor(IMAGE);
	}

	//@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		try {
			model = new NewSpringBootWizardModel();
		} catch (Exception e) {
			//Ensure exception is logged. (Eclipse UI may not log it!).
			WizardPlugin.log(e);
			//rethrow, attempts to proceed without model will just throw NPE anyhow.
			throw new Error(e);
		}
	}

	@Override
	public void addPages() {
		super.addPages();
		addPage(new PageOne());
//		addPage(pageTwo);
	}

	public class PageOne extends WizardPageWithSections {

		protected PageOne() {
			super("page1", "New Spring Boot Project", null);
		}

		@Override
		protected List<WizardPageSection> createSections() {
			List<WizardPageSection> sections = new ArrayList<WizardPageSection>();

			FieldModel<String> projectName = model.getProjectName();
			sections.add(new StringFieldSection(this, projectName));
			sections.add(new ProjectLocationSection(this, model.getLocation(), projectName.getVariable(), model.getLocationValidator()));

			for (FieldModel<String> f : model.stringInputs) {
				//caution! we already created the section for projectName because we want it at the top
				if (projectName!=f) {
					sections.add(new StringFieldSection(this, f));
				}
			}

			sections.add(
				new CheckBoxesSection<String>(this, model.style)
					.columns(3)
			);

			sections.add(new GroupSection(this, "Initializr Site Info",
					new StringFieldSection(this, "Base Url", model.baseUrl, model.baseUrlValidator),
					new DescriptionSection(this, model.downloadUrl).label("Full Url").readOnly(false)
			));

			return sections;
		}

	}

	@Override
	public boolean performFinish() {
		Job job = new Job("Import Getting Started Content") {
			@Override
			protected IStatus run(IProgressMonitor mon) {
				try {
					model.performFinish(mon);
					return Status.OK_STATUS;
				} catch (Throwable e) {
					return ExceptionUtil.status(e);
				}
			}
		};
		//job.setRule(ResourcesPlugin.getWorkspace().getRuleFactory().buildRule());
		job.setPriority(Job.BUILD);
		job.setUser(true); //shows progress in default eclipse config
		job.schedule();
		return true;
	}


}
