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
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.internal.ui.text.correction.ASTResolving;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ide.eclipse.quickfix.QuickfixImages;
import org.springframework.ide.eclipse.quickfix.jdt.util.ProposalCalculatorUtil;


/**
 * Quickfix proposal for adding @Autowired to method or field declaration
 * @author Terry Denney
 */
public class AddAutowireCompletionProposal extends AnnotationCompletionProposal {

	private final BodyDeclaration decl;

	private final Param[] params;

	public AddAutowireCompletionProposal(BodyDeclaration decl, ICompilationUnit cu, Param... params) {
		super(getDisplayName(params), cu, QuickfixImages.getImage(QuickfixImages.ANNOTATION));
		this.decl = decl;
		this.params = params;
	}

	private Annotation createAnnotation(String qualifiedName, String simpleName, CompilationUnit astRoot, AST ast,
			ASTRewrite astRewrite, ImportRewrite importRewrite, boolean memberValuePair, ASTNode precedingNode) {
		if (!ProposalCalculatorUtil.containsImport(getCompilationUnit(), qualifiedName)) {
			importRewrite.addImport(qualifiedName);
		}

		Annotation annotation;

		if (memberValuePair) {
			annotation = ast.newNormalAnnotation();
		}
		else {
			annotation = ast.newMarkerAnnotation();
		}
		annotation.setTypeName(ast.newSimpleName(simpleName));

		ChildListPropertyDescriptor property;

		if (decl instanceof FieldDeclaration) {
			property = FieldDeclaration.MODIFIERS2_PROPERTY;
		}
		else {
			property = MethodDeclaration.MODIFIERS2_PROPERTY;
		}

		if (precedingNode != null) {
			astRewrite.getListRewrite(decl, property).insertBefore(annotation, precedingNode, null);
		}
		else {
			astRewrite.getListRewrite(decl, property).insertFirst(annotation, null);
		}

		return annotation;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected ASTRewrite getRewrite() throws CoreException {
		CompilationUnit astRoot = ASTResolving.findParentCompilationUnit(decl);
		ASTRewrite astRewrite = ASTRewrite.create(astRoot.getAST());

		AST ast = astRewrite.getAST();

		ImportRewrite importRewrite = createImportRewrite(astRoot);
		if (params.length == 0) {
			createAnnotation(Autowired.class.getCanonicalName(), Autowired.class.getSimpleName(), astRoot, ast,
					astRewrite, importRewrite, false, null);
		}
		else {
			NormalAnnotation autowiredAnnotation = (NormalAnnotation) createAnnotation(Autowired.class
					.getCanonicalName(), Autowired.class.getSimpleName(), astRoot, ast, astRewrite, importRewrite,
					true, null);

			for (int i = 0; i < params.length; i++) {
				switch (params[i]) {
				case REQUIRED:
					MemberValuePair requiredValue = ast.newMemberValuePair();
					requiredValue.setName(ast.newSimpleName("required"));
					requiredValue.setValue(ast.newBooleanLiteral(false));
					addLinkedPosition(astRewrite.track(requiredValue.getValue()), i == 0, "Autowire");
					autowiredAnnotation.values().add(requiredValue);
					break;
				// case QUALIFIER:
				// SingleMemberAnnotation qualifierAnnotation =
				// (SingleMemberAnnotation) createAnnotation(
				// Qualifier.class.getCanonicalName(),
				// Qualifier.class.getSimpleName(), astRoot, ast,
				// astRewrite, importRewrite, false, autowiredAnnotation);
				// StringLiteral qualifierValue = ast.newStringLiteral();
				// qualifierValue.setLiteralValue("qualifier");
				// qualifierAnnotation.setValue(qualifierValue);
				// addLinkedPosition(astRewrite.track(qualifierValue), i == 0,
				// "Qualifier");
				// break;
				}
			}
		}

		return astRewrite;
	}

	private static String getDisplayName(Param[] params) {
		StringBuffer buf = new StringBuffer();
		buf.append("Add @Autowired");

		if (params.length > 0) {
			for (int i = 0; i < params.length; i++) {
				if (i > 0) {
					buf.append(", ");
				}
				buf.append(getParamName(params[i]));
			}
		}
		return buf.toString();
	}

	private static String getParamName(Param param) {
		switch (param) {
		case REQUIRED:
			return "(required=false)";
			// case QUALIFIER:
			// return "qualifier";
		}
		return null;
	}

	public enum Param {
		REQUIRED, QUALIFIER
	}

}
