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
package org.springframework.ide.eclipse.quickfix.jdt.proposals;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.springframework.ide.eclipse.quickfix.QuickfixImages;


/**
 * @author Terry Denney
 * @since 2.6
 */
public class RemoveQualifierCompletionProposal extends MarkerResolutionProposal {

	private final Annotation annotation;

	public RemoveQualifierCompletionProposal(Annotation annotation, ICompilationUnit cu) {
		super("Remove @Qualifier", cu, QuickfixImages.getImage(QuickfixImages.REMOVE_CORRECTION));
		this.annotation = annotation;
	}

	@Override
	protected ASTRewrite getRewrite() throws CoreException {
		AST ast = annotation.getAST();
		ASTRewrite astRewrite = ASTRewrite.create(ast);

		astRewrite.remove(annotation, null);

		return astRewrite;
	}

}
