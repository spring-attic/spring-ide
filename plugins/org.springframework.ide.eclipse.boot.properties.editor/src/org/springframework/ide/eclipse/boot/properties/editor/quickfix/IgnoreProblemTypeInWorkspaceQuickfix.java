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
package org.springframework.ide.eclipse.boot.properties.editor.quickfix;

import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.springframework.ide.eclipse.boot.properties.editor.reconciling.SpringPropertiesProblemType;
import org.springframework.ide.eclipse.editor.support.preferences.ProblemSeverityPreferencesUtil;
import org.springframework.ide.eclipse.editor.support.reconcile.ProblemSeverity;

/**
 * Quickfix proposal to change a particular problem type's severity to 'Ignore' in the
 * workspace.
 *
 * @author Kris De Volder
 */
@SuppressWarnings("restriction")
public class IgnoreProblemTypeInWorkspaceQuickfix implements ICompletionProposal {

	private SpringPropertiesProblemType problemType;
	private IPreferenceStore preferences;

	public IgnoreProblemTypeInWorkspaceQuickfix(IPreferenceStore preferences, SpringPropertiesProblemType type) {
		this.preferences = preferences;
		this.problemType = type;
	}

	@Override
	public void apply(IDocument document) {
		ProblemSeverityPreferencesUtil.setSeverity(preferences, problemType, ProblemSeverity.IGNORE);
		ProblemSeverityPreferencesUtil.save(preferences);
	}

	@Override
	public Point getSelection(IDocument document) {
		return null;
	}

	@Override
	public String getAdditionalProposalInfo() {
		return "Globally sets problem severity preference for problems of type '"+problemType.getLabel()+"' to 'Ignore'. ";
	}

	@Override
	public String getDisplayString() {
		return "Ignore '"+ problemType.getLabel()+"' in workspace.";
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