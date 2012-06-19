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

/*
 * @author Kaitlin Duck Sherwood
 */

package org.springframework.ide.eclipse.wizard.template.util;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class AddEditExampleProjectDialog extends Dialog {
	AbstractNameUrlPreferenceModel model;

	Text nameText;

	Text urlText;

	String name;

	String urlString;

	private final String explanatoryText;

	public AddEditExampleProjectDialog(Shell parent, AbstractNameUrlPreferenceModel aModel, NameUrlPair nameUrl,
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
		Composite composite = new Composite(parent, SWT.NONE);

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

		Label UrlLabel = new Label(composite, SWT.NONE);
		UrlLabel.setText(NLS.bind("URL:", null));
		UrlLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
		urlText = new Text(composite, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).hint(300, SWT.DEFAULT).applyTo(urlText);
		urlText.setEditable(true);
		if (urlString != null && urlString.length() > 0) {
			urlText.setText(urlString);
		}

		return composite;
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

}
