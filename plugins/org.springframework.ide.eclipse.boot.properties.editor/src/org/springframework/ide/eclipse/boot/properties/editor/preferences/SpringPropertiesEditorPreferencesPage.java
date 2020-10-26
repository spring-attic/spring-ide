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

import java.util.List;

import org.eclipse.jface.preference.IPreferenceStore;
import org.springframework.ide.eclipse.boot.properties.editor.SpringPropertiesEditorPlugin;
import org.springframework.ide.eclipse.boot.properties.editor.reconciling.SpringPropertiesProblemType;
import org.springframework.ide.eclipse.editor.support.preferences.AbstractProblemSeverityPreferencesPage;
import org.springframework.ide.eclipse.editor.support.preferences.EditorType;
import org.springframework.ide.eclipse.editor.support.preferences.ProblemSeverityPreferencesUtil;
import org.springframework.ide.eclipse.editor.support.reconcile.ProblemType;

import com.google.common.collect.ImmutableList;

/**
 * @author Kris De Volder
 */
public class SpringPropertiesEditorPreferencesPage extends AbstractProblemSeverityPreferencesPage {

	public SpringPropertiesEditorPreferencesPage() {
		super(PreferenceConstants.severityUtils);
	}

	protected List<ProblemType> getProblemTypes() {
		return ImmutableList.copyOf(SpringPropertiesProblemType.FOR_PROPERTIES);
	}

	@Override
	protected String getEnableProjectPreferencesKey() {
		//TODO: replace PreferenceConstants.severityUtils with super.util
		return PreferenceConstants.severityUtils.ENABLE_PROJECT_PREFERENCES(EditorType.PROP);
	}

	@Override
	protected String getPluginId() {
		return SpringPropertiesEditorPlugin.PLUGIN_ID;
	}

}
