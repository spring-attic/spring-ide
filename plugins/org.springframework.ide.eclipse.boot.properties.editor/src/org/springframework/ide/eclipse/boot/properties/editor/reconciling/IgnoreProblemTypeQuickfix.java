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
package org.springframework.ide.eclipse.boot.properties.editor.reconciling;

import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.springframework.ide.eclipse.boot.properties.editor.preferences.ProblemSeverityPreferencesUtil;

/**
 * Quickfix proposal to change a particular problem type's severity to 'Ignore'.
 *
 * @author Kris De Volder
 */
@SuppressWarnings("restriction")
public class IgnoreProblemTypeQuickfix implements ICompletionProposal {

	private ProblemType problemType;
	private IPreferenceStore preferences;

	public IgnoreProblemTypeQuickfix(IPreferenceStore preferences, ProblemType type) {
		this.preferences = preferences;
		this.problemType = type;
	}

	@Override
	public void apply(IDocument document) {
		ProblemSeverityPreferencesUtil.setSeverity(preferences, problemType, ProblemSeverity.IGNORE);
	}

	@Override
	public Point getSelection(IDocument document) {
		return null;
	}

	@Override
	public String getAdditionalProposalInfo() {
		return "Sets problem severity preference for problems of type '"+problemType.getLabel()+"' to 'Ignore'. ";
	}

	@Override
	public String getDisplayString() {
		return "Ignore all '"+problemType.getLabel()+"' problems";
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