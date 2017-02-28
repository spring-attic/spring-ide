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

import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.springframework.ide.eclipse.boot.core.initializr.InitializrServiceSpec.Dependency;
import org.springframework.ide.eclipse.boot.wizard.CheckBoxesSection.CheckBoxModel;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.SelectionModel;
import org.springsource.ide.eclipse.commons.livexp.core.UIValueListener;
import org.springsource.ide.eclipse.commons.livexp.ui.CommentSection;
import org.springsource.ide.eclipse.commons.livexp.ui.IPageWithSections;
import org.springsource.ide.eclipse.commons.livexp.ui.WizardPageWithSections;
import org.springsource.ide.eclipse.commons.livexp.util.Filter;

public class ChooseDependencySection extends WizardPageSectionWithConfiguration {
	private static final int NUM_DEP_COLUMNS = 1;

	private final NewSpringBootWizardModel model;
	private DepencyCheckBoxesSection<Dependency> selectedDependencySection = null;
	private CommentSection commentSection;
	private UIValueListener<Filter<CheckBoxModel<Dependency>>> sectionFilterListener = null;
	private SelectionModel<String> categorySelection;
	private Composite dependencyArea;

	public ChooseDependencySection(IPageWithSections owner, NewSpringBootWizardModel model,
			SelectionModel<String> categorySelection, SectionConfiguration configuration) {
		super(owner, configuration);
		this.model = model;
		this.categorySelection = categorySelection;
	}

	@Override
	public void createContents(Composite page) {

		// This dependency area stays active throughout the life of the
		// dependency section. The contents of the area may change and are
		// managed by the check boxes section created in this area when category
		// selection changes
		dependencyArea = area(page);

		categorySelection.selection.addListener(new UIValueListener<String>() {

			@Override
			protected void uiGotValue(LiveExpression<String> exp, String category) {
				updateDependencySection(category);
			}

		});

	}

	private void applyFilter(Filter<CheckBoxModel<Dependency>> filter,
			DepencyCheckBoxesSection<Dependency> checkboxes) {
		boolean visChanged = checkboxes.applyFilter(filter);
		if (checkboxes.isCreated()) {
			boolean hasVisible = checkboxes.hasVisible();
			if (hasVisible && visChanged) {
				layout();
			}
		}
	}

	protected GridLayout createLayout() {
		GridLayout layout = new GridLayout(NUM_DEP_COLUMNS, true);
		return layout;
	}

	protected WizardPageWithSections getWizardOwner() {
		return (WizardPageWithSections) owner;
	}

	private void updateDependencySection(String category) {
		MultiSelectionFieldModel<Dependency> dependencyGroup = null;
		for (String cat : model.dependencies.getCategories()) {
			if (cat.equals(category)) {
				dependencyGroup = model.dependencies.getContents(cat);
				break;
			}
		}
		if (dependencyArea != null && !dependencyArea.isDisposed()) {

			if (dependencyGroup != null) {
				if (commentSection != null) {
					commentSection.dispose();
				}
				// Only ONE check box section is required and kept active
				// throughout
				// the life of the dependency section.
				// The CONTENT of the box section may change, but the section is
				// NOT
				// recreated every time the dependency content changes
				if (selectedDependencySection == null) {
					selectedDependencySection = new DepencyCheckBoxesSection<>(owner,
							dependencyGroup.getCheckBoxModels());
					selectedDependencySection.createContents(dependencyArea);

				} else {
					selectedDependencySection.setModel(dependencyGroup.getCheckBoxModels());
				}

				addSelectionFilterListener();
			} else {
				if (commentSection == null) {
					commentSection = new CommentSection(getWizardOwner(), "Please select a category to see available dependency options.");
					commentSection.createContents(dependencyArea);
				}
			}

			layout();
		}
	}

	private void addSelectionFilterListener() {
		// NOTE: adding a listener triggers a value change, and it is the way to
		// "apply" a filter
		// any time a category selection changes, so that filter is still in
		// effect when dependency check box contents change during category
		// changes. However, to do this correctly without memory leak,
		// be sure to REMOVE the existing listener first from the model
		if (sectionFilterListener != null) {
			model.getDependencyFilter().removeListener(sectionFilterListener);
		}
		sectionFilterListener = new UIValueListener<Filter<CheckBoxModel<Dependency>>>() {
			@Override
			protected void uiGotValue(LiveExpression<Filter<CheckBoxModel<Dependency>>> exp,
					Filter<CheckBoxModel<Dependency>> value) {
				applyFilter(value, selectedDependencySection);
			}

		};
		model.getDependencyFilter().addListener(sectionFilterListener);

	}

	private void layout() {
		if (dependencyArea != null && !dependencyArea.isDisposed()) {
			dependencyArea.layout(true);
			dependencyArea.getParent().layout(true);
		}
		getWizardOwner().reflow();
	}

	@Override
	public void dispose() {
		if (selectedDependencySection != null) {
			selectedDependencySection.dispose();
		}
		if (sectionFilterListener != null) {
			model.getDependencyFilter().removeListener(sectionFilterListener);
		}
		super.dispose();
	}

	class DepencyCheckBoxesSection<T> extends CheckBoxesSection<T> {

		public DepencyCheckBoxesSection(IPageWithSections owner, List<CheckBoxModel<T>> model) {
			super(owner, model);
			columns(NUM_DEP_COLUMNS);
		}

	}

}
