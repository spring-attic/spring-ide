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

		final Combo springVersion = createLabeledCombo(container, "Select Spring version:");
		List<SpringVersion> versions = SpringVersion.getVersions();
		int length = versions.size();
		String[] versionValues = new String[length];
		int i = 0;
		for (SpringVersion version : versions) {
			if (i < length) {
				versionValues[i++] = version.getDisplay();
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

		springVersion.setItems(versionValues);
		springVersion.select(0);
		return container;

	}

	protected Combo createLabeledCombo(Composite parent, String labelValue) {
		Label label = new Label(parent, SWT.NONE);
		GridDataFactory.fillDefaults().grab(false, false).align(SWT.FILL, SWT.CENTER).applyTo(label);
		label.setText(labelValue);

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
