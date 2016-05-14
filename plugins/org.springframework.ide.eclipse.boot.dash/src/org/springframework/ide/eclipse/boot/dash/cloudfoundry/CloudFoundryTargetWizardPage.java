/*******************************************************************************
 * Copyright (c) 2015, 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cloudfoundry;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFSpace;
import org.springframework.ide.eclipse.boot.dash.model.RunTarget;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.CannotAccessPropertyException;
import org.springframework.ide.eclipse.boot.util.Log;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;
import org.springsource.ide.eclipse.commons.livexp.core.ValueListener;
import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;

/**
 * Creates a Cloud Foundry target by prompting user for credentials and Cloud
 * Foundry target URL.
 *
 *
 */
public class CloudFoundryTargetWizardPage extends WizardPage implements ValueListener<ValidationResult> {

	private Text emailText;

	private Text passwordText;

	private Text urlText;

	private Text spaceValueText;

	private Button trustSelfSigned;

	private Button orgsSpacesButton;

	private boolean canFinish = false;

	private CloudFoundryTargetWizardModel wizardModel;

	private EnableSpaceControlListener credentialsValidationListener = null;

	private SetSpaceValListener setSpaceValListener = null;

	public CloudFoundryTargetWizardPage(CloudFoundryTargetWizardModel model) {
		super("Add a Cloud Foundry Target");
		this.wizardModel = model;
		setTitle("Add a Cloud Foundry Target");
		setDescription("Enter credentials and a Cloud Foundry target URL.");

		this.setImageDescriptor(BootDashActivator.getImageDescriptor("icons/wizban_cloudfoundry.png"));

	}

	public void createControl(Composite parent) {
		Composite area = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).hint(400, SWT.DEFAULT).applyTo(area);
		GridLayoutFactory.fillDefaults().margins(10, 10).numColumns(1).applyTo(area);

		createCredentialsUI(area);

		Dialog.applyDialogFont(area);
		setControl(area);
	}

	private void createCredentialsUI(Composite parent) {

		Composite topComposite = new Composite(parent, SWT.NONE);
		topComposite.setLayout(GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(false).spacing(5, 10).create());
		topComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		Label emailLabel = new Label(topComposite, SWT.NONE);
		emailLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		emailLabel.setText("Email: ");

		emailText = new Text(topComposite, SWT.BORDER);
		emailText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		emailText.setEditable(true);
		emailText.setFocus();

		emailText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				wizardModel.setUsername(emailText.getText());
			}
		});

		Label passwordLabel = new Label(topComposite, SWT.NONE);
		passwordLabel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
		passwordLabel.setText("Password: ");

		Composite passwordComposite = new Composite(topComposite, SWT.NONE);
		GridLayoutFactory.fillDefaults().spacing(5, 2).applyTo(passwordComposite);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(passwordComposite);

		passwordText = new Text(passwordComposite, SWT.PASSWORD | SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(passwordText);
		passwordText.setEditable(true);

		passwordText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				try {
					wizardModel.setPassword(passwordText.getText());
				} catch (CannotAccessPropertyException e1) {
					// Ignore. This will execute without exception
				}
			}
		});

		final Button rememberPassword = new Button(passwordComposite, SWT.CHECK);
		rememberPassword.setText("Remember Password");
		rememberPassword.setSelection(false);
		rememberPassword.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				wizardModel.setStorePassword(rememberPassword.getSelection());
			}

		});

		Label urlLabel = new Label(topComposite, SWT.NONE);
		urlLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		urlLabel.setText("URL: ");

		urlText = new Text(topComposite, SWT.BORDER);
		urlText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		urlText.setEditable(true);

		urlText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				wizardModel.setUrl(urlText.getText());
			}
		});

		Label spacesLabel = new Label(topComposite, SWT.NONE);
		spacesLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		spacesLabel.setText("Space: ");

		Composite buttonComposite = new Composite(topComposite, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(buttonComposite);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(buttonComposite);

		spaceValueText = new Text(buttonComposite, SWT.BORDER);
		spaceValueText.setEnabled(false);
		spaceValueText.setBackground(buttonComposite.getBackground());
		spaceValueText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		orgsSpacesButton = new Button(buttonComposite, SWT.PUSH);
		orgsSpacesButton.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));

		orgsSpacesButton.setText("Select Space...");
		orgsSpacesButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {

				// Fetch an updated list of orgs and spaces in a cancellable
				// operation (i.e. the operation
				// can be cancelled in the wizard's progress bar)
				OrgsAndSpaces spaces = null;
				try {
					spaces = wizardModel.resolveSpaces(getWizard().getContainer());
				} catch (Exception e) {
					Log.log(e);
					setErrorMessage(ExceptionUtil.getMessage(e));
					refreshWizardUI();
					return;
				}
				if (spaces != null) {
					OrgsAndSpacesWizard spacesWizard = new OrgsAndSpacesWizard(wizardModel);
					WizardDialog dialog = new WizardDialog(getShell(), spacesWizard);
					dialog.open();
				}
			}
		});

		trustSelfSigned = new Button(topComposite, SWT.CHECK);
		trustSelfSigned.setText("Self-signed");
		GridDataFactory.fillDefaults().grab(false, false).applyTo(trustSelfSigned);
		trustSelfSigned.setSelection(false);

		trustSelfSigned.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				wizardModel.setSelfsigned(trustSelfSigned.getSelection());
			}

		});

		wizardModel.addAllPropertiesListener(this);
		wizardModel.addCredentialsListener(credentialsValidationListener = new EnableSpaceControlListener());
		wizardModel.addSpaceSelectionListener(setSpaceValListener = new SetSpaceValListener());

		setValuesFromTargetProperties();
		refreshWizardUI();

	}

	private void setValuesFromTargetProperties() {

		String userName = wizardModel.getUsername();
		if (emailText != null && !emailText.isDisposed() && userName != null) {
			emailText.setText(userName);
		}
		String password = null;
		try {
			password = wizardModel.getPassword();
		} catch (CannotAccessPropertyException e) {
			// Ignore
		}
		if (passwordText != null && !passwordText.isDisposed() && password != null) {
			passwordText.setText(password);
		}
		String url = wizardModel.getUrl();
		if (urlText != null && !urlText.isDisposed() && url != null) {
			urlText.setText(url);
		}

	}

	private void refreshWizardUI() {
		if (getWizard() != null && getWizard().getContainer() != null) {
			setPageComplete(canFinish);

			getWizard().getContainer().updateButtons();
		}
	}

	@Override
	public boolean isPageComplete() {
		return canFinish;
	}

	public RunTarget getRunTarget() {
		try {
			return wizardModel.finish();
		} catch (Exception e) {
			Log.log(e);
		}
		return null;
	}

	/*
	 *
	 * Properties change and validation handlers
	 *
	 */

	/*
	 * General validation handler that notifies the wizard if there is an error
	 * in ANY properties validation, whether credentials, URL or org/space.
	 * (non-Javadoc)
	 *
	 * @see
	 * org.springsource.ide.eclipse.commons.livexp.core.ValueListener#gotValue(
	 * org.springsource.ide.eclipse.commons.livexp.core.LiveExpression,
	 * java.lang.Object)
	 */
	@Override
	public void gotValue(LiveExpression<ValidationResult> exp, ValidationResult value) {
		setErrorMessage(null);

		if (value.isOk()) {
			canFinish = true;
			String message = "Press 'Finish' to create the target";
			setMessage(message);
		} else {
			canFinish = false;

			if (value.status == IStatus.ERROR) {
				setErrorMessage(value.msg);
			} else {
				setMessage(value.msg, value.status);
			}
		}
		refreshWizardUI();
	}

	@Override
	public void dispose() {
		wizardModel.removeAllPropertiesListeners(this);
		wizardModel.removeCredentialsListeners(credentialsValidationListener);
		wizardModel.removeSpaceSelectionListeners(setSpaceValListener);
		super.dispose();
	}

	class SetSpaceValListener implements ValueListener<CFSpace> {

		@Override
		public void gotValue(LiveExpression<CFSpace> exp, CFSpace value) {
			if (spaceValueText != null && !spaceValueText.isDisposed()) {
				spaceValueText.setText(value != null ? value.getName() : "");
			}
		}
	}

	/**
	 *
	 * This is a separate listener from the general validation handler that only
	 * enables the org/space UI, and it is only notified when credential
	 * properties are validated, but not org/spaces validation.
	 * <p/>
	 * The reason is that if all credentials and URL are entered, only then
	 * should the org/space UI be enabled. But at the same time, the org/space
	 * UI should not be disabled if there is an error with org/space validation
	 * (which may happen if this is part of the general validation handler), as
	 * it would prevent the user from interacting with the org/space UI.
	 * <p/>
	 * For example, if all credentials and URL are entered, the org/space should
	 * be enabled, whether there are errors with org/space validation or not,
	 * but if a credential or URL is missing, there is no point in enabling the
	 * org/space UI since org/spaces cannot be fetched unless all the missing
	 * credential and URL information is entered by the user.
	 *
	 */
	class EnableSpaceControlListener implements ValueListener<ValidationResult> {

		@Override
		public void gotValue(LiveExpression<ValidationResult> exp, ValidationResult value) {

			if (orgsSpacesButton != null && !orgsSpacesButton.isDisposed()) {
				orgsSpacesButton.setEnabled(value.isOk());
			}
		}
	}
}
