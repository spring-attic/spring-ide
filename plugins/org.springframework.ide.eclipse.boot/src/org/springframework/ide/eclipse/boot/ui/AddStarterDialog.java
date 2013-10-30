/*******************************************************************************
 * Copyright (c) 2013 GoPivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.ui;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Shell;
import org.springframework.ide.eclipse.boot.core.SpringBootStarter;
import org.springframework.ide.eclipse.boot.core.dialogs.AddStarterModel;
import org.springsource.ide.eclipse.commons.livexp.ui.ChooseOneSection;
import org.springsource.ide.eclipse.commons.livexp.ui.CommentSection;
import org.springsource.ide.eclipse.commons.livexp.ui.DialogWithSections;
import org.springsource.ide.eclipse.commons.livexp.ui.InfoFieldSection;
import org.springsource.ide.eclipse.commons.livexp.ui.WizardPageSection;

public class AddStarterDialog  extends DialogWithSections {

	private AddStarterModel model;

	public AddStarterDialog(AddStarterModel model, Shell shell) {
		super("Add Spring Starter to pom", model, shell);
		this.model = model;
	}
	
	@Override
	protected List<WizardPageSection> createSections() throws CoreException {
		//return super.createSections();
		return Arrays.asList(
				new InfoFieldSection(this, "Project", model.getProjectName()),
				new ChooseOneSection<SpringBootStarter>(this, "Starters", model.getAvailableStarters(), model.chosen, model.validator)
		);
	}

	public static int openFor(IProject selectedProject, Shell shell) throws CoreException {
		return new AddStarterDialog(new AddStarterModel(selectedProject), shell).open();
	}
	
}