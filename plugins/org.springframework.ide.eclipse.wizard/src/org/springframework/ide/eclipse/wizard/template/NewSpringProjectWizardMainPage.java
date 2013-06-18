/*******************************************************************************
 *  Copyright (c) 2012, 2013 GoPivotal, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.wizard.template;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.internal.ui.workingsets.IWorkingSetIDs;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.dialogs.WorkingSetGroup;
import org.springframework.ide.eclipse.wizard.template.infrastructure.Template;
import org.springsource.ide.eclipse.commons.content.core.util.Descriptor;

/**
 * Creates a base Spring project based on a Java project creation.
 * 
 */
public class NewSpringProjectWizardMainPage extends WizardPage implements IWizardPageStatusHandler {

	private SpringVersion version;

	private TemplateSelectionPart part;

	private SpringVersionArea springArea;

	private final NewSpringProjectWizardModel model;

	private WorkingSetGroup workingSetGroup;

	private final List<WizardPageArea> wizardAreas = new ArrayList<WizardPageArea>();

	public SpringVersion getVersion() {
		return version;
	}

	public NewSpringProjectWizardMainPage(NewSpringProjectWizardModel model) {
		super(NewSpringProjectWizardMessages.NewProject_title,
				"Create a Spring project by selecting a template or simple project type.", null);
		this.model = model;

	}

	public void createControl(Composite parent) {
		initializeDialogUnits(parent);

		Composite mainArea = new Composite(parent, SWT.NULL);
		mainArea.setFont(parent.getFont());
		mainArea.setLayout(initGridLayout(new GridLayout(1, false), true));

		GridDataFactory.fillDefaults().grab(true, true).applyTo(mainArea);

		new ProjectNameLocationArea(model, this, getShell()).createArea(mainArea);

		(springArea = new SpringVersionArea()).createArea(mainArea);

		(part = new TemplateSelectionPart(getWizard(), model, this)).createArea(mainArea);

		// No need to create an area that handles error events for the working
		// set, as the working set controls do not set error messages,
		// nor do they accept error handlers.
		String[] workingSetIds = new String[] { IWorkingSetIDs.JAVA, IWorkingSetIDs.RESOURCE };
		IStructuredSelection selection = ((NewSpringProjectWizard) getWizard()).getSelection();
		createWorkingSetGroup(mainArea, selection, workingSetIds);

		setControl(mainArea);

		refreshUI();

	}

	public WorkingSetGroup createWorkingSetGroup(Composite composite, IStructuredSelection selection,
			String[] supportedWorkingSetTypes) {

		workingSetGroup = new WorkingSetGroup(composite, selection, supportedWorkingSetTypes);
		return workingSetGroup;
	}

	public IWorkingSet[] getSelectedWorkingSets() {
		return workingSetGroup != null ? workingSetGroup.getSelectedWorkingSets() : new IWorkingSet[0];
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
		if (springArea != null) {
			springArea.refreshUI();
		}
	}

	protected void displayStatus(WizardPageArea area) {

		IStatus status = area.getValidationStatus();
		if (status == null || status.getSeverity() == IStatus.OK) {
			setErrorMessage(null);
			setMessage(null);
			return;
		}

		// Some error messages can be very long which may result in the wizard
		// expanding. Instead, do the following:
		// 1. Show full error in a separate dialogue if it is too long
		// 2. Show shortened error in Wizard
		// 3. Log the error in Eclipse so that it can be accessed by the user
		// for bug reports

		if (status.getSeverity() == IStatus.ERROR) {
			setMessage(null);
			String errorMessage = status.getMessage();
			if (errorMessage == null) {
				errorMessage = "Unknown error occurred in template project wizard. Unable to determine the nature of the error.";
			}
			// If the error message is short enough, show it in the wizard
			// dialogue.
			// Otherwise, open another dialogue to show the full message.
			if (ErrorUtils.isWithinWizardErrorMaximum(errorMessage)) {
				setErrorMessage(errorMessage);
			}
			else {
				// No need to pass the error message, as the dialog retrieves
				// the
				// message from the status
				ErrorDialog.openError(getShell(), NewSpringProjectWizardMessages.NewProject_errorMessage, null, status);
			}
		}
		else {
			setErrorMessage(null);
			setMessage(status.getMessage(), status.getSeverity());
		}

	}

	public void addPageArea(WizardPageArea area) {
		if (area == null || wizardAreas.contains(area)) {
			return;
		}

		wizardAreas.add(area);
	}

	protected WizardPageArea getAreaBySeverity(int severity) {
		// Get an updated status for each area
		for (WizardPageArea area : wizardAreas) {

			IStatus areaStatus = area.getValidationStatus(true);
			if (areaStatus != null && areaStatus.getSeverity() == severity) {
				return area;
			}
		}
		return null;
	}

	public void notifyStatusChange(WizardPageArea currentArea) {
		// Otherwise, see if there are any other errors, and display them
		WizardPageArea nonOKArea = getAreaBySeverity(IStatus.ERROR);
		if (nonOKArea == null) {
			nonOKArea = getAreaBySeverity(IStatus.WARNING);
			if (nonOKArea == null) {
				nonOKArea = getAreaBySeverity(IStatus.INFO);
			}
		}

		if (nonOKArea != null) {
			displayStatus(nonOKArea);

			setPageComplete(nonOKArea.isAreaComplete());
		}
		else {

			// Otherwise Handle the OK status of the current Area.
			boolean isPageComplete = currentArea.isAreaComplete();
			setErrorMessage(null);
			IStatus status = currentArea.getValidationStatus();
			if (status == null) {
				setMessage(null);
			}
			else {
				// If the message is "OK", set message to null, so that
				// the "OK"
				// message doesn't get displayed in the wizard.
				if (status.getMessage().equals(Status.OK_STATUS.getMessage())) {
					setMessage(null);
				}
				else {
					setMessage(status.getMessage());
				}
			}

			setPageComplete(isPageComplete);
		}

		refreshUI();
	}

	@Override
	public boolean isPageComplete() {
		return super.isPageComplete() && !hasErrors();
	}

	protected boolean hasErrors() {
		for (WizardPageArea area : wizardAreas) {
			if (!area.isAreaComplete()) {
				return true;
			}
			IStatus areaStatus = area.getValidationStatus();

			if (areaStatus != null && areaStatus.getSeverity() == IStatus.ERROR) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void dispose() {
		super.dispose();
		if (part != null) {
			part.dispose();
		}
	}

	protected class SpringVersionArea extends WizardPageArea {

		private Combo springVersionCombo;

		public SpringVersionArea() {
			super(NewSpringProjectWizardMainPage.this);
			// For now, any Spring version selection won't block the wizard
			// completion
			setPageComplete(true);
		}

		@Override
		public Control createArea(Composite parent) {
			Composite container = new Composite(parent, SWT.NONE);
			GridLayout layout = new GridLayout();
			layout.numColumns = 2;
			container.setLayout(layout);

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

			GridDataFactory.fillDefaults().grab(true, false).applyTo(container);

			return container;
		}

		@Override
		public void refreshUI() {
			if (springVersionCombo == null || springVersionCombo.isDisposed()) {
				return;
			}

			Template template = ((NewSpringProjectWizard) getWizard()).getModel().selectedTemplate.getValue();
			int selectionIndex = -1;
			String descriptorVersion = null;

			if (template != null) {
				Descriptor descriptor = template.getItem().getLocalDescriptor();
				if (descriptor == null) {
					descriptor = template.getItem().getRemoteDescriptor();
				}
				if (descriptor != null && descriptor.getSpringVersion() != null) {
					// if the template has a spring version defined, enable the
					// spring version widget, as the template spring version can
					// now
					// be used for string substitution when processing a
					// template.
					// This allows
					// a user to select another spring version that should be
					// the
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

			// Whether the spring version is in the combo list or not, the fact
			// that
			// there is a spring version in the descriptor should be enouch
			// criteria
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

		@Override
		protected IStatus validateArea() {
			return Status.OK_STATUS;
		}
	}

}
