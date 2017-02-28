/*******************************************************************************
 * Copyright (c) 2017 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.wizard;

import java.util.ArrayList;
import java.util.List;

import org.springframework.ide.eclipse.boot.core.initializr.InitializrServiceSpec.Dependency;
import org.springframework.ide.eclipse.boot.wizard.CheckBoxesSection.CheckBoxModel;
import org.springsource.ide.eclipse.commons.livexp.core.SelectionModel;
import org.springsource.ide.eclipse.commons.livexp.ui.ButtonSection;
import org.springsource.ide.eclipse.commons.livexp.ui.ChooseOneSectionCombo;
import org.springsource.ide.eclipse.commons.livexp.ui.CommentSection;
import org.springsource.ide.eclipse.commons.livexp.ui.GroupSection;
import org.springsource.ide.eclipse.commons.livexp.ui.WizardPageSection;
import org.springsource.ide.eclipse.commons.livexp.ui.WizardPageWithSections;

public abstract class MultipleViewsDependencyPage extends WizardPageWithSections {
	private static final int NUM_COLUMNS_FREQUENTLY_USED = 4;
	private static final int MAX_MOST_POPULAR = 3*NUM_COLUMNS_FREQUENTLY_USED;

	private CheckBoxesSection<Dependency> frequentlyUsedCheckboxes;


	private final NewSpringBootWizardModel model;

	protected MultipleViewsDependencyPage() {
		super("page2", "New Spring Starter Project Dependencies", null);
		this.model = getModel();
	}
	
	abstract protected NewSpringBootWizardModel getModel();

	private void refreshFrequentlyUsedDependencies() {
		List<CheckBoxModel<Dependency>> dependenciesCheckboxes = model.getFrequentlyUsedDependencies(MAX_MOST_POPULAR);
		frequentlyUsedCheckboxes.setModel(dependenciesCheckboxes);
		reflow();
	}

	@Override
	protected List<WizardPageSection> createSections() {
		List<WizardPageSection> sections = new ArrayList<>();

		RadioGroup bootVersion = model.getBootVersion();
		sections.add(
			new ChooseOneSectionCombo<>(this, bootVersion.getLabel(),
						bootVersion.getSelection(), bootVersion.getRadios()
			)
			.useFieldLabelWidthHint(false)
		);

		sections.add(
				new CommentSection(this, model.dependencies.getLabel())
		);

		
		sections.add(
			new GroupSection(this, null,
				new SearchBoxSection(this, model.getDependencyFilterBoxText()) {
					@Override
					protected String getSearchHint() {
						return "Type to search dependencies";
					}
				},

				new ButtonSection(this, "Make Default", () -> {
					if (model.saveDefaultDependencies()) {
						refreshFrequentlyUsedDependencies();
					}
				})
				.tooltip("Make currently selected dependencies selected by default"),

				new ButtonSection(this, "Clear Selection", () -> {
					model.dependencies.clearSelection();
				})
				.tooltip("Clear dependencies selection")
			)
			.columns(3, false)
		);
		
		sections.add(createFrequentlyUsedSection());
		
		// Shared selection model between the different "dependency" sections
		SelectionModel<String> categorySelection = new SelectionModel<>();

		sections.add(
				new GroupSection(this, null,
						new GroupSection(this, "Categories", new ChooseCategorySection(this, 
								model,
								categorySelection)),

						new GroupSection(this, "Dependencies", new ChooseDependencySection(this,
								model, 
								categorySelection)),
						
						new GroupSection(this, "Selected", new SelectedDependenciesSection(this,
								model))
					)
					.columns(3, true)
				);

		return sections;
	}
	
	protected GroupSection createFrequentlyUsedSection() {
		List<CheckBoxModel<Dependency>> frequentDependencies = model.getFrequentlyUsedDependencies(MAX_MOST_POPULAR);
		frequentlyUsedCheckboxes = new CheckBoxesSection<>(this, frequentDependencies).columns(NUM_COLUMNS_FREQUENTLY_USED);
		GroupSection frequentlyUsedSection = new GroupSection(this, "Frequently Used", frequentlyUsedCheckboxes);
		frequentlyUsedSection.isVisible.setValue(!frequentDependencies.isEmpty());
		return frequentlyUsedSection;
	}

}
