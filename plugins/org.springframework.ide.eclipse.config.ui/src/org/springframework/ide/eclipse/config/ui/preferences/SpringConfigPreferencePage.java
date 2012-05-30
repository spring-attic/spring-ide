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
package org.springframework.ide.eclipse.config.ui.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.springframework.ide.eclipse.config.core.preferences.SpringConfigPreferenceConstants;
import org.springframework.ide.eclipse.config.ui.ConfigUiPlugin;


/**
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
public class SpringConfigPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private BooleanFieldEditor gefEnablementButton;

	@Override
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridData data = new GridData(GridData.FILL, GridData.FILL, true, true);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		composite.setLayout(layout);
		composite.setLayoutData(data);
		composite.setFont(parent.getFont());

		Label label = new Label(composite, SWT.WRAP);
		label.setText(Messages.getString("SpringConfigPreferencePage_PREF_PAGE_HEADER")); //$NON-NLS-1$

		Composite gefComposite = new Composite(composite, SWT.NONE);
		gefComposite.setLayout(new GridLayout());
		gefComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Composite innerComposite = new Composite(gefComposite, SWT.NONE);
		innerComposite.setLayout(new GridLayout());
		innerComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		gefEnablementButton = new BooleanFieldEditor(SpringConfigPreferenceConstants.PREF_ENABLE_GEF_PAGES, Messages
				.getString("SpringConfigPreferencePage_GEF_PREF_LABEL"), innerComposite); //$NON-NLS-1$
		gefEnablementButton.setPage(this);
		gefEnablementButton.setPreferenceStore(getPreferenceStore());
		gefEnablementButton.load();

		applyDialogFont(composite);
		return composite;
	}

	@Override
	protected IPreferenceStore doGetPreferenceStore() {
		return ConfigUiPlugin.getDefault().getPreferenceStore();
	}

	public void init(IWorkbench workbench) {
		setPreferenceStore(doGetPreferenceStore());
	}

	@Override
	protected void performDefaults() {
		gefEnablementButton.loadDefault();
		super.performDefaults();
	}

	@Override
	public boolean performOk() {
		gefEnablementButton.store();
		return super.performOk();
	}

}
