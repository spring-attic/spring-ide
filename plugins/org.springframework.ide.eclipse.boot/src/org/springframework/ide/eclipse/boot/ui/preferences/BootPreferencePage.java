/*******************************************************************************
 *  Copyright (c) 2015 Pivotal, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.ui.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.springframework.ide.eclipse.boot.core.BootActivator;
import org.springframework.ide.eclipse.boot.core.BootPreferences;

/**
 * @author Kris De Volder
 */
public class BootPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(BootActivator.getDefault().getPreferenceStore());
	}

	@Override
	protected void createFieldEditors() {
		Composite parent = getFieldEditorParent();

		StringFieldEditor projectExclude = new RegExpFieldEditor(BootPreferences.PREF_BOOT_PROJECT_EXCLUDE, "Exclude Projects", parent);
		setTooltip(parent, projectExclude, "Any project who's name matches this regexp will NOT be treated as a Spring Boot App");
		addField(projectExclude);
	}


	private void setTooltip(Composite parent, StringFieldEditor fe, String tooltip) {
		fe.getLabelControl(parent).setToolTipText(tooltip);
		fe.getTextControl(parent).setToolTipText(tooltip);
	}

}
