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
import org.springsource.ide.eclipse.commons.content.core.ContentPlugin;
import org.springsource.ide.eclipse.commons.content.core.util.IContentConstants;

/**
 * @author Kaitlin Duck Sherwood
 */

// modified from ExampleProjectsPreferences, with some help from
// RuntimePreferencePage and SpringConfigPreferencePage
public class TemplatesPreferencePage extends AbstractNameUrlPreferencePage {
	public static final String EXAMPLE_PREFERENCES_PAGE_ID = "com.springsource.sts.help.ui.templatepreferencepage";

	public static final String URL_SUFFIX = "/" + ContentPlugin.FILENAME_DESCRIPTORS;

	public static final String PREFERENCE_PAGE_HEADER = NLS
			.bind("You can import template projects via New->Spring Template Project.\n\n(Note that templates and descriptors require special packaging.)",
					null);

	public static final String ADD_EDIT_URL_DIALOG_INSTRUCTIONS = NLS.bind("Give the URL to a {0} or {1} file.",
			IContentConstants.TEMPLATE_DATA_FILE_NAME, ContentPlugin.FILENAME_DESCRIPTORS);

	@Override
	protected String preferencePageHeaderText() {
		return PREFERENCE_PAGE_HEADER;
	}

	@Override
	protected String addDialogHeaderText() {
		return ADD_EDIT_URL_DIALOG_INSTRUCTIONS;
	}

	@Override
	protected TemplatesPreferencesModel getModel() {
		return TemplatesPreferencesModel.getInstance();
	}

	@Override
	protected boolean shouldShowCheckbox() {
		return true;
	}

	@Override
	protected String checkboxLabel() {
		return NLS.bind("Show self-hosted templates in New Template Wizard", null);
	}

	// is the "Show self-hosted templates" checkbox enabled?
	public boolean shouldGetSelfHosted() {
		return getCheckboxValue();
	}

	@Override
	protected TemplateAddEditNameUrlDialog getAddEditDialog(NameUrlPair existingNameUrlPair) {
		return new TemplateAddEditNameUrlDialog(getShell(), model, existingNameUrlPair, addDialogHeaderText());
	}

}
