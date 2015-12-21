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

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ITrackedNodePosition;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.internal.ui.text.correction.ASTResolving;
import org.springframework.ide.eclipse.quickfix.QuickfixImages;
import org.springframework.ide.eclipse.quickfix.jdt.util.ProposalCalculatorUtil;
import org.springframework.ide.eclipse.quickfix.jdt.util.UriTemplateVariable;
import org.springframework.web.bind.annotation.PathVariable;


/**
 * @author Terry Denney
 * @since 2.6
 */
public class AddPathVariableCompletionProposal extends MarkerResolutionProposal {

	private final MethodDeclaration methodDecl;

	private final SingleVariableDeclaration param;

	private final UriTemplateVariable variable;

	public AddPathVariableCompletionProposal(UriTemplateVariable variable, SingleVariableDeclaration param,
			MethodDeclaration methodDecl, ICompilationUnit cu, boolean atTypeDecl) {
		super(getDisplayName(variable, param, methodDecl, atTypeDecl), cu, QuickfixImages
				.getImage(QuickfixImages.ANNOTATION));
		this.variable = variable;
		this.param = param;
		this.methodDecl = methodDecl;
	}

	@SuppressWarnings("unchecked")
	private static String getDisplayName(UriTemplateVariable variable, SingleVariableDeclaration param,
			MethodDeclaration methodDecl, boolean atTypeDecl) {
		StringBuilder result = new StringBuilder();
		result.append("Add @PathVariable(\"");
		result.append(variable.getVariableName());
		result.append("\") to ");
		result.append(param.getName().getFullyQualifiedName());
		if (atTypeDecl) {
			result.append(" to method ");
			result.append(methodDecl.getName().getFullyQualifiedName());
			result.append("(");
			List<SingleVariableDeclaration> params = methodDecl.parameters();
			for (SingleVariableDeclaration p : params) {
				result.append(ProposalCalculatorUtil.getTypeName(p.getType()));
			}
			result.append(")");
		}
		return result.toString();
	}

	@Override
	protected ASTRewrite getRewrite() throws CoreException {
		CompilationUnit astRoot = ASTResolving.findParentCompilationUnit(methodDecl);
		final ASTRewrite astRewrite = ASTRewrite.create(astRoot.getAST());

		AST ast = astRewrite.getAST();

		String importName = PathVariable.class.getCanonicalName();
		if (!ProposalCalculatorUtil.containsImport(getCompilationUnit(), importName)) {
			ImportRewrite importRewrite = createImportRewrite(astRoot);
			importRewrite.addImport(importName);
		}

		SingleMemberAnnotation annotation = ast.newSingleMemberAnnotation();
		annotation.setTypeName(ast.newSimpleName("PathVariable"));
		StringLiteral pathVariableName = ast.newStringLiteral();
		pathVariableName.setLiteralValue(variable.getVariableName());
		annotation.setValue(pathVariableName);

		astRewrite.getListRewrite(param, SingleVariableDeclaration.MODIFIERS2_PROPERTY).insertFirst(annotation, null);

		final ITrackedNodePosition literalPosition = astRewrite.track(variable.getNode());
		addLinkedPosition(new ITrackedNodePosition() {

			public int getStartPosition() {
				return literalPosition.getStartPosition() + variable.getOffsetFromNode();
			}

			public int getLength() {
				return variable.getVariableName().length();
			}
		}, true, "PathVariable");

		addLinkedPosition(new StringLiteralTrackedPosition(astRewrite.track(pathVariableName)), false, "PathVariable");

		return astRewrite;
	}
}
