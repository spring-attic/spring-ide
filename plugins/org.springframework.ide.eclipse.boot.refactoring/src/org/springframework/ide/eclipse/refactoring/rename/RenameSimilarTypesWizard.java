/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.refactoring.rename;

import java.util.List;

import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardPage;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.springsource.ide.eclipse.commons.livexp.core.UIValueListener;
import org.springsource.ide.eclipse.commons.livexp.ui.InfoFieldSection;
import org.springsource.ide.eclipse.commons.livexp.ui.StringFieldSection;
import org.springsource.ide.eclipse.commons.livexp.ui.WizardPageSection;
import org.springsource.ide.eclipse.commons.livexp.ui.WizardPageWithSections;

import com.google.common.collect.ImmutableList;

public class RenameSimilarTypesWizard extends RefactoringWizard {

	private RenameSimilarTypesModel model;

	public class InputPage extends UserInputWizardPage {

		private static final String INPUT_PAGE_NAME = "inputPage";

		public InputPage() {
			super(INPUT_PAGE_NAME);
			setTitle(model.getDialogTitle());
			// TODO Auto-generated constructor stub
		}

		private WizardPageWithSections delegate = new WizardPageWithSections(INPUT_PAGE_NAME, RenameSimilarTypesRefactoring.REFACTORING_NAME, null) {

			//TODO: delegate's validator is not hooked up to anything in the wizard. (Instead a separate more custom validator is hooked-up to the
			// wizard directly. This validator uses the refactoring's initial condition checking status.

			@Override
			protected List<WizardPageSection> createSections() {
				ImmutableList.Builder<WizardPageSection> builder = ImmutableList.builder();
				builder.add(new StringFieldSection(this, "Old Name", model.oldName));
				builder.add(new StringFieldSection(this, "New Name", model.newName));
				builder.add(new SelectTypesSection(this, model.foundTypes, model.checkBoxStates));
				return builder.build();
			}

			@Override
			protected IWizardContainer getContainer() {
				return InputPage.this.getContainer();
			}

			@Override
			protected void setControl(Control newControl) {
				super.setControl(newControl);
				InputPage.this.setControl(newControl);
			}
		};

		@Override
		public void createControl(Composite parent) {
			delegate.createControl(parent);
			model.getRefactoringStatus().addListener(UIValueListener.from((e, status) -> {
				setPageComplete(status);
				setInitialConditionCheckingStatus(status);
			}));
		}
	}

	public RenameSimilarTypesWizard(RenameSimilarTypesModel model, String refactoringName) {
		super(model.getRefactoring(), RefactoringWizard.WIZARD_BASED_USER_INTERFACE);
		this.setWindowTitle(model.getDialogTitle());
		this.model = model;
	}

	@Override
	protected void addUserInputPages() {
		RefactoringWizardPage inputPage = new InputPage();
		addPage(inputPage);
	}

}
