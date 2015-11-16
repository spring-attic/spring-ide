/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.properties.editor.preferences;

import static org.springframework.ide.eclipse.boot.properties.editor.preferences.PreferenceConstants.*;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.springframework.ide.eclipse.boot.properties.editor.SpringPropertiesEditorPlugin;


public class AnotationProcessingPreferencesPage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public AnotationProcessingPreferencesPage() {
		super(FieldEditorPreferencePage.GRID);
		setPreferenceStore(SpringPropertiesEditorPlugin.getDefault().getPreferenceStore());
	}

	@Override
	protected void createFieldEditors() {
		BooleanFieldEditor fEnableM2EAptSupport = new BooleanFieldEditor(AUTO_CONFIGURE_APT_M2E_PREF, "Auto configure JDT APT for maven projects", getFieldEditorParent());
		setTooltip(fEnableM2EAptSupport,
				"Automatically enable and configure JDT APT for m2e projects that have spring-boot-configuration-processor on their classpath");
		addField(fEnableM2EAptSupport);
		BooleanFieldEditor fEnableGradleAptSupport = new BooleanFieldEditor(AUTO_CONFIGURE_APT_GRADLE_PREF, "Auto configure JDT APT for gradle projects (only works with STS tooling, not Buildship)", getFieldEditorParent());
		setTooltip(fEnableGradleAptSupport,
				"Automatically enable and configure JDT APT for Gradle projects that have spring-boot-configuration-processor on their classpath");
		addField(fEnableGradleAptSupport);
	}

	private void setTooltip(BooleanFieldEditor field, String tooltip) {
		field.getDescriptionControl(getFieldEditorParent()).setToolTipText(tooltip);
	}

	@Override
	public void init(IWorkbench workbench) {
	}


}
