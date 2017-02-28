/*******************************************************************************
 * Copyright (c) 2013, 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.wizard;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.springframework.ide.eclipse.boot.core.initializr.InitializrServiceSpec.Dependency;
import org.springframework.ide.eclipse.boot.wizard.CheckBoxesSection.CheckBoxModel;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.UIValueListener;
import org.springsource.ide.eclipse.commons.livexp.ui.DialogWithSections;
import org.springsource.ide.eclipse.commons.livexp.ui.ExpandableSection;
import org.springsource.ide.eclipse.commons.livexp.ui.WizardPageSection;
import org.springsource.ide.eclipse.commons.livexp.util.Filter;

/**
 * @author Kris De Volder
 */
public class EditStartersDialog extends DialogWithSections {

	private static final int NUM_DEP_COLUMNS = 4;
	public EditStartersModel model;
	private DependencyFilterBox searchBoxModel;

	public EditStartersDialog(EditStartersModel model, Shell shell) {
		super("Edit Spring Boot Starters", model, shell);
		this.setShellStyle(SWT.RESIZE | getShellStyle());
		this.model = model;
		this.searchBoxModel = new DependencyFilterBox();
	}

	private void applyFilter(Filter<CheckBoxModel<Dependency>> filter, ExpandableSection expandable, CheckBoxesSection<Dependency> checkboxes) {
		boolean visChanged = checkboxes.applyFilter(filter);

		if (checkboxes.isCreated()) {
			boolean hasVisible = checkboxes.hasVisible();
			expandable.setVisible(hasVisible);
			if (hasVisible && visChanged) {
				//Reveal if visibility changed
				expandable.getExpansionState().setValue(true);
				this.reflow();
			}
		}
	}

	@Override
	protected List<WizardPageSection> createSections() throws CoreException {
		//return super.createSections();
		ArrayList<WizardPageSection> sections = new ArrayList<>();
//		sections.add(new CommentSection(this, "Project: "+model.getProjectName()));

		List<CheckBoxModel<Dependency>> mostpopular = model.getFrequentlyUsedDependencies(4*NUM_DEP_COLUMNS);
		if (!mostpopular.isEmpty()) {
			sections.add(new ExpandableSection(this, "Frequently Used",
					new CheckBoxesSection<Dependency>(this, mostpopular)
						.columns(NUM_DEP_COLUMNS)
			));
		}

		sections.add(new SearchBoxSection(this, searchBoxModel.getText()) {
			@Override
			protected String getSearchHint() {
				return "Type to search dependencies";
			}
		});

		for (String cat : model.dependencies.getCategories()) {
			MultiSelectionFieldModel<Dependency> dependencyGroup = model.dependencies.getContents(cat);
			final ExpandableSection expandable;
			final CheckBoxesSection<Dependency> checkboxes;
			sections.add(
				expandable = new ExpandableSection(this, dependencyGroup.getLabel(),
						checkboxes = new CheckBoxesSection<Dependency>(this, dependencyGroup.getCheckBoxModels())
							.columns(NUM_DEP_COLUMNS)
				)
			);
			expandable.getExpansionState().setValue(false);
			searchBoxModel.getFilter().addListener(new UIValueListener<Filter<CheckBoxModel<Dependency>>>() {
				@Override
				protected void uiGotValue(
						LiveExpression<Filter<CheckBoxModel<Dependency>>> exp,
						Filter<CheckBoxModel<Dependency>> value
				) {
					applyFilter(value, expandable, checkboxes);
				}

			});
		}

		return sections;
	}

	public static int openFor(IProject selectedProject, Shell shell) throws Exception {
		EditStartersModel model = new EditStartersModel(selectedProject);
		String notSupported = model.getNotSupportedMessage();
		if (notSupported==null) {
			return new EditStartersDialog(model, shell).open();
		}
		MessageDialog.openError(shell, "Couldn't open the 'Edit Starters' dialog",
				notSupported
		);
		return 0;
	}

}
