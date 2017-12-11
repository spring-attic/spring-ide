/*******************************************************************************
 * Copyright (c) 2016 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.dialogs;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.SelectionDialog;

public class ReplaceExistingApplicationDialog extends SelectionDialog {


	private final String message;

	public ReplaceExistingApplicationDialog(Shell parent, String title, String message) {
		super(parent);
		setTitle(title);
		this.message = message;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);

		Label messageLabel = new Label(composite, SWT.WRAP);
		messageLabel.setText(message);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(messageLabel);
		return composite;
	}
}
