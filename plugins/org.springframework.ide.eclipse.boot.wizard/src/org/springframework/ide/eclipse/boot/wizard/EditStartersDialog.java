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
import org.springsource.ide.eclipse.commons.livexp.ui.ChooseOneSectionCombo;
import org.springsource.ide.eclipse.commons.livexp.ui.CommentSection;
import org.springsource.ide.eclipse.commons.livexp.ui.DialogWithSections;
import org.springsource.ide.eclipse.commons.livexp.ui.GroupSection;
import org.springsource.ide.eclipse.commons.livexp.ui.WizardPageSection;
import org.springsource.ide.eclipse.commons.livexp.util.Parser;

import com.google.common.collect.ImmutableList;

/**
 * @author Kris De Volder
 */
public class EditStartersDialog extends DialogWithSections {

	private static final int NUM_DEP_COLUMNS = 3;
	private static final Point DEPENDENCY_SECTION_SIZE = new Point(SWT.DEFAULT, 300);
	static final String NO_CONTENT_AVAILABLE = "No content available.";
	public InitializrFactoryModel<EditStartersModel> model;
	private boolean firstSearchBox = true; //becomes false after the searchBox is created first time.
	private long openingTime;

	public EditStartersDialog(InitializrFactoryModel<EditStartersModel> model, Shell shell) {
		super("Edit Spring Boot Starters", model, shell);
		this.setShellStyle(SWT.RESIZE | getShellStyle());
		this.model = model;
	}

	@Override
	protected List<WizardPageSection> createSections() throws CoreException {
		openingTime = System.currentTimeMillis();
		ChooseOneSectionCombo<String> comboSection = new ChooseOneSectionCombo<>(this, model.getServiceUrlField(), model.getUrls()).grabHorizontal(true);
		comboSection.allowTextEdits(Parser.IDENTITY);

		DynamicSection dynamicSection = new DynamicSection(this, model.getModel().apply((dynamicModel) -> {
			if (dynamicModel != null) {
				return createDynamicContents(dynamicModel);
			}
			return new CommentSection(this, NO_CONTENT_AVAILABLE);
		} ));

		return ImmutableList.of(comboSection, dynamicSection);
	}

	protected WizardPageSection createDynamicContents(EditStartersModel model) {
		EditStartersDialog owner = EditStartersDialog.this;
		GroupSection sections = new GroupSection(owner, null)
				.grabVertical(true);
//		sections.add(new CommentSection(this, "Project: "+model.getProjectName()));

		List<CheckBoxModel<Dependency>> mostpopular = model.getFrequentlyUsedDependencies(4*NUM_DEP_COLUMNS);
		if (!mostpopular.isEmpty()) {
			sections.addSections(new GroupSection(this, "Frequently Used",
					new CheckBoxesSection<>(this, mostpopular).columns(NUM_DEP_COLUMNS)
			));
		}

		sections.addSections(
				new GroupSection(owner, null,
					//Column 1:
					new GroupSection(owner, null,
						new CommentSection(owner, "Available:"),
						searchBox(model),
						new GroupSection(owner, "",
							new FilteredDependenciesSection(owner, model.dependencies, model.searchBox.getFilter())
							.sizeHint(DEPENDENCY_SECTION_SIZE)
						)
						.noMargins(true)
						.grabVertical(true)
					).grabVertical(true),
					//Column 2:
					new GroupSection(owner, null,
						new CommentSection(owner, "Selected:"),
						new GroupSection(owner, "",
							new SelectedDependenciesSection(owner, model.dependencies)
							.sizeHint(DEPENDENCY_SECTION_SIZE)
						)
						.noMargins(true)
						.grabVertical(true)
					).grabVertical(true)
				)
				.grabVertical(true)
				.columns(2)
		);


		return sections;
	}

//	private Color getSystemColor(int c) {
//		Display display = Display.getDefault();
//		LiveVariable<Color> color = new LiveVariable<>();
//		display.syncExec(() -> {
//			color.setValue(display.getSystemColor(c));
//		});
//		return color.getValue();
//	}

	private SearchBoxSection searchBox(EditStartersModel model) {
		return new SearchBoxSection(this, model.searchBox.getText()) {
			@Override
			protected String getSearchHint() {
				return "Type to search dependencies";
			}
		}
		.grabFocus(searchBoxShouldGrabFocus());
	}

	private boolean searchBoxShouldGrabFocus() {
		System.out.println("firstSeachBox = "+this.firstSearchBox);
		try {
			return this.firstSearchBox && isCloseToOpeningTime();
		} finally {
			this.firstSearchBox = false;
		}
	}

	private boolean isCloseToOpeningTime() {
		System.out.println("openingTime = "+openingTime);
		long age = System.currentTimeMillis() - openingTime;
		System.out.println("age = "+age);
		return age < 3000;
	}

	public static int openFor(IProject selectedProject, Shell shell) {
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
