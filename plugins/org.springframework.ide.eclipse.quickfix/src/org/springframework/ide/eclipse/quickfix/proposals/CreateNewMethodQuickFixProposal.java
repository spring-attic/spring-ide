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

import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.graphics.Image;

/**
 * Quick fix proposal for creating a new setter method for a bean property
 * @author Terry Denney
 * @author Leo Dos Santos
 * @author Christian Dupuis
 * @since 2.0
 */
public class CreateNewMethodQuickFixProposal extends BeanAttributeQuickFixProposal {

	private final String label;

	private Object proposal = null;

	public CreateNewMethodQuickFixProposal(int offset, int length, String label, ICompilationUnit targetCU,
			ASTNode invocationNode, List<Expression> arguments, ITypeBinding binding, int relevance,
			boolean missingEndQuote) {
		super(offset, length, missingEndQuote);

		this.proposal = QuickfixReflectionUtils.createNewMethodProposal(label, targetCU, invocationNode, arguments,
				binding, relevance, getImage());
		this.label = label;
	}

	@Override
	public void applyQuickFix(IDocument document) {
		QuickfixReflectionUtils.applyProposal(proposal, document);
	}

	public String getDisplayString() {
		return label;
	}

	public Image getImage() {
		return JavaPluginImages.get(JavaPluginImages.IMG_MISC_PUBLIC);
	}

}
