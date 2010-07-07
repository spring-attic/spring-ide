/*******************************************************************************
 * Copyright (c) 2005, 2010 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.internal.uaa.preferences;

import org.eclipse.jface.preference.PreferencePage;
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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.springframework.ide.eclipse.internal.uaa.UaaPlugin;

/**
 * {@link PreferencePage} to configure the UAA.
 * @author Christian Dupuis
 * @since 2.3.3
 */
public class UaaPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private Button decodeButton;

	private Text decodedUserAgentText;

	private Button disabledButton;

	private Text encodedUserAgentText;

	private Button fullButton;

	private Button limitedButton;

	public void init(IWorkbench workbench) {
	}

	public boolean performOk() {

		return super.performOk();
	}

	protected Control createContents(Composite parent) {

		Composite entryTable = new Composite(parent, SWT.NULL);

		// Create a data that takes up the extra space in the dialog .
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.grabExcessHorizontalSpace = true;
		entryTable.setLayoutData(data);

		GridLayout layout = new GridLayout();
		entryTable.setLayout(layout);

		Label label = new Label(entryTable, SWT.NONE | SWT.WRAP);
		label.setText("Spring User Agent Analysis (UAA) provides anonymous, non-identifiable usage data to help us better understand community usage of Spring technologies. You can use this preferences page to adjust the privacy settings of UAA and view information it has collected.");
		label.setFont(parent.getFont());
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 1;
		gd.widthHint = 300;
		label.setLayoutData(gd);

		Composite colorComposite = new Composite(entryTable, SWT.NONE);
		colorComposite.setLayout(new GridLayout());

		// Create a data that takes up the extra space in the dialog.
		colorComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		Group group = new Group(colorComposite, SWT.NONE);
		layout.marginWidth = 3;
		layout.marginHeight = 3;
		group.setLayout(layout);
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		group.setText("Privacy Level");

		Composite colorComposite2 = new Composite(group, SWT.NONE);
		colorComposite2.setLayout(layout);
		colorComposite2.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		label = new Label(colorComposite2, SWT.NONE | SWT.WRAP);
		label.setText("Select a privacy level to preview the data UAA would transfer.");
		label.setFont(parent.getFont());
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 1;
		gd.widthHint = 270;
		label.setLayoutData(gd);
		
		disabledButton = new Button(colorComposite2, SWT.RADIO);
		disabledButton.setText("Send nothing (opt out)");
		disabledButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				selectionUpdated();
			}
		});
		limitedButton = new Button(colorComposite2, SWT.RADIO);
		limitedButton.setText("Send minimal anonymous information");
		limitedButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				selectionUpdated();
			}
		});
		fullButton = new Button(colorComposite2, SWT.RADIO);
		fullButton.setText("Send enhanced anonymous information (recommended)");
		fullButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				selectionUpdated();
			}
		});

		int level = UaaPlugin.getDefault().getPrivacyLevel();
		if (level == UaaPlugin.FULL_DATA) {
			fullButton.setSelection(true);
		}
		else if (level == UaaPlugin.LIMITED_DATA) {
			limitedButton.setSelection(true);
		}
		else if (level == UaaPlugin.NO_DATA) {
			disabledButton.setSelection(true);
		}

		Label heading = new Label(colorComposite, SWT.NONE | SWT.WRAP);
		heading.setText("Decoded information:");

		decodedUserAgentText = new Text(colorComposite, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.WRAP);
		decodedUserAgentText.setText(UaaPlugin.getDefault().getUserAgentContents(
				UaaPlugin.getDefault().getUserAgentHeader()));
		data = new GridData(GridData.FILL_BOTH);
		data.heightHint = 150;
		data.widthHint = 500;
		decodedUserAgentText.setLayoutData(data);
//		decodedUserAgentText.setFont(JFaceResources.getTextFont());
		decodedUserAgentText.setEditable(false);

		heading = new Label(colorComposite, SWT.NONE | SWT.WRAP);
		heading.setText("Encoded information:");

		encodedUserAgentText = new Text(colorComposite, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.WRAP);
		encodedUserAgentText.setText(UaaPlugin.getDefault().getUserAgentHeader());
		data = new GridData(GridData.FILL_BOTH);
		data.heightHint = 50;
		data.widthHint = 500;
		encodedUserAgentText.setLayoutData(data);
//		encodedUserAgentText.setFont(JFaceResources.getTextFont());
		encodedUserAgentText.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				decodeButton.setEnabled(true);
			}
		});

		decodeButton = new Button(colorComposite, SWT.NONE);
		decodeButton.setText("Decode");
		gd = new GridData(GridData.HORIZONTAL_ALIGN_END);
		decodeButton.setLayoutData(gd);
		decodeButton.setEnabled(false);
		decodeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleDecode();
			}
		});

		label = new Label(entryTable, SWT.NONE | SWT.WRAP);
		label.setText("Note: the User Agent Analysis feature will record certain usage events which you can review in the 'Encoded information' text box. The collected information will be presented via a HTTP User-Agent header when accessing content on Spring-related domains such as springide.org, springsource.org and springsource.com.");
		label.setFont(parent.getFont());
		gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 1;
		gd.widthHint = 300;
		label.setLayoutData(gd);

		return entryTable;
	}

	protected void handleDecode() {
		try {
			decodedUserAgentText.setText(UaaPlugin.getDefault().getUserAgentContents(encodedUserAgentText.getText()));
		}
		catch (Exception e) {
			decodedUserAgentText.setText("could not decode uaa string");
		}
	}

	protected void performDefaults() {
		UaaPlugin.getDefault().setPrivacyLevel(UaaPlugin.DEFAULT_PRIVACY_LEVEL);
		limitedButton.setSelection(true);
		disabledButton.setSelection(false);
		fullButton.setSelection(false);
	}

	protected void selectionUpdated() {
		if (disabledButton.getSelection()) {
			UaaPlugin.getDefault().setPrivacyLevel(UaaPlugin.NO_DATA);
		}
		else if (limitedButton.getSelection()) {
			UaaPlugin.getDefault().setPrivacyLevel(UaaPlugin.LIMITED_DATA);
		}
		else if (fullButton.getSelection()) {
			UaaPlugin.getDefault().setPrivacyLevel(UaaPlugin.FULL_DATA);
		}
		decodedUserAgentText.setText(UaaPlugin.getDefault().getUserAgentContents(
				UaaPlugin.getDefault().getUserAgentHeader()));
		encodedUserAgentText.setText(UaaPlugin.getDefault().getUserAgentHeader());
		decodeButton.setEnabled(false);
	}

}