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
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.internal.ui.text.correction.ASTResolving;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.ide.eclipse.quickfix.QuickfixImages;
import org.springframework.ide.eclipse.quickfix.jdt.util.ProposalCalculatorUtil;


/**
 * Quickfix proposal for adding @Qualifier to method parameter
 * 
 * @author Terry Denney
 * @since 2.6
 */
public class AddQualiferToMethodParamCompletionProposal extends MarkerResolutionProposal {

	private final MethodDeclaration methodDecl;

	// private final List<String> qualifiers;

	private final List<SingleVariableDeclaration> params;

	public AddQualiferToMethodParamCompletionProposal(List<SingleVariableDeclaration> params,
			MethodDeclaration methodDecl, ICompilationUnit cu) {
		super(getDisplayName(params, methodDecl), cu, QuickfixImages.getImage(QuickfixImages.ANNOTATION));
		this.params = params;
		// this.qualifiers = qualifiers;
		this.methodDecl = methodDecl;
	}

	private SingleMemberAnnotation createAnnotation(SingleVariableDeclaration decl, String qualifiedName,
			String simpleName, CompilationUnit astRoot, AST ast, ASTRewrite astRewrite, ImportRewrite importRewrite,
			boolean multiple, int pos) {
		if (!ProposalCalculatorUtil.containsImport(getCompilationUnit(), qualifiedName)) {
			importRewrite.addImport(qualifiedName);
		}

		SingleMemberAnnotation annotation = ast.newSingleMemberAnnotation();
		annotation.setTypeName(ast.newSimpleName(simpleName));
		StringLiteral literal = ast.newStringLiteral();
		// literal.setLiteralValue(qualifier);
		annotation.setValue(literal);
		setTrackPosition(new StringLiteralTrackedPosition(astRewrite.track(literal)));

		ChildListPropertyDescriptor property;

		property = SingleVariableDeclaration.MODIFIERS2_PROPERTY;

		// if (precedingNode != null) {
		// astRewrite.getListRewrite(decl, property).insertAfter(annotation,
		// precedingNode, null);
		// }
		// else {
		astRewrite.getListRewrite(decl, property).insertLast(annotation, null);
		// }

		if (multiple) {
			addLinkedPosition(new StringLiteralTrackedPosition(astRewrite.track(literal)), pos == 0, "Qualifier " + pos);
		}

		return annotation;
	}

	@Override
	protected ASTRewrite getRewrite() throws CoreException {
		CompilationUnit astRoot = ASTResolving.findParentCompilationUnit(methodDecl);
		ASTRewrite astRewrite = ASTRewrite.create(astRoot.getAST());

		AST ast = astRewrite.getAST();

		ImportRewrite importRewrite = createImportRewrite(astRoot);

		// List<SingleVariableDeclaration> parameters = methodDecl.parameters();
		// for (int i = 0; i < qualifiers.size(); i++) {
		// String qualifier = qualifiers.get(i);
		// if (qualifier != null) {
		// SingleVariableDeclaration paramDecl = parameters.get(i);
		// createAnnotation(qualifier, paramDecl,
		// Qualifier.class.getCanonicalName(), Qualifier.class
		// .getSimpleName(), astRoot, ast, astRewrite, importRewrite, null);
		// }
		// }
		for (int i = 0; i < params.size(); i++) {
			createAnnotation(params.get(i), Qualifier.class.getCanonicalName(), "Qualifier", astRoot, ast, astRewrite,
					importRewrite, params.size() > 0, i);
		}

		return astRewrite;
	}

	private static String getDisplayName(List<SingleVariableDeclaration> params, MethodDeclaration methodDecl) {
		StringBuffer buf = new StringBuffer();
		buf.append("Add @Qualifier for ");

		for (int i = 0; i < params.size(); i++) {
			if (i > 0) {
				buf.append(", ");
			}
			buf.append(params.get(i).getName().getFullyQualifiedName());
		}

		// List<SingleVariableDeclaration> params = methodDecl.parameters();
		//
		// boolean first = true;
		// for (int i = 0; i < qualifiers.size(); i++) {
		// String qualifier = qualifiers.get(i);
		// if (qualifier != null) {
		// if (!first) {
		// buf.append(", ");
		// }
		// buf.append("@Qualifier(");
		// buf.append(qualifier);
		// buf.append(") for ");
		//
		// buf.append(params.get(i).getName().toString());
		// first = false;
		// }
		// }

		return buf.toString();
	}

}
