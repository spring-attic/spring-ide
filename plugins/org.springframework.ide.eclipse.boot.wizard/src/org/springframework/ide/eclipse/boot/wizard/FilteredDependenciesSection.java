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

import java.util.List;

import org.eclipse.swt.widgets.Composite;
import org.springframework.ide.eclipse.boot.core.initializr.InitializrServiceSpec.Dependency;
import org.springframework.ide.eclipse.boot.wizard.CheckBoxesSection.CheckBoxModel;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.ui.ExpandableSection;
import org.springsource.ide.eclipse.commons.livexp.ui.IPageWithSections;
import org.springsource.ide.eclipse.commons.livexp.util.Filter;

public class FilteredDependenciesSection extends WizardPageSectionWithConfiguration {
	private static final int NUM_DEP_COLUMNS = 1;

	protected final NewSpringBootWizardModel model;
	private Composite dependencyArea;

	private LiveExpression<Filter<Dependency>> filter;

	public FilteredDependenciesSection(IPageWithSections owner, NewSpringBootWizardModel model, LiveExpression<Filter<Dependency>> filter) {
		super(owner);
		this.model = model;
		this.filter = filter;
	}

	@Override
	public void createContents(Composite page) {

		// This dependency area stays active throughout the life of the
		// dependency section. The contents of the area may change and are
		// managed by the check boxes section created in this area when category
		// selection changes
		dependencyArea = area(page);

		for (String cat : model.dependencies.getCategories()) {
			MultiSelectionFieldModel<Dependency> dependencyGroup = model.dependencies.getContents(cat);
			CheckBoxesSection<Dependency> checkboxesSection = new CheckBoxesSection<Dependency>(getWizardOwner(),
					dependencyGroup.getCheckBoxModels()).columns(NUM_DEP_COLUMNS);
			ExpandableSection expandable = new ExpandableSection(getWizardOwner(), dependencyGroup.getLabel(),
					checkboxesSection);

			expandable.createContents(dependencyArea);

			// Always expanded as it only shows selections. If there are no
			// selections, the expandable
			// section itself is hidden
//			expandable.getExpansionState().setValue(true);
//			expandable.setVisible(false);

			this.filter.addListener((exp, value) -> makeSectionsVisible(expandable, checkboxesSection, cat));
		}
	}

	private void makeSectionsVisible(ExpandableSection expandable, CheckBoxesSection<Dependency> checkboxesSection,
			String cat) {
		expandable.setVisible(isVisible(cat));
		checkboxesSection.applyFilter(this.filter.getValue());
		layout();
	}

	private boolean isVisible(String cat) {
		MultiSelectionFieldModel<Dependency> dependencyGroup = model.dependencies.getContents(cat);
		List<CheckBoxModel<Dependency>> models = dependencyGroup.getCheckBoxModels();
		Filter<Dependency> filterVal = filter.getValue();

		for (CheckBoxModel<Dependency> checkBoxModel : models) {
			if (filterVal.accept(checkBoxModel.getValue())) {
				return true;
			}
		}
		return false;
	}

	private void layout() {
		if (dependencyArea != null && !dependencyArea.isDisposed()) {
			dependencyArea.layout(true);
			dependencyArea.getParent().layout(true);
		}
		getWizardOwner().reflow();
	}

}
