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
package org.springframework.ide.eclipse.osgi.runtime.ui;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.springframework.ide.eclipse.osgi.runtime.OsgiPlugin;


/**
 * {@link PreferencePage} that enables the user to define a post number for the
 * Equninox console.
 * @author Christian Dupuis
 * @author Leo Dos Santos
 * @since 1.0
 */
public class OSGiRuntimePreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private StringFieldEditor textEditor;

	public void init(IWorkbench workbench) {
		// Initialize the preference store we wish to use
		setPreferenceStore(OsgiPlugin.getDefault().getPreferenceStore());
	}

	@Override
	public boolean performOk() {
		textEditor.store();
		return super.performOk();
	}

	@Override
	protected Control createContents(Composite parent) {

		Composite entryTable = new Composite(parent, SWT.NULL);

		// Create a data that takes up the extra space in the dialog .
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.grabExcessHorizontalSpace = true;
		entryTable.setLayoutData(data);

		GridLayout layout = new GridLayout();
		entryTable.setLayout(layout);

		Label label = new Label(entryTable, SWT.NONE | SWT.WRAP);
		label.setText("Use this preference page to specify the port number needed to connect to an running Equinox console.");
		label.setFont(parent.getFont());
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 1;
		gd.widthHint = 300;
		label.setLayoutData(gd);

		Composite radioComposite = new Composite(entryTable, SWT.NONE);
		radioComposite.setLayout(new GridLayout());

		// Create a data that takes up the extra space in the dialog.
		radioComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Composite radioComposite2 = new Composite(radioComposite, SWT.NONE);
		layout.marginWidth = 3;
		layout.marginHeight = 3;
		radioComposite2.setLayout(layout);
		radioComposite2.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		textEditor = new StringFieldEditor(OsgiPlugin.PORT_PREFERENCE_KEY, "Port number:", 10, radioComposite2);
		textEditor.setPage(this);
		textEditor.setPreferenceStore(getPreferenceStore());
		textEditor.load();

		Label noteLabel = new Label(radioComposite, SWT.NONE | SWT.WRAP);
		noteLabel.setText("Note: the port number will also be available by using the "
				+ "${osgi.console.port} placeholder in the OSGi Framework launch configuration.");
		noteLabel.setFont(parent.getFont());
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 1;
		gd.widthHint = 300;
		noteLabel.setLayoutData(gd);

		return entryTable;
	}

	@Override
	protected void performDefaults() {
		textEditor.loadDefault();
	}

}