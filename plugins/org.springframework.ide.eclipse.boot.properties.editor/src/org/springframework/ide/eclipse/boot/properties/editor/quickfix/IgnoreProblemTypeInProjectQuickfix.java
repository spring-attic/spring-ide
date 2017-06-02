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
package org.springframework.ide.eclipse.boot.properties.editor.quickfix;

import static org.springframework.ide.eclipse.editor.support.preferences.ProblemSeverityPreferencesUtil.*;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.springframework.ide.eclipse.boot.properties.editor.reconciling.SpringPropertiesProblemType;
import org.springframework.ide.eclipse.editor.support.preferences.EditorType;
import org.springframework.ide.eclipse.editor.support.preferences.ProblemSeverityPreferencesUtil;
import org.springframework.ide.eclipse.editor.support.reconcile.ProblemSeverity;
import org.springframework.ide.eclipse.editor.support.reconcile.QuickfixContext;

/**
 * Quickfix proposal to change a particular problem type's severity to 'Ignore' in
 * the project.
 *
 * @author Kris De Volder
 */
@SuppressWarnings("restriction")
public class IgnoreProblemTypeInProjectQuickfix implements ICompletionProposal {

	private SpringPropertiesProblemType problemType;
	private IPreferenceStore workspacePrefs;
	private IPreferenceStore projectPrefs;
	private IProject project;

	public IgnoreProblemTypeInProjectQuickfix(QuickfixContext context, SpringPropertiesProblemType type) {
		this.project = context.getProject();
		this.workspacePrefs = context.getWorkspacePreferences();
		this.projectPrefs = context.getProjectPreferences();
		this.problemType = type;
	}

	@Override
	public void apply(IDocument document) {
		EditorType et = problemType.getEditorType();
		if (!projectPreferencesEnabled(projectPrefs, et)) {
			//Tricky: if project preferences are not yet enabled, enabling them may 'revert' some
			//globally changed preferences back to their default values.
			//Avoid that confusing behavior by copying the preferences that would change from workspace to project level.
			for (SpringPropertiesProblemType problemType : SpringPropertiesProblemType.FOR(et)) {
				ProblemSeverity currentEffectiveValue = getSeverity(workspacePrefs, problemType);
				ProblemSeverity currentProjectValue = getSeverity(projectPrefs, problemType);
				if (!currentEffectiveValue.equals(currentProjectValue)) {
					ProblemSeverityPreferencesUtil.setSeverity(projectPrefs, problemType, currentEffectiveValue);
				}
			}
			enableProjectPrefs(projectPrefs, et, true);
		}
		setSeverity(projectPrefs, problemType, ProblemSeverity.IGNORE);
		save(projectPrefs);
	}

	@Override
	public Point getSelection(IDocument document) {
		return null;
	}

	@Override
	public String getAdditionalProposalInfo() {
		return "Sets problem severity preference for problems of type '"+problemType.getLabel()+"' to 'Ignore' in project '"+project.getName()+"'.";
	}

	@Override
	public String getDisplayString() {
		return "Ignore '"+problemType.getLabel()+"' in project.";
	}

	@Override
	public Image getImage() {
		return JavaPluginImages.get(JavaPluginImages.IMG_OBJS_NLS_NEVER_TRANSLATE);
	}

	@Override
	public IContextInformation getContextInformation() {
		return null;
	}

}