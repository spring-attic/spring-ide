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
import org.eclipse.jdt.internal.ui.text.correction.proposals.NewVariableCorrectionProposal;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.graphics.Image;
import org.springframework.ide.eclipse.quickfix.QuickfixUtils;


/**
 * @author Terry Denney
 */
public class CreateNewFieldQuickFixProposal extends BeanAttributeQuickFixProposal {

	private final String className;

	private final String fieldName;

	private final NewVariableCorrectionProposal proposal;

	public CreateNewFieldQuickFixProposal(int offset, int length, String text, boolean missingEndQuote,
			IJavaProject javaProject, String className, String fieldName) {
		super(offset, length, missingEndQuote);
		this.className = className;
		this.fieldName = fieldName;

		proposal = QuickfixUtils.createNewVariableCorrectionProposal(className, fieldName, javaProject,
				getDisplayString(), true);
	}

	@Override
	public void applyQuickFix(IDocument document) {
		proposal.apply(document);
	}

	public String getDisplayString() {
		return "Create field \'" + fieldName + "\' in \'" + className + "\'";
	}

	public Image getImage() {
		return proposal.getImage();
	}

}
