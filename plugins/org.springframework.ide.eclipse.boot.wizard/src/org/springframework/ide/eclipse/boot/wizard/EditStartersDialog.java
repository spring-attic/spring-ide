/*******************************************************************************
 * Copyright (c) 2013, 2016-2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.wizard;

import static org.springframework.ide.eclipse.boot.livexp.ui.DynamicSection.DEFAULT_MIN_SIZE;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.springframework.ide.eclipse.boot.core.BootActivator;
import org.springframework.ide.eclipse.boot.core.SpringBootCore;
import org.springframework.ide.eclipse.boot.core.initializr.InitializrService;
import org.springframework.ide.eclipse.boot.core.initializr.InitializrServiceSpec.Dependency;
import org.springframework.ide.eclipse.boot.livexp.ui.DynamicSection;
import org.springframework.ide.eclipse.boot.wizard.CheckBoxesSection.CheckBoxModel;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.UIValueListener;
import org.springsource.ide.eclipse.commons.livexp.ui.ChooseOneSectionCombo;
import org.springsource.ide.eclipse.commons.livexp.ui.CommentSection;
import org.springsource.ide.eclipse.commons.livexp.ui.DialogWithSections;
import org.springsource.ide.eclipse.commons.livexp.ui.ExpandableSection;
import org.springsource.ide.eclipse.commons.livexp.ui.GroupSection;
import org.springsource.ide.eclipse.commons.livexp.ui.WizardPageSection;
import org.springsource.ide.eclipse.commons.livexp.util.Filter;
import org.springsource.ide.eclipse.commons.livexp.util.Parser;

import com.google.common.collect.ImmutableList;

/**
 * @author Kris De Volder
 */
public class EditStartersDialog extends DialogWithSections {

	private static final int NUM_DEP_COLUMNS = 4;
	static final String NO_CONTENT_AVAILABLE = "No content available.";
	public InitializrFactoryModel<EditStartersModel> model;

	public EditStartersDialog(InitializrFactoryModel<EditStartersModel> model, Shell shell) {
		super("Edit Spring Boot Starters", model, shell);
		this.setShellStyle(SWT.RESIZE | getShellStyle());
		this.model = model;
	}

	private void applyFilter(Filter<Dependency> filter, ExpandableSection expandable, CheckBoxesSection<Dependency> checkboxes) {
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
		ChooseOneSectionCombo<String> comboSection = new ChooseOneSectionCombo<>(this, model.getServiceUrlField(), model.getUrls());
		comboSection.allowTextEdits(Parser.IDENTITY);

		DynamicSection dynamicSection = new DynamicSection(this, model.getModel().apply((dynamicModel) -> {
			if (dynamicModel != null) {
				return createDynamicContents(dynamicModel);
			}
			return new CommentSection(this, NO_CONTENT_AVAILABLE);
		} ));
		dynamicSection.setHeightHint(null);

		return ImmutableList.of(comboSection, dynamicSection);
	}

	protected WizardPageSection createDynamicContents(EditStartersModel model) {

		GroupSection sections = new GroupSection(EditStartersDialog.this, null);
//		sections.add(new CommentSection(this, "Project: "+model.getProjectName()));

		List<CheckBoxModel<Dependency>> mostpopular = model.getFrequentlyUsedDependencies(4*NUM_DEP_COLUMNS);
		if (!mostpopular.isEmpty()) {
			sections.addSections(new ExpandableSection(this, "Frequently Used",
					new CheckBoxesSection<>(this, mostpopular)
						.columns(NUM_DEP_COLUMNS)
			));
		}

		sections.addSections(new SearchBoxSection(this, model.searchBox.getText()) {
			@Override
			protected String getSearchHint() {
				return "Type to search dependencies";
			}
		});

		for (String cat : model.dependencies.getCategories()) {
			MultiSelectionFieldModel<Dependency> dependencyGroup = model.dependencies.getContents(cat);
			final ExpandableSection expandable;
			final CheckBoxesSection<Dependency> checkboxes;
			sections.addSections(
				expandable = new ExpandableSection(this, dependencyGroup.getLabel(),
						checkboxes = new CheckBoxesSection<>(this, dependencyGroup.getCheckBoxModels())
							.columns(NUM_DEP_COLUMNS)
				)
			);
			expandable.getExpansionState().setValue(false);
			model.searchBox.getFilter().addListener(new UIValueListener<Filter<Dependency>>() {
				@Override
				protected void uiGotValue(
						LiveExpression<Filter<Dependency>> exp,
						Filter<Dependency> value
				) {
					applyFilter(value, expandable, checkboxes);
				}

			});
		}

		return sections;
	}

	public static int openFor(IProject selectedProject, Shell shell) throws Exception {
		InitializrFactoryModel<EditStartersModel> fmodel = new InitializrFactoryModel<>((url) -> {
			if (url!=null) {
				InitializrService initializr = InitializrService.create(BootActivator.getUrlConnectionFactory(), url);
				SpringBootCore core = new SpringBootCore(initializr);
				return new EditStartersModel(
						selectedProject,
						core,
						BootActivator.getDefault().getPreferenceStore()
				);
			}
			return null;
		});
		return new EditStartersDialog(fmodel, shell).open();
	}

}
