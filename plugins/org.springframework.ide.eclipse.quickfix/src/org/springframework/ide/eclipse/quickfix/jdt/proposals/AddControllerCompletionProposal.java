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
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.internal.ui.text.correction.ASTResolving;
import org.springframework.ide.eclipse.quickfix.QuickfixImages;
import org.springframework.ide.eclipse.quickfix.jdt.util.ProposalCalculatorUtil;
import org.springframework.stereotype.Controller;


/**
 * @author Terry Denney
 * @since 2.6
 */
public class AddControllerCompletionProposal extends AnnotationCompletionProposal {

	private final TypeDeclaration typeDecl;

	public AddControllerCompletionProposal(TypeDeclaration typeDecl, ICompilationUnit cu, boolean atTypeDecl) {
		super(getDisplayName(typeDecl, atTypeDecl), cu, QuickfixImages.getImage(QuickfixImages.ANNOTATION));
		this.typeDecl = typeDecl;
	}

	private static String getDisplayName(TypeDeclaration typeDecl, boolean atTypeDecl) {
		StringBuilder result = new StringBuilder();
		result.append("Add @Controller");
		if (!atTypeDecl) {
			result.append(" to type ");
			result.append(typeDecl.getName().getFullyQualifiedName());
		}
		return result.toString();
	}

	@Override
	protected ASTRewrite getRewrite() throws CoreException {
		CompilationUnit astRoot = ASTResolving.findParentCompilationUnit(typeDecl);
		ASTRewrite astRewrite = ASTRewrite.create(astRoot.getAST());

		AST ast = astRewrite.getAST();

		String importName = Controller.class.getCanonicalName();
		if (!ProposalCalculatorUtil.containsImport(getCompilationUnit(), importName)) {
			ImportRewrite importRewrite = createImportRewrite(astRoot);
			importRewrite.addImport(importName);
		}

		MarkerAnnotation annotation = ast.newMarkerAnnotation();
		annotation.setTypeName(ast.newSimpleName("Controller"));

		astRewrite.getListRewrite(typeDecl, TypeDeclaration.MODIFIERS2_PROPERTY).insertFirst(annotation, null);

		return astRewrite;
	}

}
