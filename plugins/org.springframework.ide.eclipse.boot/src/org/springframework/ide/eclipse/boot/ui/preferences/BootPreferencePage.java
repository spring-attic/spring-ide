/*******************************************************************************
 *  Copyright (c) 2015, 2016 Pivotal, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.ui.preferences;

import static org.springframework.ide.eclipse.boot.core.BootPreferences.DEFAULT_PREF_IGNORE_SILENT_EXIT;
import static org.springframework.ide.eclipse.boot.core.BootPreferences.PREF_BOOT_PROJECT_EXCLUDE;
import static org.springframework.ide.eclipse.boot.core.BootPreferences.PREF_IGNORE_SILENT_EXIT;

import org.eclipse.debug.internal.ui.preferences.BooleanFieldEditor2;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.springframework.ide.eclipse.boot.core.BootActivator;

/**
 * @author Kris De Volder
 */
@SuppressWarnings("restriction")
public class BootPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	@Override
	public void init(IWorkbench workbench) {
		IPreferenceStore store = BootActivator.getDefault().getPreferenceStore();
		store.setDefault(PREF_IGNORE_SILENT_EXIT, DEFAULT_PREF_IGNORE_SILENT_EXIT);
		setPreferenceStore(BootActivator.getDefault().getPreferenceStore());
	}

	@Override
	protected void createFieldEditors() {
		Composite parent = getFieldEditorParent();

		StringFieldEditor projectExclude = new RegExpFieldEditor(PREF_BOOT_PROJECT_EXCLUDE, "Exclude Projects", parent);
		setTooltip(parent, projectExclude, "Any project who's name matches this regexp will NOT be treated as a Spring Boot App");
		addField(projectExclude);


		BooleanFieldEditor2 ignoreSilentExit = new BooleanFieldEditor2(PREF_IGNORE_SILENT_EXIT, "Ignore Silent Exit", SWT.CHECK, parent);
		setTooltip(parent, ignoreSilentExit, "When debugging a Boot App, do not suspend when 'SilentExitException' is raised. "
				+ "(This exception is raised by spring-boot-devtools as part of its normal operation)");
		addField(ignoreSilentExit);
	}

	private void setTooltip(Composite parent, StringFieldEditor fe, String tooltip) {
		fe.getLabelControl(parent).setToolTipText(tooltip);
		fe.getTextControl(parent).setToolTipText(tooltip);
	}

	private void setTooltip(Composite parent, BooleanFieldEditor2 fe, String tooltip) {
		fe.getChangeControl(parent).setToolTipText(tooltip);
	}

}
