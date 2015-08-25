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
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.springframework.ide.eclipse.wizard.WizardImages;
import org.springframework.ide.eclipse.wizard.WizardPlugin;
import org.springframework.ide.eclipse.wizard.gettingstarted.boot.json.InitializrServiceSpec.Dependency;
import org.springframework.ide.eclipse.wizard.gettingstarted.guides.DescriptionSection;
import org.springsource.ide.eclipse.commons.frameworks.core.ExceptionUtil;
import org.springsource.ide.eclipse.commons.livexp.core.FieldModel;
import org.springsource.ide.eclipse.commons.livexp.ui.ChooseOneSectionCombo;
import org.springsource.ide.eclipse.commons.livexp.ui.CommentSection;
import org.springsource.ide.eclipse.commons.livexp.ui.ExpandableSection;
import org.springsource.ide.eclipse.commons.livexp.ui.GroupSection;
import org.springsource.ide.eclipse.commons.livexp.ui.ProjectLocationSection;
import org.springsource.ide.eclipse.commons.livexp.ui.StringFieldSection;
import org.springsource.ide.eclipse.commons.livexp.ui.WizardPageSection;
import org.springsource.ide.eclipse.commons.livexp.ui.WizardPageWithSections;

public class NewSpringBootWizard extends Wizard implements INewWizard, IImportWizard {

	private static final ImageDescriptor IMAGE = WizardImages.TEMPLATE_WIZARD_ICON;

	private NewSpringBootWizardModel model;

	private IStructuredSelection selection;

	private WorkingSetSection workingSetSection;

	private ProjectDetailsPage projectPage;

	public NewSpringBootWizard() throws Exception {
		setDefaultPageImageDescriptor(IMAGE);
	}

	//@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		try {
			model = new NewSpringBootWizardModel();
			this.selection = selection;
		} catch (Exception e) {
			MessageDialog.openError(workbench.getActiveWorkbenchWindow().getShell(), "Error opening the wizard",
					ExceptionUtil.getMessage(e)+"\n\n"+
					"Note that this wizard uses a webservice and needs internet access.\n"+
					"A more detailed error message may be found in the Eclipse error log."
			);
			//Ensure exception is logged. (Eclipse UI may not log it!).
			WizardPlugin.log(e);
			throw new Error(e);
		}
	}

	@Override
	public void addPages() {
		super.addPages();
		addPage(projectPage = new ProjectDetailsPage());
		addPage(new DependencyPage());
		addPage(new PageThree());
	}

	@Override
	public boolean canFinish() {
		return super.canFinish() && getContainer().getCurrentPage()!=projectPage;
	}

	private WizardPageSection createRadioGroupsSection(WizardPageWithSections owner) {
		boolean notEmpty = false;
		RadioGroup bootVersion = model.getBootVersion(); //This is placed specifically somewhere else so must skip it here
		ArrayList<WizardPageSection> radioSections = new ArrayList<WizardPageSection>();
		for (RadioGroup radioGroup : model.getRadioGroups().getGroups()) {
			if (radioGroup!=bootVersion) {
				if (radioGroup.getRadios().length>1) {
					//Don't add a UI elements for something that offers no real choice
					radioSections.add(
						new ChooseOneSectionCombo<RadioInfo>(owner, radioGroup.getLabel(), radioGroup.getSelection(), radioGroup.getRadios())
						//new ChooseOneSectionCombo<RadioInfo>(owner, radioGroup.getLabel(), radioGroup.getSelection(), radioGroup.getRadios())
					);
					notEmpty = true;
				}
			}
		}
		if (notEmpty) {
			return new GroupSection(owner, null, radioSections.toArray(new WizardPageSection[radioSections.size()])).columns(2);
		}
		return null;
	}

	public class ProjectDetailsPage extends WizardPageWithSections {

		protected ProjectDetailsPage() {
			super("page1", "New Spring Starter Project", null);
		}

		@Override
		protected List<WizardPageSection> createSections() {
			List<WizardPageSection> sections = new ArrayList<WizardPageSection>();

			FieldModel<String> projectName = model.getProjectName();
			sections.add(new StringFieldSection(this, projectName));
			sections.add(new ProjectLocationSection(this, model.getLocation(), projectName.getVariable(), model.getLocationValidator()));

			WizardPageSection radios = createRadioGroupsSection(this);
			if (radios!=null) {
				sections.add(radios);
			}

			for (FieldModel<String> f : model.stringInputs) {
				//caution! we already created the section for projectName because we want it at the top
				if (projectName!=f) {
					sections.add(new StringFieldSection(this, f));
				}
			}

			sections.add(workingSetSection = new WorkingSetSection(this, selection));

			return sections;
		}

	}

	public class DependencyPage extends WizardPageWithSections {

		private static final int NUM_DEP_COLUMNS = 5;

		protected DependencyPage() {
			super("page2", "New Spring Starter Project", null);
		}

		@Override
		protected List<WizardPageSection> createSections() {
			List<WizardPageSection> sections = new ArrayList<WizardPageSection>();

			RadioGroup bootVersion = model.getBootVersion();
			sections.add(
					new ChooseOneSectionCombo<RadioInfo>(this, bootVersion.getLabel(),
							bootVersion.getSelection(), bootVersion.getRadios()));

			sections.add(
					new CommentSection(this, model.dependencies.getLabel())
			);

//			mostpopular = model.getMostPopular(2*NUM_DEP_COLUMNS);

			for (String cat : model.dependencies.getCategories()) {
				MultiSelectionFieldModel<Dependency> dependencyGroup = model.dependencies.getContents(cat);
				ExpandableSection expandable;
				sections.add(
					expandable = new ExpandableSection(this, dependencyGroup.getLabel(),
							new CheckBoxesSection<Dependency>(this, dependencyGroup)
								.columns(NUM_DEP_COLUMNS)
					)
				);
				expandable.getExpansionState().setValue(false);
			}

			return sections;
		}
	}

	public class PageThree extends WizardPageWithSections {

		protected PageThree() {
			super("page3", "New Spring Starter Project", null);
		}

		@Override
		protected List<WizardPageSection> createSections() {
			List<WizardPageSection> sections = new ArrayList<WizardPageSection>();

			sections.add(new GroupSection(this, "Site Info",
					new StringFieldSection(this, "Base Url", model.baseUrl, model.baseUrlValidator),
					new DescriptionSection(this, model.downloadUrl).label("Full Url").readOnly(false)
			));

			return sections;
		}

	}

	@Override
	public boolean performFinish() {
		model.setWorkingSets(workingSetSection.getWorkingSets()); //must be in ui thread. Don't put in job!
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
