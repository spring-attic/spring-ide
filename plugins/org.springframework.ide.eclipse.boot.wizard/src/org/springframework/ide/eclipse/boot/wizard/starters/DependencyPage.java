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
import org.springframework.ide.eclipse.boot.wizard.NewSpringBootWizard;
import org.springframework.ide.eclipse.boot.wizard.SearchBoxSection;
import org.springframework.ide.eclipse.boot.wizard.SelectedDependenciesSection;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;
import org.springsource.ide.eclipse.commons.livexp.ui.CommentSection;
import org.springsource.ide.eclipse.commons.livexp.ui.GroupSection;
import org.springsource.ide.eclipse.commons.livexp.ui.IPageSection;
import org.springsource.ide.eclipse.commons.livexp.ui.LabeledPropertySection;
import org.springsource.ide.eclipse.commons.livexp.ui.WizardPageSection;
import org.springsource.ide.eclipse.commons.livexp.ui.WizardPageWithSections;
import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;

import com.google.common.collect.ImmutableList;

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
		// "link" the page section creation live exp to the wizard validator live exp, so that page section creation DEPENDS
		// on wizard validation. For example, when the wizard validator validates the model (due to some other independent trigger)
		//, it will in turn cause the page section creation
		// live exp to also be called.
		LiveExpression<IPageSection> pageCreationExp = validator.apply((result) -> {
			if (result != null) {
				InitializrModel model = wizardModel.getInitializrFactoryModel().getModel().getValue();
				if (model != null) {
					List<WizardPageSection> sections = new ArrayList<>();
					sections.add(createBootInfoSection());

					if (result.isOk()) {
						model.onDependencyChange(() -> {
							Display.getDefault().asyncExec(() -> {
								refreshWizardUi();
							});
						});
						createDynamicSections(model, sections);
					} else {
						createErrorSection(sections);
					}

					GroupSection groupSection = new GroupSection(this, null,
							sections.toArray(new WizardPageSection[0]));
					groupSection.grabVertical(true);
					return groupSection;
				}
			}
			return new CommentSection(this, NewSpringBootWizard.NO_CONTENT_AVAILABLE);
		});
		DynamicSection dynamicSection = new DynamicSection(this, pageCreationExp);

		return ImmutableList.of(dynamicSection);
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

		getWizard().getContainer().getShell().getDisplay().asyncExec(() -> loadWithProgress());
	}

	private void createErrorSection(List<WizardPageSection> sections) {

		// IMPORTANT: this method may be called multiple times throughout the life cycle
		// of the page, Only show
		// error section IF there is an actual error
		ValidationResult validation = validator.getValue();

		if (validation instanceof AddStartersError) {
			AddStartersError addStartersError = (AddStartersError) validation;
			GroupSection errorSection = new GroupSection(this, null, new CommentSection(this, "Error:"),
					new GroupSection(this, "", new CommentSection(this, addStartersError.details)));

			sections.add(errorSection);
		}
	}

	private void loadWithProgress() {
		try {
			getContainer().run(true, false, monitor -> {
				monitor.beginTask("Loading starters data", IProgressMonitor.UNKNOWN);
				monitor.subTask("Creating Boot project model...");
				wizardModel.createInitializrModelForProject();
				String bootVersion = wizardModel.getBootVersion().getValue();
				monitor.subTask("Fetching data for boot version '" +  bootVersion + "' from Initializr Service");
				wizardModel.downloadStarterInfos();
				monitor.done();
			});
		} catch (Exception e) {
			pageValidator.setValue(ValidationResult
					.error("Failed fetching data from Initializr Service: " + ExceptionUtil.getMessage(e)));
		}
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

	protected WizardPageSection createBootInfoSection() {
		LabeledPropertySection section = new LabeledPropertySection(this, wizardModel.getBootVersion());
		return section;
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
