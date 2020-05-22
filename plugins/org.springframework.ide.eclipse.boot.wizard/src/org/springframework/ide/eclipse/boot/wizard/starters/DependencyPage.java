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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.springframework.ide.eclipse.boot.core.initializr.InitializrServiceSpec.Dependency;
import org.springframework.ide.eclipse.boot.livexp.ui.DynamicSection;
import org.springframework.ide.eclipse.boot.wizard.CheckBoxesSection;
import org.springframework.ide.eclipse.boot.wizard.CheckBoxesSection.CheckBoxModel;
import org.springframework.ide.eclipse.boot.wizard.FilteredDependenciesSection;
import org.springframework.ide.eclipse.boot.wizard.MakeDefaultSection;
import org.springframework.ide.eclipse.boot.wizard.SearchBoxSection;
import org.springframework.ide.eclipse.boot.wizard.SelectedDependenciesSection;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;
import org.springsource.ide.eclipse.commons.livexp.ui.ChooseOneSectionCombo;
import org.springsource.ide.eclipse.commons.livexp.ui.CommentSection;
import org.springsource.ide.eclipse.commons.livexp.ui.GroupSection;
import org.springsource.ide.eclipse.commons.livexp.ui.IPageSection;
import org.springsource.ide.eclipse.commons.livexp.ui.LabeledPropertySection;
import org.springsource.ide.eclipse.commons.livexp.ui.WizardPageSection;
import org.springsource.ide.eclipse.commons.livexp.ui.WizardPageWithSections;
import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;
import org.springsource.ide.eclipse.commons.livexp.util.Parser;

public class DependencyPage extends WizardPageWithSections {

	private static final int NUM_COLUMNS_FREQUENTLY_USED = 3;
	private static final int MAX_MOST_POPULAR = 3 * NUM_COLUMNS_FREQUENTLY_USED;
	private static final Point DEPENDENCY_SECTION_SIZE = new Point(SWT.DEFAULT, 300);

	private CheckBoxesSection<Dependency> frequentlyUsedCheckboxes;

	// A validator that generates UI-specific results that for some reason were not
	// contained in the actual wizard model. For example, catching some wizard UI related
	// exceptions. Should only be used for wizard UI issues. Any validation that occurs
	// in the model should use the model's own validator
	private LiveVariable<ValidationResult> pageValidator = new LiveVariable<>();

	protected final AddStartersWizardModel wizardModel;

	/**
	 *
	 * Creates the dynamic sections of this wizard based on the wizard model's availability
	 * and validation
	 *
	 */

	final private LiveExpression<IPageSection> dynamicControlCreation = new LiveExpression<IPageSection>() {

		@Override
		protected IPageSection compute() {
			List<WizardPageSection> sections = new ArrayList<>();
			// If model is available, model loading has been successful
			InitializrModel model = wizardModel.getModel().getValue();
			ValidationResult validation = wizardModel.getValidator().getValue();
			if (validation != null && validation.status ==  IStatus.ERROR) {
				createErrorSection(sections);
			}
			else if (model != null) {
				model.onDependencyChange(() -> {
					Display.getDefault().asyncExec(() -> {
						refreshWizardUi();
					});
				});
				createDynamicSections(model, sections);
			}

			GroupSection groupSection = new GroupSection(DependencyPage.this, null,
					sections.toArray(new WizardPageSection[0]));
			groupSection.grabVertical(true);
			return groupSection;
		}
	};



	public DependencyPage(AddStartersWizardModel wizardModel) {
		super("Dependencies", "New Spring Starter Project Dependencies", null);
		this.wizardModel = wizardModel;
	}

	private void refreshFrequentlyUsedDependencies(InitializrModel model) {
		List<CheckBoxModel<Dependency>> dependenciesCheckboxes = model.getFrequentlyUsedDependencies(MAX_MOST_POPULAR);
		if (frequentlyUsedCheckboxes.isCreated()) {
			frequentlyUsedCheckboxes.setModel(dependenciesCheckboxes);
		}
		reflow();
	}

	@Override
	protected List<WizardPageSection> createSections() {
		// "link" the page section creation to the wizard model. When the model gets validated,
		// it will trigger dynamic page creation
		dynamicControlCreation.dependsOn(wizardModel.getValidator());
		List<WizardPageSection> sections = new ArrayList<>();

		// PT 172323896 - Focus is lost in the Service URL control every time
		// a user types a new character and the model is reloaded.
		// The reason is that, although the wizard dialog's built-in mechanism to
		// restore focus on controls works (the dialog correctly remembers that the service URL
		// control had focus PRIOR to starting background progress work, like model loading, and
		// attempts to restore it after work is completed - see org.eclipse.jface.wizard.WizardDialog#stopped())
		// this automatic restoration of focus on a control will NOT work if the control is disposed
		// which would happen for dynamically created sections, which get recreated on each model loading.
		// SOLUTION: EXCLUDE "static" controls like the service URL and boot version
		// from being recreated every time (i.e. dont include their creation in the dynamic section).
		// This ensures that when the wizard attempts to restore focus on the service URL control, it is
		// still active and not disposed
		createBootInfoSection(sections);

		DynamicSection dynamicSection = new DynamicSection(this, dynamicControlCreation);
		sections.add(dynamicSection);

		return sections;
	}

	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);

		// Add any validators to the wizard validator. This is important
		// as this "hooks" the separate validators to the wizards general validation
		// mechanism, which among things is responsbile for showing errors from
		// the various validators that exist
		validator.addChild(wizardModel.getValidator());
		validator.addChild(pageValidator);

		wizardModel.addModelLoader(() -> {
			runWithWizardProgress(monitor -> {
				monitor.beginTask("Loading starters data", IProgressMonitor.UNKNOWN);
				monitor.subTask("Creating Boot project model and fetching data from Initializr Service...");
				wizardModel.createInitializrModel(monitor);
				monitor.done();
			});
		});
	}

	private void createErrorSection(List<WizardPageSection> sections) {

		ValidationResult validation = wizardModel.getValidator().getValue();
		GroupSection errorSection = null;
		if (validation instanceof AddStartersError) {
			AddStartersError addStartersError = (AddStartersError) validation;
			 errorSection = new GroupSection(this, null, new CommentSection(this, "Error:"),
					new GroupSection(this, "", new CommentSection(this, addStartersError.details)));
		} else {
			 errorSection = new GroupSection(this, null, new CommentSection(this, "Details:"),
						new GroupSection(this, "", new CommentSection(this, "No content available")));
		}

		sections.add(errorSection);
	}


	private void runWithWizardProgress(IRunnableWithProgress runnable) {
		getWizard().getContainer().getShell().getDisplay().asyncExec(() -> {
			try {
				getContainer().run(true, false, runnable);
			} catch (Exception e) {
				pageValidator.setValue(ValidationResult
						.error(ExceptionUtil.getMessage(e)));
			}
		});
	}

	protected void createDynamicSections(InitializrModel model, List<WizardPageSection> sections) {
		sections.add(createFrequentlyUsedSection(model));
		sections.add(createTwoColumnSection(model));
	}

	@SuppressWarnings("resource")
	public WizardPageSection createTwoColumnSection(final InitializrModel model) {
		return new GroupSection(this,null,
				new GroupSection(this, null,
						new CommentSection(this, "Available:"),
						getSearchSection(model),
						new GroupSection(this, "",
								new FilteredDependenciesSection(this, model.dependencies, model.searchBox.getFilter())
								.sizeHint(DEPENDENCY_SECTION_SIZE)
							)
							.grabVertical(true)
							.noMargins(true)
						)
						.grabVertical(true)
						.noMargins(true),
				new GroupSection(this, null,
						new CommentSection(this, "Selected:"),
						new GroupSection(this, "",
								new SelectedDependenciesSection(this, model.dependencies)
								.sizeHint(DEPENDENCY_SECTION_SIZE)
							)
							.grabVertical(true)
							.noMargins(true),
						new MakeDefaultSection(this, () -> {
							if (model.saveDefaultDependencies()) {
								refreshFrequentlyUsedDependencies(model);
							}
						}, () -> {
							model.dependencies.clearSelection();
						})
					)
				)
				.columns(2, true)
				.grabVertical(true);
	}

	protected WizardPageSection getSearchSection(final InitializrModel model) {
		final SearchBoxSection searchBoxSection = new SearchBoxSection(this, model.searchBox.getText()) {
			@Override
			protected String getSearchHint() {
				return "Type to search dependencies";
			}
		};
//		PlatformUI.getWorkbench().getDisplay().asyncExec(() -> getControl().addListener(SWT.Show, event -> searchBoxSection.focusControl()));
		return searchBoxSection;
	}

	@SuppressWarnings("resource")
	protected WizardPageSection createFrequentlyUsedSection(InitializrModel model) {
		List<CheckBoxModel<Dependency>> frequentDependencies = model.getFrequentlyUsedDependencies(MAX_MOST_POPULAR);
		frequentlyUsedCheckboxes = new CheckBoxesSection<>(this, frequentDependencies).columns(NUM_COLUMNS_FREQUENTLY_USED);
		GroupSection frequentlyUsedSection = new GroupSection(this,
				null,
				new CommentSection(this, "Frequently Used:"),
				new GroupSection(this, "", frequentlyUsedCheckboxes));
		frequentlyUsedSection.isVisible.setValue(!frequentDependencies.isEmpty());
		return frequentlyUsedSection;
	}

	protected void createBootInfoSection(List<WizardPageSection> sections) {
		ChooseOneSectionCombo<String> serviceUrlSection  = new ChooseOneSectionCombo<String>(this, wizardModel.getServiceUrl(), wizardModel.getServiceUrlOptions())
				.grabHorizontal(true)
				.showErrorMarker(true);
		serviceUrlSection.allowTextEdits(Parser.IDENTITY);

		sections.add(serviceUrlSection);

		LabeledPropertySection section = new LabeledPropertySection(this, wizardModel.getBootVersion());
		sections.add(section);
	}

	@Override
	public boolean isPageComplete() {
		// We cannot complete from  the dependency page as
		// a user has to go to the  next page to manually accept changes
		// into their existing project
		return false;
	}

	@Override
	public boolean canFlipToNextPage() {
		return isValid();
	}

	private boolean isValid() {
		return validator.getValue() != null && validator.getValue().isOk();
	}

	private void refreshWizardUi() {
		IWizard wizard = DependencyPage.this.getWizard();
		if (wizard != null) {
			IWizardContainer container = wizard.getContainer();
			if (container != null) {
				container.updateButtons();
			}
		}
	}
}
