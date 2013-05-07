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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.ui.wizards.NewJavaProjectWizardPageOne;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.springframework.ide.eclipse.wizard.template.infrastructure.Template;

/**
 * Creates a base Spring project based on a Java project creation.
 * 
 */
public class NewSpringProjectWizardMainPage extends NewJavaProjectWizardPageOne implements IWizardPageStatusHandler {

	// private final BuildType buildType = BuildType.Maven;

	private SpringVersion version;

	private Composite mainArea;

	private TemplateSelectionPart part;

	public SpringVersion getVersion() {
		return version;
	}

	@Override
	public void createControl(Composite parent) {
		initializeDialogUnits(parent);

		mainArea = new Composite(parent, SWT.NULL);
		mainArea.setFont(parent.getFont());
		mainArea.setLayout(initGridLayout(new GridLayout(1, false), true));
		mainArea.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));

		// create UI elements
		Control nameControl = createNameControl(mainArea);
		nameControl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Control locationControl = createLocationControl(mainArea);
		locationControl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// createSpringSelectionControl(mainArea);

		part = new TemplateSelectionPart((NewSpringProjectWizard) getWizard(), this);
		Control control = part.createControl(mainArea);
		control.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Control workingSetControl = createWorkingSetControl(mainArea);
		workingSetControl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		setControl(mainArea);

	}

	@Override
	public boolean canFlipToNextPage() {
		// Override the default behaviour so that instead of asking the wizard
		// to get the next page, which may
		// involve downloading a template content, ask the wizard if there are
		// further pages
		// which may rely on template metadata rather than actual
		// downloaded template content.
		return isPageComplete() && ((NewSpringProjectWizard) getWizard()).hasPages(this);
	}

	/**
	 * 
	 * @return the currently selected template, without processing the template
	 * contents.
	 */
	public Template getSelectedTemplate() {
		return part != null ? part.getTemplate() : null;
	}

	public void downloadTemplateContent() throws CoreException {
		if (part != null) {
			part.downloadTemplateData();
		}
	}

	// protected void createSpringSelectionControl(Composite parent) {
	//
	// Composite composite = new Composite(parent, SWT.NONE);
	// composite.setLayout(new GridLayout());
	// GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(true).applyTo(composite);
	// GridDataFactory.fillDefaults().grab(false, false).applyTo(composite);
	//
	// final Combo springVersion = createLabeledCombo(composite,
	// "Select Spring version:");
	// List<SpringVersion> versions = SpringVersion.getVersions();
	// int length = versions.size();
	// String[] versionValues = new String[length];
	// int i = 0;
	// for (SpringVersion version : versions) {
	// if (i < length) {
	// versionValues[i++] = version.getVersion() + "";
	// }
	// }
	//
	// springVersion.addSelectionListener(new SelectionAdapter() {
	// @Override
	// public void widgetSelected(SelectionEvent event) {
	// // should always parse correctly
	// int selectionIndex = springVersion.getSelectionIndex();
	// if (selectionIndex != -1) {
	// List<SpringVersion> versions = SpringVersion.getVersions();
	// if (selectionIndex < versions.size()) {
	// version = versions.get(selectionIndex);
	// }
	// }
	// }
	// });
	//
	// springVersion.setItems(versionValues);
	// springVersion.select(0);
	//
	// length = BuildType.values().length;
	// String[] buildValues = new String[length];
	// i = 0;
	// for (BuildType type : BuildType.values()) {
	// if (i < length) {
	// buildValues[i++] = type.name();
	// }
	// }
	//
	// final Combo buildTypeCombo = createLabeledCombo(composite,
	// "Select Build type:");
	//
	// buildTypeCombo.setItems(buildValues);
	// buildTypeCombo.select(0);
	//
	// buildTypeCombo.addSelectionListener(new SelectionAdapter() {
	// @Override
	// public void widgetSelected(SelectionEvent event) {
	// // should always parse correctly
	// int selectionIndex = buildTypeCombo.getSelectionIndex();
	// if (selectionIndex != -1 && selectionIndex < BuildType.values().length) {
	// buildType = BuildType.values()[selectionIndex];
	// }
	// update();
	//
	// }
	// });
	//
	// }

	protected Combo createLabeledCombo(Composite parent, String labelValue) {
		Label label = new Label(parent, SWT.NONE);
		GridDataFactory.fillDefaults().grab(false, false).applyTo(label);
		label.setText(labelValue);

		final Combo combo = new Combo(parent, SWT.BORDER | SWT.READ_ONLY);
		GridDataFactory.fillDefaults().grab(false, false).applyTo(combo);
		combo.setEnabled(true);

		return combo;
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
		// This will trigger validation of all wizard pages
		getWizard().getContainer().updateButtons();

	}

	public ProjectWizardDescriptor getDescriptor() {
		return new ProjectWizardDescriptor(part.getTemplate());
	}

	public void setStatus(IStatus status, boolean validateWizardPages) {
		if (status == null) {
			setErrorMessage(null);
			setMessage(null);
		}
		else if (status.getSeverity() == IStatus.ERROR) {
			setErrorMessage(status.getMessage());
		}
		else {
			if (status.isOK()) {
				setErrorMessage(null);
				// If the message is OK, set message to null
				if (status.getMessage().equals(Status.OK_STATUS.getMessage())) {
					setMessage(null);
				}
				else {
					setMessage(status.getMessage());
				}
			}
			else if (status.getSeverity() == IStatus.WARNING) {
				setErrorMessage(null);
				setMessage(status.getMessage(), WARNING);
			}

			// Only validate other wizard pages if requested to do so and status
			// is OK or Warning. No need to do so in error
			// case as that will disable the Finish button, which won't require
			// further validation of other pages
			if (validateWizardPages) {
				update();
			}
		}
	}

	@Override
	public boolean isPageComplete() {
		return super.isPageComplete() && part != null && part.isValid();
	}

	@Override
	public void dispose() {
		super.dispose();
		if (part != null) {
			part.dispose();
		}
	}

}
