/*******************************************************************************
 *  Copyright (c) 2013 GoPivotal, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.gettingstarted.preferences;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ui.IWorkbenchPreferencePage;
import org.springsource.ide.eclipse.commons.livexp.ui.PreferencePageWithSections;
import org.springsource.ide.eclipse.commons.livexp.ui.PrefsPageSection;

public class GettingStartedPreferencesPage extends PreferencePageWithSections
		implements IWorkbenchPreferencePage {

	public GettingStartedPreferencesPage() {
	}

	@Override
	protected List<PrefsPageSection> createSections() {
		List<PrefsPageSection> sections = new ArrayList<PrefsPageSection>();
		sections.add(new DashboardUrlsPreferenceSection(this));
		return sections;
	}

}
