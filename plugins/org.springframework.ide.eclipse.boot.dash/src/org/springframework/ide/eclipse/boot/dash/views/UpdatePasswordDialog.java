/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
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

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * @author Terry Denney
 */
public class UpdatePasswordDialog extends MessageDialog {

	private String password;

	private String targetId;

	private Label description;

	public UpdatePasswordDialog(Shell parentShell, String username, String targetId) {
		super(parentShell, "Update Password", null, "Update password for " + username, CONFIRM,
				new String[] { IDialogConstants.OK_LABEL, IDialogConstants.CANCEL_LABEL }, 0);
		this.targetId = targetId;
		setShellStyle(getShellStyle() | SWT.RESIZE);
	}

	public String getPassword() {
		return password;
	}

	@Override
	protected Control createCustomArea(Composite parent) {

		Composite area = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(area);
		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(area);

		description = new Label(area, SWT.NONE | SWT.WRAP);

		// Set a minimum width such that the wrapped text does not enlarge the
		// dialogue with extra space
		GridDataFactory.fillDefaults().hint(400, SWT.DEFAULT).applyTo(description);
		description.setText("The password must match your existing target credentials in " + targetId
				+ ". This only updates the password locally.");

		Composite composite = new Composite(area, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(composite);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(composite);

		Label updatePasswordLabel = new Label(composite, SWT.NONE);
		updatePasswordLabel.setText("Password:");
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(false, false).applyTo(updatePasswordLabel);

		final Text newPasswordText = new Text(composite, SWT.PASSWORD | SWT.BORDER);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(newPasswordText);
		newPasswordText.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				password = newPasswordText.getText();
				update();
			}
		});

		return area;
	}

	private void update() {
		getButton(OK).setEnabled(password != null && password.length() > 0);

		if (password == null || password.length() == 0) {
			description.setText("Please enter a password.");
			getButton(OK).setEnabled(false);
		} else {
			description.setText("Please press 'OK' to update the password.");
			getButton(OK).setEnabled(true);
		}
	}

	@Override
	protected Control createButtonBar(Composite parent) {
		Control buttonBar = super.createButtonBar(parent);
		getButton(OK).setEnabled(false);
		return buttonBar;
	}
}
