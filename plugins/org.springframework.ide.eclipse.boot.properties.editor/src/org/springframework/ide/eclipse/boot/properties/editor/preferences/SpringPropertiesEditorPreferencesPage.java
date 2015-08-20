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

import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.osgi.service.prefs.BackingStoreException;
import org.springframework.ide.eclipse.boot.core.BootActivator;
import org.springframework.ide.eclipse.boot.properties.editor.SpringPropertiesEditorPlugin;
import org.springframework.ide.eclipse.boot.properties.editor.reconciling.ProblemSeverity;
import org.springframework.ide.eclipse.boot.properties.editor.reconciling.ProblemType;

/**
 * @author Kris De Volder
 */
public class SpringPropertiesEditorPreferencesPage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	private static final Comparator<ProblemType> PROBLEM_TYPE_COMPARATOR = new Comparator<ProblemType>() {
		public int compare(ProblemType o1, ProblemType o2) {
			return o1.getLabel().compareTo(o2.getLabel());
		}
	};
	private static final String[][] SEVERITY_NAMES_AND_VALUES = {
			{"Error", ProblemSeverity.ERROR.toString()},
			{"Warning", ProblemSeverity.WARNING.toString()},
			{"Ignore", ProblemSeverity.IGNORE.toString()}
	};

	public SpringPropertiesEditorPreferencesPage() {
		super(FieldEditorPreferencePage.GRID);
		initializeDefaults();
		setPreferenceStore(SpringPropertiesEditorPlugin.getDefault().getPreferenceStore());
	}

	private void initializeDefaults() {
		IEclipsePreferences defaults = SpringPropertiesEditorPlugin.getDefault().getDefaultPreferences();
		for (ProblemType problemType : getProblemTypes()) {
			defaults.put(getPreferenceName(problemType), problemType.getDefaultSeverity().toString());
		}
		try {
			defaults.flush();
		} catch (BackingStoreException e) {
			BootActivator.log(e);
		}
	}

	@Override
	protected void createFieldEditors() {
		ProblemType[] problemTypes = getProblemTypes();
		Arrays.sort(problemTypes, PROBLEM_TYPE_COMPARATOR);

		for (ProblemType problemType : problemTypes) {
			ComboFieldEditor field = new ComboFieldEditor(
					getPreferenceName(problemType),
					problemType.getLabel(),
					SEVERITY_NAMES_AND_VALUES,
					getFieldEditorParent()
			);
			setTooltip(field, problemType.getDescription());
			addField(field);
		}
	}

	protected String getPreferenceName(ProblemType problemType) {
		String prefix = getPreferencePrefix();
		return prefix+problemType.toString();
	}

	protected String getPreferencePrefix() {
		return "spring.properties.editor.problem.";
	}

	protected ProblemType[] getProblemTypes() {
		return ProblemType.FOR_PROPERTIES;
	}

	private void setTooltip(ComboFieldEditor field, String tooltip) {
		field.getLabelControl(getFieldEditorParent()).setToolTipText(tooltip);
	}

	@Override
	public void init(IWorkbench workbench) {
	}

}
