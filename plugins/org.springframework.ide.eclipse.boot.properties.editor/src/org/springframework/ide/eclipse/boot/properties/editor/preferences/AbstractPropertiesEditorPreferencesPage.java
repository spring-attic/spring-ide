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

import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.springframework.ide.eclipse.boot.properties.editor.SpringPropertiesEditorPlugin;
import org.springframework.ide.eclipse.boot.properties.editor.reconciling.ProblemSeverity;
import org.springframework.ide.eclipse.boot.properties.editor.reconciling.ProblemType;

/**
 * @author Kris De Volder
 */
public abstract class AbstractPropertiesEditorPreferencesPage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

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

	protected AbstractPropertiesEditorPreferencesPage() {
		super(FieldEditorPreferencePage.GRID);
		ProblemSeverityPreferencesUtil.initializeDefaults();
		setPreferenceStore(SpringPropertiesEditorPlugin.getDefault().getPreferenceStore());
	}

	@Override
	public void init(IWorkbench workbench) {
	}

	@Override
	protected void createFieldEditors() {
		ProblemType[] problemTypes = getProblemTypes();
		Arrays.sort(problemTypes, PROBLEM_TYPE_COMPARATOR);

		for (ProblemType problemType : problemTypes) {
			ComboFieldEditor field = new ComboFieldEditor(
					ProblemSeverityPreferencesUtil.getPreferenceName(problemType),
					problemType.getLabel(),
					SEVERITY_NAMES_AND_VALUES,
					getFieldEditorParent()
			);
			setTooltip(field, problemType.getDescription());
			addField(field);
		}
	}

	protected abstract ProblemType[] getProblemTypes();

	protected void setTooltip(ComboFieldEditor field, String tooltip) {
		field.getLabelControl(getFieldEditorParent()).setToolTipText(tooltip);
	}



}
