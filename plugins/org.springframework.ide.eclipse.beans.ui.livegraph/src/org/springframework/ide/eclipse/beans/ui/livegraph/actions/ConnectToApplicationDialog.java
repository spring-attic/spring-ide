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
package org.springframework.ide.eclipse.beans.ui.livegraph.actions;

import org.eclipse.jface.dialogs.Dialog;
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
 * @author Leo Dos Santos
 */
public class ConnectToApplicationDialog extends Dialog {

	private Label description;

	private String serviceUrl = "service:jmx:rmi:///jndi/rmi://127.0.0.1:6969/jmxrmi";

	private String appName = "";

	private String username;

	private String password;

	public ConnectToApplicationDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	protected Control createButtonBar(Composite parent) {
		Control buttonBar = super.createButtonBar(parent);
		getButton(OK).setEnabled(false);
		return buttonBar;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		getShell().setText("Connect to Application...");
		Composite control = (Composite) super.createDialogArea(parent);

		Composite composite = new Composite(control, SWT.NONE);
		GridDataFactory.fillDefaults().applyTo(composite);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(composite);

		description = new Label(composite, SWT.NONE);
		description.setText("Enter a JMX service URL and application name");
		GridDataFactory.fillDefaults().span(2, 1).applyTo(description);

		Label serviceLabel = new Label(composite, SWT.NONE);
		serviceLabel.setText("Service URL:");
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).applyTo(serviceLabel);

		final Text serviceText = new Text(composite, SWT.BORDER);
		serviceText.setText(serviceUrl);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).hint(350, SWT.DEFAULT)
				.applyTo(serviceText);
		serviceText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				serviceUrl = serviceText.getText();
				update();
			}
		});

		Label serviceHint = new Label(composite, SWT.NONE);
		serviceHint.setText("ex: service:jmx:rmi://localhost/jndi/rmi://localhost:1099/jmxrmi");
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.TOP).span(2, 1).applyTo(serviceHint);

		Label appLabel = new Label(composite, SWT.NONE);
		appLabel.setText("Application Name:");
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).applyTo(appLabel);

		final Text appText = new Text(composite, SWT.BORDER);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).hint(350, SWT.DEFAULT)
				.applyTo(appText);
		appText.setText(appName);
		appText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				appName = appText.getText();
				update();
			}
		});

		Label authentication = new Label(composite, SWT.NONE);
		authentication.setText("Server Authentication (if required):");
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).span(2, 1).applyTo(authentication);

		Label usernameLabel = new Label(composite, SWT.NONE);
		usernameLabel.setText("Username:");
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).applyTo(usernameLabel);

		final Text usernameText = new Text(composite, SWT.BORDER);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).hint(350, SWT.DEFAULT)
				.applyTo(usernameText);
		usernameText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				username = usernameText.getText();
				update();
			}
		});

		Label passLabel = new Label(composite, SWT.NONE);
		passLabel.setText("Password:");
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).applyTo(passLabel);

		final Text passText = new Text(composite, SWT.BORDER | SWT.PASSWORD);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).hint(350, SWT.DEFAULT)
				.applyTo(passText);
		passText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				password = passText.getText();
				update();
			}
		});

		return control;
	}

	public String getApplicationName() {
		return appName;
	}

	public String getPassword() {
		return password;
	}

	public String getServiceUrl() {
		return serviceUrl;
	}

	public String getUsername() {
		return username;
	}

	private void update() {
		if (serviceUrl == null || serviceUrl.length() == 0) {
			description.setText("Enter a valid JMX service URL");
			getButton(OK).setEnabled(false);
		}
		else if (appName == null /* || appName.length() == 0 */) {
			description.setText("Enter an application name");
			getButton(OK).setEnabled(false);
		}
		else {
			description.setText("Select OK to proceed");
			getButton(OK).setEnabled(true);
		}
	}

}
