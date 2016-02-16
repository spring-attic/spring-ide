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
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.internal.ui.text.correction.ASTResolving;
import org.springframework.ide.eclipse.quickfix.QuickfixImages;
import org.springframework.ide.eclipse.quickfix.jdt.util.ProposalCalculatorUtil;
import org.springframework.web.bind.annotation.PathVariable;


/**
 * @author Terry Denney
 * @since 2.6
 */
public class AddRequestMappingParamCompletionProposal extends AnnotationCompletionProposal {

	private final SingleVariableDeclaration param;

	private final Class<?> annotationClass;

	public AddRequestMappingParamCompletionProposal(SingleVariableDeclaration param, Class<?> annotationClass,
			ICompilationUnit cu) {
		super("Add @" + annotationClass.getSimpleName(), cu, QuickfixImages.getImage(QuickfixImages.ANNOTATION));
		this.param = param;
		this.annotationClass = annotationClass;
	}

	@Override
	protected ASTRewrite getRewrite() throws CoreException {
		CompilationUnit astRoot = ASTResolving.findParentCompilationUnit(param);
		ASTRewrite astRewrite = ASTRewrite.create(astRoot.getAST());

		AST ast = astRewrite.getAST();

		String importName = annotationClass.getCanonicalName();
		if (!ProposalCalculatorUtil.containsImport(getCompilationUnit(), importName)) {
			ImportRewrite importRewrite = createImportRewrite(astRoot);
			importRewrite.addImport(importName);
		}

		Annotation annotation;
		if (annotationClass.equals(PathVariable.class)) {
			SingleMemberAnnotation sAnnotation = ast.newSingleMemberAnnotation();
			StringLiteral paramName = ast.newStringLiteral();
			paramName.setLiteralValue(param.getName().getFullyQualifiedName());
			sAnnotation.setValue(paramName);

			addLinkedPosition(new StringLiteralTrackedPosition(astRewrite.track(paramName)), true, "paramValue");
			annotation = sAnnotation;
		}
		else {
			annotation = ast.newMarkerAnnotation();
		}

		annotation.setTypeName(ast.newSimpleName(annotationClass.getSimpleName()));

		astRewrite.getListRewrite(param, SingleVariableDeclaration.MODIFIERS2_PROPERTY).insertFirst(annotation, null);

		return astRewrite;
	}
}
