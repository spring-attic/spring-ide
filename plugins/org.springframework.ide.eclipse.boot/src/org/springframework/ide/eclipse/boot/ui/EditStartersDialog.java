/*******************************************************************************
 * Copyright (c) 2013 GoPivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.ui;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.springframework.ide.eclipse.boot.core.EditStartersModel;
import org.springframework.ide.eclipse.boot.core.SpringBootStarter;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;
import org.springsource.ide.eclipse.commons.livexp.core.Validator;
import org.springsource.ide.eclipse.commons.livexp.ui.ChooseMultipleSection;
import org.springsource.ide.eclipse.commons.livexp.ui.CommentSection;
import org.springsource.ide.eclipse.commons.livexp.ui.DialogWithSections;
import org.springsource.ide.eclipse.commons.livexp.ui.WizardPageSection;

/**
 * @author Kris De Volder
 */
public class EditStartersDialog extends DialogWithSections {

	private EditStartersModel model;

	public EditStartersDialog(EditStartersModel model, Shell shell) {
		super("Edit Spring Boot Starters", shell);
		this.model = model;
	}
	
	@Override
	protected List<WizardPageSection> createSections() throws CoreException {
		//return super.createSections();
		return Arrays.asList(
				new CommentSection(this, "Project: "+model.getProjectName()),
				new ChooseMultipleSection<SpringBootStarter>(this, "Starters", model.getAvailableStarters(), model.starters, Validator.constant(ValidationResult.OK))
		);
	}

	public static int openFor(IProject selectedProject, Shell shell) throws CoreException {
		return new EditStartersDialog(new EditStartersModel(selectedProject), shell).open();
	}
	
	@Override
	protected void okPressed() {
		super.okPressed();
		model.performOk();
	}

}
