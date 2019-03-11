/*******************************************************************************
 *  Copyright (c) 2012 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/

/*
 * @author Kaitlin Duck Sherwood
 */

package org.springframework.ide.eclipse.wizard.template.util;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class AddEditNameUrlDialog extends Dialog {
	AbstractNameUrlPreferenceModel model;

	Text nameText;

	Text urlText;

	String name;

	String urlString;

	private final String explanatoryText;

	protected Label errorTextLabel;

	protected Composite composite;

	private String title;

	public AddEditNameUrlDialog(Shell parent, AbstractNameUrlPreferenceModel aModel, NameUrlPair nameUrl,
			String headerText) {
		super(parent);
		explanatoryText = headerText;
		model = aModel;
		if (nameUrl != null) {
			name = nameUrl.getName();
			urlString = nameUrl.getUrlString();
		}
		else {
			name = null;
			urlString = null;
		}
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		composite = new Composite(parent, SWT.NONE);

		GridLayoutFactory.fillDefaults().numColumns(2).extendedMargins(5, 13, 10, 0).applyTo(composite);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(composite);

		Label explanatoryTextLabel = new Label(composite, SWT.WRAP);
		explanatoryTextLabel.setText(explanatoryText);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(explanatoryTextLabel);

		Label nameLabel = new Label(composite, SWT.NONE);
		nameLabel.setText(NLS.bind("Name:", null));
		nameLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));

		nameText = new Text(composite, SWT.BORDER + SWT.FILL);
		GridDataFactory.fillDefaults().grab(true, false).hint(300, SWT.DEFAULT).applyTo(nameText);
		nameText.setEditable(true);
		if (name != null && name.length() > 0) {
			nameText.setText(name);
		}

		Label urlLabel = new Label(composite, SWT.NONE);
		urlLabel.setText(NLS.bind("URL:", null));
		urlLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
		urlText = new Text(composite, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).hint(300, SWT.DEFAULT).applyTo(urlText);
		urlText.setEditable(true);
		if (urlString != null && urlString.length() > 0) {
			urlText.setText(urlString);
		}

		urlText.addKeyListener(getUrlValidationListener());

		String errorText = "";
		errorTextLabel = new Label(composite, SWT.WRAP);
		errorTextLabel.setText(errorText);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(errorTextLabel);

		// getButton(IDialogConstants.OK_ID).setEnabled(validateUrl(urlString));

		return composite;
	}

	@Override
	public void create() {
		super.create();
		if (title != null) {
			getShell().setText(title);
		}
		getButton(IDialogConstants.OK_ID).setEnabled(validateUrl(urlString));
	}

	protected KeyListener getUrlValidationListener() {
		return new KeyListener() {

			public void keyReleased(KeyEvent e) {

				String urlString = ((Text) e.getSource()).getText().trim();
				if (!validateUrl(urlString)) {
					getButton(IDialogConstants.OK_ID).setEnabled(false);
				}
				else {
					errorTextLabel.setText("");
					composite.update();
					getButton(IDialogConstants.OK_ID).setEnabled(true);
				}
			}

			public void keyPressed(KeyEvent e) {
				// do nothing
			}
		};
	}

	@Override
	protected void okPressed() {
		name = nameText.getText();
		urlString = urlText.getText();
		if (urlString.length() > 0) {
			if (name.length() <= 0) {
				name = urlString;
			}
		}

		super.okPressed();
	}

	public String getUrlString() {
		return urlString;
	}

	public String getName() {
		return name;
	}

	protected boolean validateUrl(String urlString) {
		if (urlString != null && urlString.contains(" ")) {
			urlString = urlString.replace(" ", "%20");
			int caret = urlText.getCaretPosition();
			urlText.setText(urlString);
			urlText.setSelection(caret + "%20".length() - 1);
		}
		if (urlString == null || urlString.length() <= 0) {
			return false;
		}

		try {
			new URI(urlString);
		}
		catch (URISyntaxException e) {
			return showError();
		}

		try {
			URL url = new URL(urlString);
			if (url.getHost().isEmpty()) {
				return showError();
			}
		}
		catch (MalformedURLException e) {
			return showError();
		}

		return true;
	}

	private boolean showError() {
		errorTextLabel.setText(AddEditNameUrlDialogMessages.malformedUrl);
		composite.update();
		return false;
	}

	protected void setTitle(String title) {
		this.title = title;
	}
}
