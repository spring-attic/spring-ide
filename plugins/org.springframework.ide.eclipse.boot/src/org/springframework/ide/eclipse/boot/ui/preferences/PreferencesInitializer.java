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

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.springframework.ide.eclipse.boot.core.BootActivator;

/**
 * Initializer of default values for the Spring Boot support
 * 
 * @author Alex Boyko
 *
 */
public class PreferencesInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = BootActivator.getDefault().getPreferenceStore();
		BootPreferencePage.initDefaults(store);
		InitializrPreferencePage.initDefaults(store);
	}

}
