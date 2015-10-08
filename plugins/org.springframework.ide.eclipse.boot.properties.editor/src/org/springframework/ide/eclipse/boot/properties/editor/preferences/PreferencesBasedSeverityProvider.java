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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.preference.IPreferenceStore;
import org.springframework.ide.eclipse.boot.properties.editor.reconciling.ProblemSeverity;
import org.springframework.ide.eclipse.boot.properties.editor.reconciling.ProblemType;
import org.springframework.ide.eclipse.boot.properties.editor.reconciling.SeverityProvider;
import org.springframework.ide.eclipse.boot.properties.editor.reconciling.SpringPropertyProblem;

/**
 * Implementation of {@link SeverityProvider} that determines the severity of a problem
 * by consulting user's preferences in a preferences store.
 *
 * @author Kris De Volder
 */
public class PreferencesBasedSeverityProvider implements SeverityProvider {

	private EditorType editorType;
	private IPreferenceStore projectPrefs;
	private IPreferenceStore workspacePrefs;

	private Map<ProblemType, ProblemSeverity> cache = null;

	public PreferencesBasedSeverityProvider(IPreferenceStore projectPrefs, IPreferenceStore workspacePrefs, EditorType editorType) {
		this.projectPrefs = projectPrefs;
		this.workspacePrefs = workspacePrefs;
		this.editorType = editorType;
	}

	@Override
	public ProblemSeverity getSeverity(SpringPropertyProblem problem) {
		return getSeverity(problem.getType());
	}

	private synchronized ProblemSeverity getSeverity(ProblemType problemType) {
		if (cache==null) {
			cache = new HashMap<ProblemType, ProblemSeverity>();
		}
		ProblemSeverity existing = cache.get(problemType);
		if (existing==null) {
			cache.put(problemType, existing = ProblemSeverityPreferencesUtil.getSeverity(getPrefs(), problemType));
		}
		return existing;
	}

	protected IPreferenceStore getPrefs() {
		if (useProjectPreferences()) {
			return projectPrefs;
		} else {
			return workspacePrefs;
		}
	}

	private boolean useProjectPreferences() {
		if (projectPrefs!=null) {
			return ProblemSeverityPreferencesUtil.projectPreferencesEnabled(projectPrefs, editorType);
		}
		return false;
	}

	@Override
	public synchronized void startReconciling() {
		cache = null;
	}
}
