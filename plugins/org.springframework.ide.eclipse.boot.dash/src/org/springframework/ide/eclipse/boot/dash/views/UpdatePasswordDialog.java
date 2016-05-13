/*******************************************************************************
 * Copyright (c) 2015, 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *
 *     Original from org.cloudfoundry.ide.eclipse.server.ui.internal
 *     and implemented under:
 *
 * Copyright (c) 2012, 2014 Pivotal Software, Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.views;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.springframework.ide.eclipse.boot.dash.dialogs.PasswordDialogModel;

/**
 * Dialog for setting the password and "store password" flag.
 *
 * @author Terry Denney
 * @author Alex Boyko
 */
public class UpdatePasswordDialog extends TitleAreaDialog {

	private static final String PLEASE_PRESS_OK_TO_SET_THE_PASSWORD = "Please press 'OK' to set the password.";
	private static final String PLEASE_ENTER_A_PASSWORD = "Please enter a password.";
	private PasswordDialogModel model;

	public UpdatePasswordDialog(Shell parentShell, PasswordDialogModel model) {
		super(parentShell);
		this.model = model;
	}

	@Override
	protected Control createDialogArea(Composite parent) {

		setTitle("Enter Password");
		if (model.getPasswordVar().getValue() == null || model.getPasswordVar().getValue().isEmpty()) {
			setMessage(PLEASE_ENTER_A_PASSWORD, IStatus.INFO);
		} else {
			setMessage(PLEASE_PRESS_OK_TO_SET_THE_PASSWORD, IStatus.INFO);
		}

		Composite composite = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(composite);
		GridLayoutFactory.fillDefaults().numColumns(2).margins(10, 10).spacing(10, 10).applyTo(composite);

		Label description = new Label(composite, SWT.NONE | SWT.WRAP);

		// Set a minimum width such that the wrapped text does not enlarge the
		// dialogue with extra space
		GridDataFactory.fillDefaults().span(2, 1).hint(300, SWT.DEFAULT).applyTo(description);
		description.setText("The password must match your existing target credentials in " + model.getTargetId() + ".");

		Label updatePasswordLabel = new Label(composite, SWT.NONE);
		updatePasswordLabel.setText("Password:");
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.TOP).grab(false, false).applyTo(updatePasswordLabel);

		Composite passwordComposite = new Composite(composite, SWT.NONE);
		GridLayoutFactory.fillDefaults().spacing(10, 10).applyTo(passwordComposite);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(passwordComposite);

		final Text newPasswordText = new Text(passwordComposite, SWT.PASSWORD | SWT.BORDER);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(newPasswordText);
		newPasswordText.setText(model.getPasswordVar().getValue() == null ? "" : model.getPasswordVar().getValue());
		newPasswordText.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				String password = newPasswordText.getText();
				model.getPasswordVar().setValue(password);

				getButton(OK).setEnabled(password != null && password.length() > 0);

				if (password == null || password.isEmpty()) {
					setMessage(PLEASE_ENTER_A_PASSWORD, IStatus.INFO);
					getButton(OK).setEnabled(false);
				} else {
					setMessage(PLEASE_PRESS_OK_TO_SET_THE_PASSWORD, IStatus.INFO);
					getButton(OK).setEnabled(true);
				}
			}
		});

		final Button rememberPassword = new Button(passwordComposite, SWT.CHECK);
		rememberPassword.setText("Remember Password");
		rememberPassword.setSelection(model.getStoreVar().getValue());
		GridDataFactory.fillDefaults().applyTo(rememberPassword);
		rememberPassword.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				model.getStoreVar().setValue(rememberPassword.getSelection());
			}

		});

		parent.pack(true);

		return composite;
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	@Override
	protected void buttonPressed(int buttonId) {
		model.buttonPressed(buttonId);
		super.buttonPressed(buttonId);
	}

}
