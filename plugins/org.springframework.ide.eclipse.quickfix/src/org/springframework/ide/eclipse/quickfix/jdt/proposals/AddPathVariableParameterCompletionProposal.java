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
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ITrackedNodePosition;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.internal.ui.text.correction.ASTResolving;
import org.springframework.ide.eclipse.quickfix.QuickfixImages;
import org.springframework.ide.eclipse.quickfix.jdt.util.ProposalCalculatorUtil;
import org.springframework.ide.eclipse.quickfix.jdt.util.UriTemplateVariable;
import org.springframework.web.bind.annotation.PathVariable;


/**
 * @author Terry Denney
 * @since 2.6
 */
public class AddPathVariableParameterCompletionProposal extends MarkerResolutionProposal {

	private final UriTemplateVariable variable;

	private final MethodDeclaration methodDecl;

	public AddPathVariableParameterCompletionProposal(UriTemplateVariable variable, MethodDeclaration methodDecl,
			ICompilationUnit cu) {
		super(getDisplayName(variable), cu, QuickfixImages.getImage(QuickfixImages.ANNOTATION));
		this.variable = variable;
		this.methodDecl = methodDecl;
	}

	private static String getDisplayName(UriTemplateVariable variable) {
		return "Create parameter " + variable.getVariableName();
	}

	@Override
	protected ASTRewrite getRewrite() throws CoreException {
		CompilationUnit astRoot = ASTResolving.findParentCompilationUnit(methodDecl);
		ASTRewrite astRewrite = ASTRewrite.create(astRoot.getAST());

		AST ast = astRewrite.getAST();

		String importName = PathVariable.class.getCanonicalName();
		if (!ProposalCalculatorUtil.containsImport(getCompilationUnit(), importName)) {
			ImportRewrite importRewrite = createImportRewrite(astRoot);
			importRewrite.addImport(importName);
		}

		addLinkedPosition(new ITrackedNodePosition() {

			public int getStartPosition() {
				return variable.getOffset();
			}

			public int getLength() {
				return variable.getVariableName().length();
			}
		}, true, "variableName");

		SingleVariableDeclaration paramDecl = ast.newSingleVariableDeclaration();

		SimpleType variableType = ast.newSimpleType(ast.newSimpleName("String"));
		paramDecl.setType(variableType);
		addLinkedPosition(astRewrite.track(variableType), false, "variableType");

		SimpleName variableName = ast.newSimpleName(variable.getVariableName());
		paramDecl.setName(variableName);
		addLinkedPosition(astRewrite.track(variableName), false, "variableName");

		SingleMemberAnnotation annotation = ast.newSingleMemberAnnotation();
		annotation.setTypeName(ast.newSimpleName("PathVariable"));

		StringLiteral pathVariableName = ast.newStringLiteral();
		pathVariableName.setLiteralValue(variable.getVariableName());
		annotation.setValue(pathVariableName);
		addLinkedPosition(new StringLiteralTrackedPosition(astRewrite.track(pathVariableName)), false, "variableName");

		ListRewrite listRewrite = astRewrite.getListRewrite(paramDecl, SingleVariableDeclaration.MODIFIERS2_PROPERTY);
		listRewrite.insertFirst(annotation, null);

		listRewrite = astRewrite.getListRewrite(methodDecl, MethodDeclaration.PARAMETERS_PROPERTY);
		listRewrite.insertLast(paramDecl, null);

		return astRewrite;
	}
}
