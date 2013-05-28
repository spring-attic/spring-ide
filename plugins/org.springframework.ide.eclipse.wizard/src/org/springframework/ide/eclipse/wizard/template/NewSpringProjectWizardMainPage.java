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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.ui.wizards.NewJavaProjectWizardPageOne;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.springframework.ide.eclipse.wizard.template.infrastructure.Template;
import org.springsource.ide.eclipse.commons.content.core.util.Descriptor;

/**
 * Creates a base Spring project based on a Java project creation.
 * 
 */
public class NewSpringProjectWizardMainPage extends NewJavaProjectWizardPageOne implements IWizardPageStatusHandler {

	private SpringVersion version;

	private Composite mainArea;

	private TemplateSelectionPart part;

	private Combo springVersionCombo;

	public SpringVersion getVersion() {
		return version;
	}

	@Override
	public void createControl(Composite parent) {
		initializeDialogUnits(parent);

		mainArea = new Composite(parent, SWT.NULL);
		mainArea.setFont(parent.getFont());
		mainArea.setLayout(initGridLayout(new GridLayout(1, false), true));

		GridDataFactory.fillDefaults().grab(true, true).applyTo(mainArea);

		// create UI elements
		Control nameControl = createNameControl(mainArea);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(nameControl);

		Control locationControl = createLocationControl(mainArea);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(locationControl);

		Control springControl = createSpringSelectionControl(mainArea);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(springControl);

		part = new TemplateSelectionPart((NewSpringProjectWizard) getWizard(), this);
		part.createControl(mainArea);

		Control workingSetControl = createWorkingSetControl(mainArea);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(workingSetControl);

		// Create it to avoid NPEs when the superclass attempts to read/write to
		// the widget,
		// but don't add it to the layout as its usually empty and takes up
		// space
		Control infoControl = createInfoControl(mainArea);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(infoControl);
		GridData data = (GridData) infoControl.getLayoutData();
		data.exclude = true;
		infoControl.setLayoutData(data);

		setControl(mainArea);

		refreshUI();

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

	protected Control createSpringSelectionControl(Composite parent) {

		Composite container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		container.setLayout(layout);

		GridDataFactory.fillDefaults().grab(false, false).applyTo(container);

		springVersionCombo = createLabeledCombo(
				container,
				"Select Spring version:",
				"Select a Spring version when building the project. Use 'Default' when using the template-defined Spring version. Spring version selection may not be available on all templates.");
		List<SpringVersion> versions = SpringVersion.getVersions();
		int length = versions.size();
		String[] versionValues = new String[length];
		int i = 0;
		for (SpringVersion version : versions) {
			if (i < length) {
				versionValues[i++] = version.getDisplay();
			}
		}

		springVersionCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				// should always parse correctly
				int selectionIndex = springVersionCombo.getSelectionIndex();
				if (selectionIndex != -1) {
					List<SpringVersion> versions = SpringVersion.getVersions();
					if (selectionIndex < versions.size()) {
						SpringVersion selectedVersion = versions.get(selectionIndex);
						if (SpringVersion.DEFAULT.equals(selectedVersion)) {
							version = null;
						}
						else {
							version = selectedVersion;
						}
					}
				}
			}
		});

		springVersionCombo.setItems(versionValues);
		springVersionCombo.select(0);

		return container;

	}

	protected Combo createLabeledCombo(Composite parent, String labelValue, String labelTooltip) {
		Label label = new Label(parent, SWT.NONE);
		GridDataFactory.fillDefaults().grab(false, false).align(SWT.FILL, SWT.CENTER).applyTo(label);
		label.setText(labelValue);

		if (labelTooltip != null) {
			label.setToolTipText(labelTooltip);
		}

		final Combo combo = new Combo(parent, SWT.BORDER | SWT.READ_ONLY);
		GridDataFactory.fillDefaults().grab(false, false).applyTo(combo);
		combo.setEnabled(true);

		return combo;
	}

	protected GridLayout initGridLayout(GridLayout layout, boolean margins) {
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
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

	protected void refreshUI() {
		refreshSpringVersion();
	}

	protected void refreshSpringVersion() {
		if (springVersionCombo == null || springVersionCombo.isDisposed()) {
			return;
		}

		Template template = part != null ? part.getTemplate() : null;
		int selectionIndex = -1;
		String descriptorVersion = null;

		if (template != null) {
			Descriptor descriptor = template.getItem().getLocalDescriptor();
			if (descriptor == null) {
				descriptor = template.getItem().getRemoteDescriptor();
			}
			if (descriptor != null && descriptor.getSpringVersion() != null) {
				// if the template has a spring version defined, enable the
				// spring version widget, as the template spring version can now
				// be used for string substitution when processing a template.
				// This allows
				// a user to select another spring version that should be the
				// replacement
				// during the template processing
				descriptorVersion = descriptor.getSpringVersion();

				SpringVersion resolvedVersion = SpringVersion.resolveSpringVersion(descriptorVersion);
				if (resolvedVersion != null) {

					String[] comboValues = springVersionCombo.getItems();
					for (int i = 0; i < comboValues.length; i++) {
						if (resolvedVersion.getDisplay().equals(comboValues[i])) {
							selectionIndex = i;
							break;
						}
					}
				}
			}
		}

		// Whether the spring version is in the combo list or not, the fact that
		// there is a spring version in the descriptor should be enouch criteria
		// to enable
		// the widget
		if (descriptorVersion != null) {
			springVersionCombo.setEnabled(true);
			if (selectionIndex >= 0) {
				springVersionCombo.select(selectionIndex);
			}
		}
		else {
			springVersionCombo.select(0);
			springVersionCombo.setEnabled(false);
		}
	}

	public ProjectWizardDescriptor getDescriptor() {
		return new ProjectWizardDescriptor(part.getTemplate());
	}

	protected void handleError(IStatus status) {
		// Some error messages can be very long which may result in the wizard
		// expanding. Instead, do the following:
		// 1. Show full error in a separate dialogue if it is too long
		// 2. Show shortened error in Wizard
		// 3. Log the error in Eclipse so that it can be accessed by the user
		// for bug reports
		String errorMessage = status.getMessage();
		if (errorMessage == null) {
			errorMessage = "Unknown error occurred in template project wizard. Unable to determine the nature of the error.";
		}
		if (ErrorUtils.isWithinWizardErrorMaximum(errorMessage)) {
			setErrorMessage(errorMessage);
		}
		else {
			// No need to pass the error message, as the dialog retrieves the
			// message from the status
			ErrorDialog.openError(getShell(), NewSpringProjectWizardMessages.NewProject_errorMessage, null, status);
		}

	}

	public void setStatus(IStatus status, boolean validateWizardPages) {
		if (status == null) {
			setErrorMessage(null);
			setMessage(null);
		}
		else if (status.getSeverity() == IStatus.ERROR) {
			handleError(status);
		}
		else {
			if (status.isOK()) {
				setErrorMessage(null);
				// If the message is "OK", set message to null, so that the "OK"
				// message doesn't get displayed in the wizard.
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
		refreshUI();
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
