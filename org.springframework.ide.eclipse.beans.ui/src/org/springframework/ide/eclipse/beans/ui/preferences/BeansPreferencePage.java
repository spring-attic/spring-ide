/*******************************************************************************
 * Copyright (c) 2005, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.preferences;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.springframework.ide.eclipse.beans.core.model.IBeansModel;
import org.springframework.ide.eclipse.beans.ui.BeansUIPlugin;

/**
 * {@link IWorkbenchPreferencePage} that allows to change the persistence
 * property for the {@link IBeansModel}.
 * @author Christian Dupuis
 * @since 2.0
 */
public class BeansPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {

	private RadioGroupFieldEditor radioEditor;

	protected Control createContents(Composite parent) {

		Composite entryTable = new Composite(parent, SWT.NULL);

		// Create a data that takes up the extra space in the dialog .
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.grabExcessHorizontalSpace = true;
		entryTable.setLayoutData(data);

		GridLayout layout = new GridLayout();
		entryTable.setLayout(layout);

		Label label = new Label(entryTable, SWT.NONE);
		label.setText("Use this preference page to specify the default Double Click Action\n"
			+ "on the Spring Explorer.");

		Composite radioComposite = new Composite(entryTable, SWT.NONE);
		radioComposite.setLayout(new GridLayout());

		// Create a data that takes up the extra space in the dialog.
		radioComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Composite radioComposite2 = new Composite(radioComposite, SWT.NONE);
		layout.marginLeft = 2;
		radioComposite2.setLayout(layout);
		radioComposite2.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		radioEditor = new RadioGroupFieldEditor(
				BeansUIPlugin.DEFAULT_DOUBLE_CLICK_ACTION_PREFERENCE_ID,
				"Default Double Click Action", 1, new String[][] {{"Open Configuration File", "true"}, {"Open Java Element", "false"}},
				radioComposite2, true);
		radioEditor.setPage(this);
		radioEditor.setPreferenceStore(getPreferenceStore());
		radioEditor.load();

		return entryTable;
	}

	public void init(IWorkbench workbench) {
		// Initialize the preference store we wish to use
		setPreferenceStore(BeansUIPlugin.getDefault().getPreferenceStore());
	}

	protected void performDefaults() {
		radioEditor.loadDefault();
	}

	public boolean performOk() {
		radioEditor.store();
		return super.performOk();
	}

}