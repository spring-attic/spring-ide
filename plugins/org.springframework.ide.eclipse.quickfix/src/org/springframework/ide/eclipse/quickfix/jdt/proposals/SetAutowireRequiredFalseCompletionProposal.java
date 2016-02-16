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
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

/**
 * @author Terry Denney
 * @since 2.6
 */
public class SetAutowireRequiredFalseCompletionProposal extends MarkerResolutionProposal {

	private final MemberValuePair valuePair;

	private final Annotation annotation;

	public SetAutowireRequiredFalseCompletionProposal(MemberValuePair valuePair, ICompilationUnit cu) {
		super("Set required=false in @Autowired", cu, null);
		this.annotation = null;
		this.valuePair = valuePair;
	}

	public SetAutowireRequiredFalseCompletionProposal(Annotation annotation, ICompilationUnit cu) {
		super("Set required=false in @Autowired", cu, null);
		this.annotation = annotation;
		this.valuePair = null;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected ASTRewrite getRewrite() throws CoreException {
		AST ast = annotation.getAST();
		ASTRewrite astRewrite = ASTRewrite.create(ast);

		// change required=true to required = false
		if (valuePair != null) {
			astRewrite.replace(valuePair.getValue(), ast.newBooleanLiteral(false), null);
			return astRewrite;
		}

		// add required=false to annotation
		else {
			NormalAnnotation newAnnotation = ast.newNormalAnnotation();
			newAnnotation.setTypeName(ast.newSimpleName(annotation.getTypeName().getFullyQualifiedName()));

			MemberValuePair requiredValue = ast.newMemberValuePair();
			requiredValue.setName(ast.newSimpleName("required"));
			requiredValue.setValue(ast.newBooleanLiteral(false));
			newAnnotation.values().add(requiredValue);

			astRewrite.replace(annotation, newAnnotation, null);
		}

		return astRewrite;
	}

}
