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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.ControlEnableState;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.springframework.ide.eclipse.boot.properties.editor.SpringPropertiesEditorPlugin;
import org.springframework.ide.eclipse.boot.properties.editor.reconciling.ProblemSeverity;
import org.springframework.ide.eclipse.boot.properties.editor.reconciling.ProblemType;
import org.springframework.ide.eclipse.boot.properties.editor.util.CheckboxWidget;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.core.UIValueListener;

/**
 * @author Kris De Volder
 */
public abstract class AbstractPropertiesEditorPreferencesPage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage, IWorkbenchPropertyPage {

	/**
	 * Project for a project propertypage, or null for a workspace preference page.
	 */
	private IProject project;

	/**
	 * The state of the 'enable project specific settings' checkbox.
	 */
	private LiveVariable<Boolean> enablePreferences = new LiveVariable<Boolean>(true);


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
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(SpringPropertiesEditorPlugin.getDefault().getPreferenceStore());
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

	@Override
	protected Control createContents(Composite parent) {
		if (isProjectPropertyPage()) {
			return createProjectPageContent(parent);
		} else {
			return super.createContents(parent);
		}
	}

	private Control createProjectPageContent(Composite parent) {
		Composite page = new Composite(parent, SWT.NONE);
		page.setLayout(new GridLayout());

		CheckboxWidget enablePreferencesCheckbox = new CheckboxWidget(page, enablePreferences);
		enablePreferencesCheckbox.setText("Enable project-specific settings");

		final Control preferencesControl = super.createContents(page);

		enablePreferences.addListener(new UIValueListener<Boolean>() {
			private ControlEnableState fBlockEnableState;

			protected void enablePreferenceContent(boolean enable) {
				if (enable) {
					if (fBlockEnableState != null) {
						fBlockEnableState.restore();
						fBlockEnableState = null;
					}
				}
				else {
					if (fBlockEnableState == null) {
						fBlockEnableState = ControlEnableState.disable(preferencesControl);
					}
				}
			}
			@Override
			protected void uiGotValue(LiveExpression<Boolean> exp, Boolean enable) {
				if (enable!=null) {
					enablePreferenceContent(enable);
				}
			}
		});

		return page;
	}


	private boolean isProjectPropertyPage() {
		return project!=null;
	}

	protected abstract ProblemType[] getProblemTypes();

	protected void setTooltip(ComboFieldEditor field, String tooltip) {
		field.getLabelControl(getFieldEditorParent()).setToolTipText(tooltip);
	}

	@Override
	public IAdaptable getElement() {
		return project;
	}

	@Override
	public void setElement(IAdaptable element) {
		project = element.getAdapter(IProject.class);
		if (project!=null) {
			setPreferenceStore(new ScopedPreferenceStore(new ProjectScope(project), SpringPropertiesEditorPlugin.PLUGIN_ID));
			enablePreferences.setValue(getPreferenceStore().getBoolean(getEnableProjectPreferencesKey()));
		}
	}

	@Override
	public boolean performOk() {
		if (isProjectPropertyPage()) {
			getPreferenceStore().setValue(getEnableProjectPreferencesKey(), enablePreferences.getValue());
		}
		return super.performOk();
	}

	@Override
	protected void performDefaults() {
		if (isProjectPropertyPage()) {
			enablePreferences.setValue(getPreferenceStore().getDefaultBoolean(getEnableProjectPreferencesKey()));
		}
		super.performDefaults();
	}

	/**
	 * Determines the property-key that is used to strore whether project-specific preferences are enabled for
	 * the properties on this page.
	 */
	protected abstract String getEnableProjectPreferencesKey();


}
