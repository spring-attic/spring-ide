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

import static org.springframework.ide.eclipse.boot.core.BootPreferences.DEFAULT_PREF_IGNORE_SILENT_EXIT;
import static org.springframework.ide.eclipse.boot.core.BootPreferences.PREF_IGNORE_SILENT_EXIT;
import static org.springframework.ide.eclipse.boot.core.BootPreferences.PREF_INITIALIZR_URL;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.springframework.ide.eclipse.boot.core.BootActivator;
import org.springsource.ide.eclipse.commons.core.preferences.StsProperties;

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
		
		/*
		 * Note that line below cannot be moved to BootPreferencePage static
		 * init method because the class has BooleanFieldEditor2 import that in
		 * turn activate eclipse preferences debug plugin which throws exception
		 * because workbench may not be started in the case of a unit test
		 */
		store.setDefault(PREF_IGNORE_SILENT_EXIT, DEFAULT_PREF_IGNORE_SILENT_EXIT);
		
		store.setDefault(PREF_INITIALIZR_URL, StsProperties.getInstance().get("spring.initializr.json.url"));
	}
		

}
