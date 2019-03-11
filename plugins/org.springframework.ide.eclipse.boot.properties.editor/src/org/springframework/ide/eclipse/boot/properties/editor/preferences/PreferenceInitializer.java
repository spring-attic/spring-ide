/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.properties.editor.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.springframework.ide.eclipse.boot.properties.editor.SpringPropertiesEditorPlugin;

import static org.springframework.ide.eclipse.boot.properties.editor.preferences.PreferenceConstants.*;

public class PreferenceInitializer extends AbstractPreferenceInitializer {

	public PreferenceInitializer() {
	}

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = SpringPropertiesEditorPlugin.getDefault().getPreferenceStore();
		store.setDefault(AUTO_CONFIGURE_APT_GRADLE_PREF, AUTO_CONFIGURE_APT_GRADLE_DEFAULT);
		store.setDefault(AUTO_CONFIGURE_APT_M2E_PREF, AUTO_CONFIGURE_APT_M2E_DEFAULT);
	}

}
