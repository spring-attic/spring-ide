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
package org.springframework.ide.eclipse.roo.ui.internal.properties;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.springframework.ide.eclipse.roo.core.internal.model.DefaultRooInstall;
import org.springframework.ide.eclipse.roo.core.model.IRooInstall;
import org.springframework.ide.eclipse.roo.ui.RooUiActivator;
import org.springframework.util.StringUtils;


/**
 * @author Christian Dupuis
 */
public class RooInstallDialog extends TitleAreaDialog {

	private Text homeText;

	private Text nameText;

	private Text versionText;

	private String home;

	private String name;

	private final IRooInstall install;

	private final InstalledRooInstallBlock prefPage;

	public RooInstallDialog(Shell parentShell, IRooInstall install, InstalledRooInstallBlock parent) {
		super(parentShell);
		this.prefPage = parent;
		this.install = install;
		this.name = install.getName();
		this.home = install.getHome();
	}

	public IRooInstall getResult() {
		return new DefaultRooInstall(home, name, install.isDefault());
	}

	private void clearError() {
		getButton(OK).setEnabled(true);
		setErrorMessage(null);
	}

	private void setError(String message) {
		getButton(OK).setEnabled(false);
		setErrorMessage(message);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.dialogs.TitleAreaDialog#createDialogArea(org.eclipse
	 * .swt.widgets.Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite parentComposite = (Composite) super.createDialogArea(parent);
		Composite composite = new Composite(parentComposite, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 10;
		layout.marginWidth = 10;
		layout.numColumns = 2;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label name = new Label(composite, SWT.WRAP);
		name.setText("Name:");
		// name.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		nameText = new Text(composite, SWT.BORDER);
		nameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		if (install.getName() != null) {
			nameText.setText(install.getName());
		}
		nameText.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				RooInstallDialog.this.name = nameText.getText();
				validate(true);
			}
		});

		Label version = new Label(composite, SWT.WRAP);
		version.setText("Version:");
		// version.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		versionText = new Text(composite, SWT.BORDER);
		versionText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		versionText.setEnabled(false);
		if (install.getVersion() != null) {
			versionText.setText(install.getVersion());
		}

		Label directory = new Label(composite, SWT.WRAP);
		directory.setText("Roo home:");
		// directory.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		homeText = new Text(composite, SWT.BORDER);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = 250;
		homeText.setLayoutData(data);
		homeText.setEnabled(false);
		if (install.getHome() != null) {
			homeText.setText(install.getHome());
		}
		homeText.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				validate(true);
			}
		});

		new Label(composite, SWT.WRAP);

		Button browseButton = new Button(composite, SWT.PUSH);
		browseButton.setText("Browse...");
		browseButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
		browseButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dialog = new DirectoryDialog(getShell());
				dialog.setMessage("Select Roo installation directory");
				dialog.setText("Roo installation directory");
				String result = dialog.open();
				if (StringUtils.hasText(result)) {
					homeText.setText(result);
					home = result;
					validate(true);
					if (!StringUtils.hasText(nameText.getText()) && StringUtils.hasText(versionText.getText())) {
						nameText.setText(prefPage.generateName("Roo " + versionText.getText(), install));
					}
				}

			}
		});


		setTitle("Configure Roo Installation");
		setTitleImage(RooUiActivator.getImageDescriptor("icons/full/wizban/roo_wizban.png").createImage());

		Dialog.applyDialogFont(composite);
		return composite;
	}
	
	protected void validate(boolean validateHome) {
		clearError();
		if (homeText.getText() == null || homeText.getText().equals("")) {
			setError("Select a Roo home directory");
			return;
		}
		else if (validateHome) {
			IRooInstall install = new DefaultRooInstall(homeText.getText(), nameText.getText(), false);
			IStatus status = install.validate();
			if (!status.isOK()) {
				setError(status.getMessage());
			}
			versionText.setText(install.getVersion());

		}
		if (nameText.getText() == null || nameText.getText().equals("")) {
			setError("A unique name is required");
		}
		else {
			if (prefPage.isDuplicateName(nameText.getText(), install)) {
				setError("Name is not unique");
			}
		}
	}
}
