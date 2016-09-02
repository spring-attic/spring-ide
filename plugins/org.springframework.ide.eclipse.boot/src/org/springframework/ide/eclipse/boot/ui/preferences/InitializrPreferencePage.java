/*******************************************************************************
 *  Copyright (c) 2016 Pivotal, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.ui.preferences;

import static org.springframework.ide.eclipse.boot.core.BootPreferences.PREF_INITIALIZR_URL;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.springframework.ide.eclipse.boot.core.BootActivator;
import org.springsource.ide.eclipse.commons.core.preferences.StsProperties;

/**
 * Preferences page for Spring IO Initializr IDE support
 * 
 * @author Alex Boyko
 *
 */
public class InitializrPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	
	private static final String LABEL_INITIALIZR_URL = "Initializr URL";
	private static final String TOOLTIP_INITIALIZR_URL = "Spring Initializr server URL";
	private static final String MSG_INVALID_URL_FORMAT = "Invalid URL format";

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(BootActivator.getDefault().getPreferenceStore());
	}

	@Override
	protected void createFieldEditors() {
		Composite parent = getFieldEditorParent();

		StringFieldEditor initializrUrl = new StringFieldEditor(PREF_INITIALIZR_URL, LABEL_INITIALIZR_URL, parent) {

			@Override
			protected boolean checkState() {
				Text text = getTextControl();
				if (text == null) {
					return false;
				}
				try {
					new URL(text.getText());
					clearErrorMessage();
					return true;
				} catch (MalformedURLException e) {
					setErrorMessage(MSG_INVALID_URL_FORMAT);
					showErrorMessage();
					return false;
				}
			}
			
		};
		
		initializrUrl.getLabelControl(parent).setToolTipText(TOOLTIP_INITIALIZR_URL);
		initializrUrl.getTextControl(parent).setToolTipText(TOOLTIP_INITIALIZR_URL);
		addField(initializrUrl);

	}

	static void initDefaults(IPreferenceStore store) {
		store.setDefault(PREF_INITIALIZR_URL, StsProperties.getInstance().get("spring.initializr.json.url"));
	}
	
}
