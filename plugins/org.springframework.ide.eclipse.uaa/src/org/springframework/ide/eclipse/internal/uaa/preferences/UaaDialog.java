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

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.springframework.ide.eclipse.uaa.UaaPlugin;
import org.springframework.ide.eclipse.uaa.UaaUtils;

/**
 * @author Christian Dupuis
 * @since 2.5.2
 */
public class UaaDialog extends TitleAreaDialog {

	private static final String DIALOG_TITLE = "User Agent Analysis";

	private static final String TITLE = "Spring User Agent Analysis (UAA)";

	private static final String MESSAGE = "%name% wants to download resources from VMware domains to improve your experience. We include anonymous usage information as part of these downloads.\n"
			+ "\n"
			+ "The Spring team gathers anonymous usage information to improve your Spring experience, not for marketing purposes. We also use this information to help guide our roadmap, prioritizing the features most valued by the community and enabling us to optimize the compatibility of technologies frequently used together.\n"
			+ "\n"
			+ "Please see the Spring User Agent Analysis (UAA) <a href=\"tou\">Terms of Use</a> for more information on what information is collected and how such information is used. There is also an <a href=\"faq\">FAQ</a> for your convenience.\n"
			+ "\n"
			+ "To consent to the Terms of Use, please click 'Accept'. If you do not click 'Accept' to indicate your consent anonymous data collection will remain disabled.\n"
			+ "\n"
			+ "You can review usage data captured by UAA in the <a href=\"prefs\">User Agent Analysis</a> preferences.";
	
	private Image image = UaaPlugin.imageDescriptorFromPlugin(UaaPlugin.PLUGIN_ID, "icons/full/wizban/uaa_wiz.png").createImage();
	
	public static final String PLATFORM_NAME = Platform.getBundle("com.springsource.sts") != null ? "SpringSource Tool Suite"
			: "Spring IDE";

	public static UaaDialog createDialog(Shell shell) {
		return new UaaDialog(shell);
	}

	private UaaDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	protected Control createContents(Composite parent) {
		getShell().setText(DIALOG_TITLE);

		Control control = super.createContents(parent);

		getButton(IDialogConstants.OK_ID).setText("Accept");
		getButton(IDialogConstants.CANCEL_ID).setText("Reject");
		getButton(IDialogConstants.CANCEL_ID).addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setReturnCode(1000);
			}
		});
		
		setTitle(TITLE);
		setMessage("Download consent required", IMessageProvider.INFORMATION);
		setTitleImage(image);
		setBlockOnOpen(true);
		setDialogHelpAvailable(false);
		setHelpAvailable(false);

		applyDialogFont(control);
		return control;
	}
	
	@Override
	public boolean close() {
		if (image != null) {
			image.dispose();
		}
		return super.close();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite parent2 = (Composite) super.createDialogArea(parent);

		Composite composite = new Composite(parent2, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.BEGINNING).applyTo(composite);

		Link link = new Link(composite, SWT.NONE | SWT.WRAP);
		link.setText(MESSAGE.replace("%name%", PLATFORM_NAME));
		link.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if ("tou".equals(e.text)) {
					UaaUtils.openUrl("http://www.springsource.org/uaa/terms_of_use");
				}
				else if ("faq".equals(e.text)) {
					UaaUtils.openUrl("http://www.springsource.org/uaa/faq");
				}
				else if ("prefs".equals(e.text)) {
					Display.getDefault().asyncExec(new Runnable() {

						public void run() {
							close();
							PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(null,
									"org.springframework.ide.eclipse.uaa.preferencePage", null, null);
							dialog.open();
							setReturnCode(CANCEL);
						}
					});
				}
			}
		});
		link.setFont(parent.getFont());

		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.hint(convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH), SWT.DEFAULT)
				.grab(true, false).applyTo(link);

		composite.pack();
		return parent;
	}
}
