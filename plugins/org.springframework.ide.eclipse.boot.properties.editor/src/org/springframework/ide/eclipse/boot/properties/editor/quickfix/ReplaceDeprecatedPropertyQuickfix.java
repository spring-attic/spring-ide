/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
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
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.springframework.ide.eclipse.boot.properties.editor.metadata.PropertyInfo;
import org.springframework.ide.eclipse.boot.properties.editor.reconciling.ProblemFixer;
import org.springframework.ide.eclipse.boot.properties.editor.reconciling.SpringPropertyProblem;
import org.springframework.ide.eclipse.boot.util.Log;
import org.springframework.ide.eclipse.editor.support.completions.DocumentEdits;
import org.springframework.ide.eclipse.editor.support.reconcile.QuickfixContext;

@SuppressWarnings("restriction")
public class ReplaceDeprecatedPropertyQuickfix implements ICompletionProposal {

	public static ProblemFixer FIXER = (context, problem, proposals) -> {
		PropertyInfo metadata = problem.getMetadata();
		if (metadata!=null) {
			String replacement = metadata.getDeprecationReplacement();
			if (replacement!=null) {
				//No need to check problem type...  we only attach this fixer to problems of applicable type.
				proposals.add(new ReplaceDeprecatedPropertyQuickfix(context, problem));
			}
		}
	};

	private final SpringPropertyProblem problem;
	private DocumentEdits edits;

	public ReplaceDeprecatedPropertyQuickfix(QuickfixContext context, SpringPropertyProblem problem) {
		this.problem = problem;
		String replacement = getReplacementProperty();
		this.edits = new DocumentEdits(context.getDocument());
		int start = problem.getOffset();
		int end = start + problem.getLength();
		edits.delete(start, end);
		edits.insert(start, replacement);
	}

	@Override
	public void apply(IDocument doc) {
		try {
			edits.apply(doc);
		} catch (Exception e) {
			Log.log(e);
		}
	}

	private String getReplacementProperty() {
		return problem.getMetadata().getDeprecationReplacement();
	}

	@Override
	public Point getSelection(IDocument doc) {
		try {
			return edits.getSelection(doc);
		} catch (Exception e) {
			Log.log(e);
			return null;
		}
	}

	@Override
	public String getAdditionalProposalInfo() {
		return null;
	}

	@Override
	public String getDisplayString() {
		return "Change to '"+getReplacementProperty()+"'";
	}

	@Override
	public Image getImage() {
		return JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_CHANGE);
	}

	@Override
	public IContextInformation getContextInformation() {
		return null;
	}

}
