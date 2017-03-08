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

import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.springframework.ide.eclipse.boot.core.initializr.InitializrServiceSpec.Dependency;
import org.springframework.ide.eclipse.boot.wizard.CheckBoxesSection.CheckBoxModel;
import org.springsource.ide.eclipse.commons.livexp.ui.ChooseOneSectionCombo;
import org.springsource.ide.eclipse.commons.livexp.ui.CommentSection;
import org.springsource.ide.eclipse.commons.livexp.ui.GroupSection;
import org.springsource.ide.eclipse.commons.livexp.ui.WizardPageSection;
import org.springsource.ide.eclipse.commons.livexp.ui.WizardPageWithSections;

public abstract class MultipleViewsDependencyPage extends WizardPageWithSections {
	private static final int NUM_COLUMNS_FREQUENTLY_USED = 3;
	private static final int MAX_MOST_POPULAR = 3 * NUM_COLUMNS_FREQUENTLY_USED;
	private static final Point DEPENDENCY_SECTION_SIZE = new Point(SWT.DEFAULT, 300);

	private CheckBoxesSection<Dependency> frequentlyUsedCheckboxes;

	private final NewSpringBootWizardModel model;

	protected MultipleViewsDependencyPage() {
		super("page2", "New Spring Starter Project Dependencies", null);
		this.model = getModel();
	}

	abstract protected NewSpringBootWizardModel getModel();

	private void refreshFrequentlyUsedDependencies() {
		List<CheckBoxModel<Dependency>> dependenciesCheckboxes = model.getFrequentlyUsedDependencies(MAX_MOST_POPULAR);
		if (frequentlyUsedCheckboxes.isCreated()) {
			frequentlyUsedCheckboxes.setModel(dependenciesCheckboxes);
		}
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

		sections.add(createFrequentlyUsedSection());
		sections.add(createTwoColumnSection());
		return sections;
	}

	public WizardCompositeSection createTwoColumnSection() {
		return new WizardCompositeSection(this, 
				new WizardCompositeSection(this, 
						new CommentSection(this, "Available:"),
						getSearchSection(),
						new WizardGroupSection(this, null, 
								new FilteredDependenciesSection(this, model, model.getDependencyFilter())
								.sizeHint(DEPENDENCY_SECTION_SIZE)
								)),
				new WizardCompositeSection(this, 
						new CommentSection(this, "Selected:"),
						new WizardGroupSection(this, null, 
								new SelectedDependenciesSection(this, model)
								.sizeHint(DEPENDENCY_SECTION_SIZE)
								),
						new MakeDefaultSection(this, () -> {
							if (model.saveDefaultDependencies()) {
								refreshFrequentlyUsedDependencies();
							}
						}, () -> {
							model.dependencies.clearSelection();
						}))) {
			@Override
			protected void applyLayout(Composite composite) {
				GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(true)
						.applyTo(composite);
			}
		};
	}

	protected WizardPageSection getSearchSection() {
		return new GroupSection(this, null,
				new SearchBoxSection(this, model.getDependencyFilterBoxText()) {
					@Override
						protected String getSearchHint() {
							return "Type to search dependencies";
						}
				}
		);
	}

	protected WizardPageSection createFrequentlyUsedSection() {
		List<CheckBoxModel<Dependency>> frequentDependencies = model.getFrequentlyUsedDependencies(MAX_MOST_POPULAR);
		frequentlyUsedCheckboxes = new CheckBoxesSection<>(this, frequentDependencies).columns(NUM_COLUMNS_FREQUENTLY_USED);
		GroupSection frequentlyUsedSection = new GroupSection(this, 
				null,
				new CommentSection(this, "Frequently Used:"), 
				new WizardGroupSection(this, null, frequentlyUsedCheckboxes));
		frequentlyUsedSection.isVisible.setValue(!frequentDependencies.isEmpty());
		return frequentlyUsedSection;
	}

}
