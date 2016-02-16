/*******************************************************************************
 * Copyright (c) 2007, 2010 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.aop.ui.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.springframework.ide.eclipse.aop.core.Activator;
import org.springframework.ide.eclipse.aop.core.model.IAopReferenceModel;

/**
 * {@link IWorkbenchPreferencePage} that allows to change the persistence
 * property for the {@link IAopReferenceModel}.
 * @author Christian Dupuis
 * @since 2.0
 */
public class AopPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {

	private BooleanFieldEditor booleanEditor;

	protected Control createContents(Composite parent) {

		Composite entryTable = new Composite(parent, SWT.NULL);

		// Create a data that takes up the extra space in the dialog .
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.grabExcessHorizontalSpace = true;
		entryTable.setLayoutData(data);

		GridLayout layout = new GridLayout();
		entryTable.setLayout(layout);

		Label label = new Label(entryTable, SWT.NONE | SWT.WRAP);
		label.setText("Use this preference page to specify whether the AOP Reference model should be saved to file on workbench close.");
		label.setFont(parent.getFont());
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 1;
		gd.widthHint = 300;
		label.setLayoutData(gd);

		Composite colorComposite = new Composite(entryTable, SWT.NONE);
		colorComposite.setLayout(new GridLayout());

		// Create a data that takes up the extra space in the dialog.
		colorComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Group group = new Group(colorComposite, SWT.NONE);
		layout.marginWidth = 3;
		layout.marginHeight = 3;
		group.setLayout(layout);
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		group.setText("Persist AOP Model");

		Composite colorComposite2 = new Composite(group, SWT.NONE);
		colorComposite2.setLayout(layout);
		colorComposite2.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		booleanEditor = new BooleanFieldEditor(Activator.PERSIST_AOP_MODEL_PREFERENCE, 
				"Persist AOP Reference Model", colorComposite2);
		booleanEditor.setPage(this);
		booleanEditor.setPreferenceStore(getPreferenceStore());
		booleanEditor.load();

		return entryTable;
	}

	public void init(IWorkbench workbench) {
		// Initialize the preference store we wish to use
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}

	protected void performDefaults() {
		booleanEditor.loadDefault();
	}

	public boolean performOk() {
		booleanEditor.store();
		return super.performOk();
	}

}