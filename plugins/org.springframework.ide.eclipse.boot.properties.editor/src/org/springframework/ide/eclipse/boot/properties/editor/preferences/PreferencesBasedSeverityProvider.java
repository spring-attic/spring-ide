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
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.springframework.ide.eclipse.boot.properties.editor.SpringPropertiesEditorPlugin;
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
public class PreferencesBasedSeverityProvider implements SeverityProvider, IPropertyChangeListener {

	private IPreferenceStore prefs;

	private Map<ProblemType, ProblemSeverity> cache = null;

	public PreferencesBasedSeverityProvider(IPreferenceStore prefs) {
		this.prefs = prefs;
		this.prefs.addPropertyChangeListener(this);
	}

	public PreferencesBasedSeverityProvider() {
		this(SpringPropertiesEditorPlugin.getDefault().getPreferenceStore());
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
			cache.put(problemType, existing = ProblemSeverityPreferencesUtil.getSeverity(prefs, problemType));
		}
		return existing;
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		if (event.getProperty().startsWith(ProblemSeverityPreferencesUtil.PREFERENCE_PREFIX)) {
			clearCache();
		}
	}

	private synchronized void clearCache() {
		cache = null;
	}

}
