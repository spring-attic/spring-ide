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
package org.springframework.ide.eclipse.roo.ui.internal.properties;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.springframework.ide.eclipse.core.SpringCorePreferences;
import org.springframework.ide.eclipse.roo.core.RooCoreActivator;
import org.springframework.ide.eclipse.ui.dialogs.ProjectAndPreferencePage;


/**
 * @author Christian Dupuis
 * @since 2.2.0
 */
public class RooInstallPropertyPage extends ProjectAndPreferencePage {

	public static final String PREF_ID = "com.springsource.sts.roo.ui.preferencePage"; //$NON-NLS-1$

	public static final String PROP_ID = "com.springsource.sts.roo.ui.projectPropertyPage"; //$NON-NLS-1$

	private Combo rooInstallCombo;

	public RooInstallPropertyPage() {
		noDefaultAndApplyButton();
	}

	@Override
	@SuppressWarnings("deprecation")
	public boolean performOk() {

		if (useProjectSettings()) {
			SpringCorePreferences.getProjectPreferences(getProject(), RooCoreActivator.LEGACY_ID).putBoolean(
					RooCoreActivator.PROJECT_PROPERTY_ID, false);
			SpringCorePreferences.getProjectPreferences(getProject(), RooCoreActivator.LEGACY_ID).putString(
					RooCoreActivator.ROO_INSTALL_PROPERTY, rooInstallCombo.getText());
		}
		else {
			SpringCorePreferences.getProjectPreferences(getProject(), RooCoreActivator.LEGACY_ID).putBoolean(
					RooCoreActivator.PROJECT_PROPERTY_ID, true);
		}

		RooCoreActivator.getDefault().savePluginPreferences();

		// always say it is ok
		return super.performOk();
	}

	@Override
	protected Control createPreferenceContent(Composite composite) {
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 2;
		composite.setLayout(layout);

		Label notes = new Label(composite, SWT.WRAP);
		notes
				.setText("If no project specific Roo installation is selected, the workspace default installation will be used. ");
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		notes.setLayoutData(gd);

		// Label spacer = new Label(composite, SWT.NONE);
		// spacer.setLayoutData(gd);

		Label options = new Label(composite, SWT.WRAP);
		options.setText("Roo Installation: ");
		options.setLayoutData(new GridData(GridData.BEGINNING));

		rooInstallCombo = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
		rooInstallCombo.setItems(RooCoreActivator.getDefault().getInstallManager().getAllInstallNames());
		rooInstallCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		String installName = SpringCorePreferences.getProjectPreferences(getProject(), RooCoreActivator.LEGACY_ID)
				.getString(RooCoreActivator.ROO_INSTALL_PROPERTY, null);
		String[] names = rooInstallCombo.getItems();
		for (int i = 0; i < names.length; i++) {
			if (names[i].equals(installName)) {
				rooInstallCombo.select(i);
				break;
			}
		}

		Dialog.applyDialogFont(composite);

		return composite;
	}

	@Override
	protected String getPreferencePageID() {
		return PREF_ID;
	}

	@Override
	protected String getPropertyPageID() {
		return PROP_ID;
	}

	@Override
	protected boolean hasProjectSpecificOptions(IProject project) {
		return !SpringCorePreferences.getProjectPreferences(project, RooCoreActivator.LEGACY_ID).getBoolean(
				RooCoreActivator.PROJECT_PROPERTY_ID, false);
	}
}
