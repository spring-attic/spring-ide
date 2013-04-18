/*******************************************************************************
 *  Copyright (c) 2012, 2013 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.wizard.template;

import java.util.List;

import org.eclipse.jdt.ui.wizards.NewJavaProjectWizardPageOne;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.springframework.ide.eclipse.wizard.template.ProjectWizardDescriptor.BuildType;
import org.springframework.ide.eclipse.wizard.template.ProjectWizardDescriptor.ProjectType;

/**
 * Creates a base Spring project based on a Java project creation.
 * 
 */
public class NewSpringProjectWizardMainPage extends NewJavaProjectWizardPageOne {

	private BuildType buildType = BuildType.Maven;

	private ProjectType projectType = ProjectType.TEMPLATE;

	private SpringVersion version;

	public SpringVersion getVersion() {
		return version;
	}

	@Override
	public void createControl(Composite parent) {
		initializeDialogUnits(parent);

		final Composite composite = new Composite(parent, SWT.NULL);
		composite.setFont(parent.getFont());
		composite.setLayout(initGridLayout(new GridLayout(1, false), true));
		composite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));

		// create UI elements
		Control nameControl = createNameControl(composite);
		nameControl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Control locationControl = createLocationControl(composite);
		locationControl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		createSpringSelectionControl(composite);

		createTemplateSelectionControl(composite);

		Control jreControl = createJRESelectionControl(composite);
		jreControl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Control layoutControl = createProjectLayoutControl(composite);
		layoutControl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Control workingSetControl = createWorkingSetControl(composite);
		workingSetControl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Control infoControl = createInfoControl(composite);
		infoControl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		setControl(composite);
	}

	protected void createSpringSelectionControl(Composite parent) {

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(true).applyTo(composite);
		GridDataFactory.fillDefaults().grab(false, false).applyTo(composite);

		final Combo springVersion = createLabeledCombo(composite, "Select Spring version:");
		List<SpringVersion> versions = SpringVersion.getVersions();
		int length = versions.size();
		String[] versionValues = new String[length];
		int i = 0;
		for (SpringVersion version : versions) {
			if (i < length) {
				versionValues[i++] = version.getVersion() + "";
			}
		}

		springVersion.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				// should always parse correctly
				int selectionIndex = springVersion.getSelectionIndex();
				if (selectionIndex != -1) {
					List<SpringVersion> versions = SpringVersion.getVersions();
					if (selectionIndex < versions.size()) {
						version = versions.get(selectionIndex);
					}
				}
			}
		});

		springVersion.setItems(versionValues);
		springVersion.select(0);

		length = BuildType.values().length;
		String[] buildValues = new String[length];
		i = 0;
		for (BuildType type : BuildType.values()) {
			if (i < length) {
				buildValues[i++] = type.name();
			}
		}

		final Combo buildTypeCombo = createLabeledCombo(composite, "Select Build type:");

		buildTypeCombo.setItems(buildValues);
		buildTypeCombo.select(0);

		buildTypeCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				// should always parse correctly
				int selectionIndex = buildTypeCombo.getSelectionIndex();
				if (selectionIndex != -1 && selectionIndex < BuildType.values().length) {
					buildType = BuildType.values()[selectionIndex];
				}
				update();
			}
		});

	}

	protected Combo createLabeledCombo(Composite parent, String labelValue) {
		Label label = new Label(parent, SWT.NONE);
		GridDataFactory.fillDefaults().grab(false, false).applyTo(label);
		label.setText(labelValue);

		final Combo combo = new Combo(parent, SWT.BORDER | SWT.READ_ONLY);
		GridDataFactory.fillDefaults().grab(false, false).applyTo(combo);
		combo.setEnabled(true);

		return combo;
	}

	protected void createTemplateSelectionControl(Composite parent) {

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(true).applyTo(composite);
		GridDataFactory.fillDefaults().grab(false, false).applyTo(composite);

		projectType = ProjectType.TEMPLATE;

		final Button simple = new Button(composite, SWT.RADIO);
		simple.setText("Simple");
		simple.setToolTipText("Create a simple project");
		simple.setSelection(projectType == ProjectType.SIMPLE);

		simple.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (simple.getSelection()) {
					projectType = ProjectType.SIMPLE;
				}
				else {
					projectType = null;
				}
				update();
			}
		});

		final Button templateButton = new Button(composite, SWT.RADIO);
		templateButton.setText("Template");
		templateButton.setToolTipText("Create a project from a template");
		templateButton.setSelection(projectType == ProjectType.TEMPLATE);

		templateButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (templateButton.getSelection()) {
					projectType = ProjectType.TEMPLATE;
				}
				else {
					projectType = null;
				}
				update();
			}
		});

	}

	protected GridLayout initGridLayout(GridLayout layout, boolean margins) {
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		if (margins) {
			layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
			layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		}
		else {
			layout.marginWidth = 0;
			layout.marginHeight = 0;
		}
		return layout;
	}

	protected void update() {
		getWizard().getContainer().updateButtons();
	}

	public ProjectWizardDescriptor getDescriptor() {
		return new ProjectWizardDescriptor(projectType, buildType);
	}

}
