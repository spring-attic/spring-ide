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
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ITrackedNodePosition;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.internal.ui.text.correction.ASTResolving;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.ide.eclipse.quickfix.QuickfixImages;
import org.springframework.ide.eclipse.quickfix.jdt.util.ProposalCalculatorUtil;


/**
 * Quickfix proposal for adding @Qualifier to method or field declaration
 * 
 * @author Terry Denney
 * @since 2.6
 */
public class AddQualiferCompletionProposal extends MarkerResolutionProposal {

	private final BodyDeclaration decl;

	// private final String qualifier;

	public AddQualiferCompletionProposal(BodyDeclaration decl, ICompilationUnit cu) {
		super("Add @Qualifier", cu, QuickfixImages.getImage(QuickfixImages.ANNOTATION));
		this.decl = decl;
	}

	@Override
	protected ASTRewrite getRewrite() throws CoreException {
		CompilationUnit astRoot = ASTResolving.findParentCompilationUnit(decl);
		ASTRewrite astRewrite = ASTRewrite.create(astRoot.getAST());

		AST ast = astRewrite.getAST();

		ImportRewrite importRewrite = createImportRewrite(astRoot);
		String qualifiedName = Qualifier.class.getName();
		if (!ProposalCalculatorUtil.containsImport(getCompilationUnit(), qualifiedName)) {
			importRewrite.addImport(qualifiedName);
		}

		SingleMemberAnnotation annotation = ast.newSingleMemberAnnotation();
		annotation.setTypeName(ast.newSimpleName("Qualifier"));
		StringLiteral literal = ast.newStringLiteral();
		// literal.setLiteralValue(qualifier);
		annotation.setValue(literal);
		ITrackedNodePosition position = astRewrite.track(literal);
		addLinkedPosition(new StringLiteralTrackedPosition(position), true, "Qualifier");

		ChildListPropertyDescriptor property;

		if (decl instanceof FieldDeclaration) {
			property = FieldDeclaration.MODIFIERS2_PROPERTY;
		}
		else {
			property = MethodDeclaration.MODIFIERS2_PROPERTY;
		}

		ListRewrite listRewrite = astRewrite.getListRewrite(decl, property);
		listRewrite.insertFirst(annotation, null);

		return astRewrite;
	}

	// private static String getDisplayName(String qualifier) {
	// StringBuffer buf = new StringBuffer();
	// buf.append("Add @Qualifier(");
	// buf.append(qualifier);
	// buf.append(")");
	// return buf.toString();
	// }

}
