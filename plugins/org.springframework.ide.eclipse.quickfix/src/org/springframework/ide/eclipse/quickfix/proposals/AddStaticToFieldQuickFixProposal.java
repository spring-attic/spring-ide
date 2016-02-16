/*******************************************************************************
 *  Copyright (c) 2012 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.quickfix.proposals;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.internal.ui.text.correction.proposals.ModifierChangeCorrectionProposal;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.swt.graphics.Image;
import org.springframework.ide.eclipse.quickfix.QuickfixUtils;


/**
 * @author Terry Denney
 */
public class AddStaticToFieldQuickFixProposal extends BeanAttributeQuickFixProposal implements ICompletionProposal {

	private final String fieldName;

	private final ModifierChangeCorrectionProposal proposal;

	public AddStaticToFieldQuickFixProposal(int offset, int length, boolean missingEndQuote, IJavaProject javaProject,
			String className, String fieldName) {
		super(offset, length, missingEndQuote);
		this.fieldName = fieldName;

		this.proposal = QuickfixUtils.createModifierChangeCorrectionProposal(className, fieldName, javaProject,
				getDisplayString(), true);
	}

	@Override
	public void applyQuickFix(IDocument document) {
		proposal.apply(document);
	}

	public String getDisplayString() {
		return "change modifier of " + fieldName + " to static";
	}

	public Image getImage() {
		return JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_CHANGE);
	}
}
