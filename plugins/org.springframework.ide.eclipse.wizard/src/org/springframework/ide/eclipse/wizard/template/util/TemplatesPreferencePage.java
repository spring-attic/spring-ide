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

// modified from ExampleProjectsPreferences, with some help from
// RuntimePreferencePage and SpringConfigPreferencePage
public class TemplatesPreferencePage extends AbstractNameUrlPreferencePage {
	public static final String EXAMPLE_PREFERENCES_PAGE_ID = "com.springsource.sts.help.ui.templatepreferencepage";

	public static final String URL_SUFFIX = "/descriptions.xml";

	public static final String PREFERENCE_PAGE_HEADER = NLS.bind(
			"You can reach templates by selecting New->Templates.\n", null);

	public static final String ADD_EDIT_URL_DIALOG_INSTRUCTIONS = NLS.bind("Give the URL to a template.\n"
			+ "Note that templates require special packaging", null);

	@Override
	protected boolean validateUrl(String urlString) {
		if (urlString.startsWith("http")) {
			return true;
		}

		// if (urlString.startsWith("file:")) {
		// return true;
		// }
		else {
			return false;
		}
	}

	@Override
	protected String preferencePageHeaderText() {
		return PREFERENCE_PAGE_HEADER;
	}

	@Override
	protected TemplatesPreferencesModel getModel() {
		return TemplatesPreferencesModel.getInstance();
	}

	@Override
	protected String validationErrorMessage(String urlString) {
		return NLS.bind("Sorry, {0} isn't a valid URL.  Right now we only take HTTP or HTTPS URLs.", urlString);
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
}
