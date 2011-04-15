/*******************************************************************************
 * Copyright (c) 2010, 2011 Spring IDE Developers
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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.springframework.ide.eclipse.uaa.IUaa;
import org.springframework.ide.eclipse.uaa.UaaPlugin;
import org.springframework.ide.eclipse.uaa.UaaUtils;

/**
 * {@link PreferencePage} to configure the UAA.
 * @author Christian Dupuis
 * @since 2.5.2
 */
public class UaaPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private Text decodedUserAgentText;

	private Button disabledButton;

	private Button fullButton;

	private Button limitedButton;

	private IUaa uaa = UaaPlugin.getUAA();

	private Button declineButton;

	private Button undecidedButton;

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
		label.setText("Select a privacy level:");
		label.setFont(parent.getFont());
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 1;
		gd.widthHint = 270;
		label.setLayoutData(gd);

		fullButton = new Button(colorComposite2, SWT.RADIO);
		fullButton.setText("Send enhanced anonymous information (accept Terms of Use)");
		fullButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				selectionUpdated();
			}
		});
		limitedButton = new Button(colorComposite2, SWT.RADIO);
		limitedButton.setText("Send minimal anonymous information (accept Terms of Use)");
		limitedButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				selectionUpdated();
			}
		});
		disabledButton = new Button(colorComposite2, SWT.RADIO);
		disabledButton.setText("Send no usage data (accept Terms of Use)");
		disabledButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				selectionUpdated();
			}
		});
		declineButton = new Button(colorComposite2, SWT.RADIO);
		declineButton.setText("Disable UAA (reject Terms of Use)");
		declineButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				selectionUpdated();
			}
		});
		if (uaa.getPrivacyLevel() == IUaa.UNDECIDED_TOU) {
			undecidedButton = new Button(colorComposite2, SWT.RADIO);
			undecidedButton.setText("Undecided on Terms of Use");
			undecidedButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					selectionUpdated();
				}
			});
			undecidedButton.setSelection(true);
		}
		int level = uaa.getPrivacyLevel();
		if (level == IUaa.FULL_DATA) {
			fullButton.setSelection(true);
		}
		else if (level == IUaa.LIMITED_DATA) {
			limitedButton.setSelection(true);
		}
		else if (level == IUaa.NO_DATA) {
			disabledButton.setSelection(true);
		}
		else if (level == IUaa.DECLINE_TOU) {
			declineButton.setSelection(true);
		}

		Label heading = new Label(colorComposite, SWT.NONE | SWT.WRAP);
		heading.setText("Usage information:");

		decodedUserAgentText = new Text(colorComposite, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		decodedUserAgentText.setText(uaa.getReadablePayload());
		data = new GridData(GridData.FILL_BOTH);
		data.heightHint = 150;
		data.widthHint = 550;
		decodedUserAgentText.setLayoutData(data);
		// decodedUserAgentText.setFont(JFaceResources.getTextFont());
		decodedUserAgentText.setEditable(false);

		Link link = new Link(entryTable, SWT.NONE | SWT.WRAP);
		link.setText("Note: the User Agent Analysis feature will record certain usage events which you can review in the 'Usage information' text box. The collected information will be presented to VMware domains such as springide.org, springsource.org and springsource.com.\n\nMore information can be obtained from the Spring UAA <a href=\"tou\">Terms of Use</a> and <a href=\"faq\">FAQ</a>.");
		link.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if ("tou".equals(e.text)) {
					UaaUtils.openUrl("http://www.springsource.org/uaa/terms_of_use");
				}
				else if ("faq".equals(e.text)) {
					UaaUtils.openUrl("http://www.springsource.org/uaa/faq");
				}
			}
		});
		link.setFont(parent.getFont());
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 1;
		gd.widthHint = 300;
		link.setLayoutData(gd);

		return entryTable;
	}

	protected void performDefaults() {
		uaa.setPrivacyLevel(IUaa.DEFAULT_PRIVACY_LEVEL);
		limitedButton.setSelection(false);
		disabledButton.setSelection(false);
		fullButton.setSelection(true);
		declineButton.setSelection(false);
	}

	protected void selectionUpdated() {
		if (disabledButton.getSelection()) {
			uaa.setPrivacyLevel(IUaa.NO_DATA);
		}
		else if (limitedButton.getSelection()) {
			uaa.setPrivacyLevel(IUaa.LIMITED_DATA);
		}
		else if (fullButton.getSelection()) {
			uaa.setPrivacyLevel(IUaa.FULL_DATA);
		}
		else if (declineButton.getSelection()) {
			uaa.setPrivacyLevel(IUaa.DECLINE_TOU);
		}
		if (undecidedButton != null) {
			undecidedButton.setEnabled(false);
		}
		
		decodedUserAgentText.setText(uaa.getReadablePayload());
	}

}