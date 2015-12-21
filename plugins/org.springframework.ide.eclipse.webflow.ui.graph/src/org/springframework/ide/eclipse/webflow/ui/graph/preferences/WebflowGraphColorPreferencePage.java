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
package org.springframework.ide.eclipse.webflow.ui.graph.preferences;

import org.eclipse.jface.preference.ColorFieldEditor;
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
import org.springframework.ide.eclipse.webflow.core.internal.model.ActionState;
import org.springframework.ide.eclipse.webflow.core.internal.model.DecisionState;
import org.springframework.ide.eclipse.webflow.core.internal.model.EndState;
import org.springframework.ide.eclipse.webflow.core.internal.model.SubflowState;
import org.springframework.ide.eclipse.webflow.core.internal.model.ViewState;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;
import org.springframework.ide.eclipse.webflow.ui.graph.Activator;

/**
 * {@link IWorkbenchPreferencePage} that enables the user to modify the UI colors
 * of the {@link IWebflowModelElement} displayed in the Web Flow Graph editor.
 * @author Christian Dupuis
 * @since 2.0
 */
public class WebflowGraphColorPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {

	private ColorFieldEditor viewStateColorEditor;

	private ColorFieldEditor actionStateColorEditor;

	private ColorFieldEditor subflowStateColorEditor;

	private ColorFieldEditor decisionStateColorEditor;

	private ColorFieldEditor endStateColorEditor;

	/*
	 * @see PreferencePage#createContents(Composite)
	 */
	protected Control createContents(Composite parent) {

		Composite entryTable = new Composite(parent, SWT.NULL);

		// Create a data that takes up the extra space in the dialog .
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.grabExcessHorizontalSpace = true;
		entryTable.setLayoutData(data);

		GridLayout layout = new GridLayout();
		entryTable.setLayout(layout);

		Label label = new Label(entryTable, SWT.NONE | SWT.WRAP);
		label.setText("Use this preference page to adjust the background colors of the state shapes within the Web Flow Editor.");
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
		group.setText("State Colors");
		
		Composite colorComposite2 = new Composite(group, SWT.NONE);
		colorComposite2.setLayout(layout);
		colorComposite2.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		viewStateColorEditor = new ColorFieldEditor(WebflowGraphPreferences
				.getPreferenceName(ViewState.class.getName()), "View State:",
				colorComposite2);
		viewStateColorEditor.setPage(this);
		viewStateColorEditor.setPreferenceStore(getPreferenceStore());
		viewStateColorEditor.load();
		
		actionStateColorEditor = new ColorFieldEditor(WebflowGraphPreferences
				.getPreferenceName(ActionState.class.getName()),
				"Action State:", colorComposite2);
		actionStateColorEditor.setPage(this);
		actionStateColorEditor.setPreferenceStore(getPreferenceStore());
		actionStateColorEditor.load();

		subflowStateColorEditor = new ColorFieldEditor(WebflowGraphPreferences
				.getPreferenceName(SubflowState.class.getName()),
				"Subflow State:", colorComposite2);
		subflowStateColorEditor.setPage(this);
		subflowStateColorEditor.setPreferenceStore(getPreferenceStore());
		subflowStateColorEditor.load();

		decisionStateColorEditor = new ColorFieldEditor(WebflowGraphPreferences
				.getPreferenceName(DecisionState.class.getName()),
				"Decision State:", colorComposite2);
		decisionStateColorEditor.setPage(this);
		decisionStateColorEditor.setPreferenceStore(getPreferenceStore());
		decisionStateColorEditor.load();

		endStateColorEditor = new ColorFieldEditor(WebflowGraphPreferences
				.getPreferenceName(EndState.class.getName()), "End State",
				colorComposite2);
		endStateColorEditor.setPage(this);
		endStateColorEditor.setPreferenceStore(getPreferenceStore());
		endStateColorEditor.load();

		return entryTable;
	}

	/*
	 * @see IWorkbenchPreferencePage#init(IWorkbench)
	 */
	public void init(IWorkbench workbench) {
		// Initialize the preference store we wish to use
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}

	/**
	 * Performs special processing when this page's Restore Defaults button has
	 * been pressed. Sets the contents of the color field to the default value
	 * in the preference store.
	 */
	protected void performDefaults() {
		viewStateColorEditor.loadDefault();
		actionStateColorEditor.loadDefault();
		subflowStateColorEditor.loadDefault();
		decisionStateColorEditor.loadDefault();
		endStateColorEditor.loadDefault();
	}

	/**
	 * Method declared on IPreferencePage. Save the color preference to the
	 * preference store.
	 */
	public boolean performOk() {
		viewStateColorEditor.store();
		actionStateColorEditor.store();
		subflowStateColorEditor.store();
		decisionStateColorEditor.store();
		endStateColorEditor.store();
		WebflowGraphPreferences.clearColorCache();
		return super.performOk();
	}

}