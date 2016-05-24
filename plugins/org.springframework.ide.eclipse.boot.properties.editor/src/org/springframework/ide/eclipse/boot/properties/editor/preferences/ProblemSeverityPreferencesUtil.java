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

import java.io.IOException;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.jface.preference.IPreferenceStore;
import org.osgi.service.prefs.BackingStoreException;
import org.springframework.ide.eclipse.boot.core.BootActivator;
import org.springframework.ide.eclipse.boot.properties.editor.SpringPropertiesEditorPlugin;
import org.springframework.ide.eclipse.boot.properties.editor.reconciling.SpringPropertiesProblemType;
import org.springframework.ide.eclipse.editor.support.reconcile.ProblemSeverity;
import org.springframework.ide.eclipse.editor.support.reconcile.ProblemType;
import org.springframework.ide.eclipse.editor.support.util.StringUtil;

/**
 * @author Kris De Volder
 */
public class ProblemSeverityPreferencesUtil {

	public static final String PREFERENCE_PREFIX = "spring.properties.editor.problem.";

	public static final String ENABLE_PROJECT_PREFERENCES(EditorType et) {
		return PREFERENCE_PREFIX+et+".project.prefs.enabled";
	}

	/**
	 * Ensures that default preference  values for all problem types are entered into the
	 * DefaultScope. Note that this mainly important for the preferences UI which displays the
	 * defaults from the peferences store. Other consumers of the preference will
	 * use ProblemType.getDefaultSeverity() when they can not find a default value in the store.
	 */
	public static void initializeDefaults() {
		IEclipsePreferences defaults = SpringPropertiesEditorPlugin.getDefault().getDefaultPreferences();
		for (SpringPropertiesProblemType problemType : SpringPropertiesProblemType.values()) {
			defaults.put(getPreferenceName(problemType), problemType.getDefaultSeverity().toString());
		}
		try {
			defaults.flush();
		} catch (BackingStoreException e) {
			BootActivator.log(e);
		}
	}

	public static String getPreferenceName(ProblemType problemType) {
		return PREFERENCE_PREFIX+problemType.toString();
	}

	public static ProblemSeverity getSeverity(IPreferenceStore prefs, ProblemType problemType) {
		String value = prefs.getString(getPreferenceName(problemType));
		try {
			if (StringUtil.hasText(value)) {
				return ProblemSeverity.valueOf(value);
			}
		} catch (Exception e) {
			//corrupted data in prefs store? Or maybe its just stale data from a different version
			// of STS. Ignore silently and use default value
		}
		return problemType.getDefaultSeverity();
	}

	public static void setSeverity(IPreferenceStore prefs, ProblemType problemType, ProblemSeverity severity) {
		prefs.setValue(getPreferenceName(problemType), severity.toString());
	}

	public static boolean projectPreferencesEnabled(IPreferenceStore projectPrefs, EditorType et) {
		return projectPrefs.getBoolean(ENABLE_PROJECT_PREFERENCES(et));
	}

	public static void enableProjectPrefs(IPreferenceStore projectPrefs, EditorType et, boolean enable) {
		projectPrefs.setValue(ENABLE_PROJECT_PREFERENCES(et), enable);
	}

	public static void save(IPreferenceStore preferences) {
		if (preferences.needsSaving() && preferences instanceof IPersistentPreferenceStore) {
			try {
				((IPersistentPreferenceStore) preferences).save();
			} catch (IOException e) {
				BootActivator.log(e);
			}
		}
	}

}
