/*******************************************************************************
 *  Copyright (c) 2012 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.wizard.template.util;

import org.eclipse.osgi.util.NLS;

/**
 * @author Kaitlin Duck Sherwood
 */

public class ExampleProjectsPreferencePage extends AbstractNameUrlPreferencePage {

	public static final String EXAMPLE_PREFERENCES_PAGE_ID = "com.springsource.sts.help.ui.examplepreferencepage";

	public static final String ADD_EDIT_URL_DIALOG_INSTRUCTIONS = NLS.bind("Give the URL to a Github project.\n\n"
			+ "Note that currently the projects must be Eclipse projects or Maven projects.", null);

	public static final String PREFERENCE_PAGE_HEADER = NLS.bind(
			"Example projects appear on the dashboard, where you can click to import them.\n"
					+ "At the moment, we only support projects on github.", null);

	@Override
	protected String preferencePageHeaderText() {
		return ExampleProjectsPreferencePage.PREFERENCE_PAGE_HEADER;
	}

	@Override
	protected ExampleProjectsPreferenceModel getModel() {
		return ExampleProjectsPreferenceModel.getInstance();
	}

	@Override
	protected boolean shouldShowCheckbox() {
		return false;
	}

	@Override
	protected String checkboxLabel() {
		return null;
	}

	@Override
	protected String addDialogHeaderText() {
		return ADD_EDIT_URL_DIALOG_INSTRUCTIONS;
	}

	@Override
	protected AddEditNameUrlDialog getAddEditDialog(NameUrlPair existingNameUrlPair) {
		AddEditNameUrlDialog dialog = new ExampleAddEditNameUrlDialog(getShell(), model, existingNameUrlPair,
				addDialogHeaderText());
		dialog.setTitle(existingNameUrlPair == null ? AddEditNameUrlDialogMessages.ExampleProjects_addTitle
				: AddEditNameUrlDialogMessages.ExampleProjects_editTitle);
		return dialog;
	}

}
