/*******************************************************************************
 *  Copyright (c) 2013 GoPivotal, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.quickfix.jdt.proposals;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ITrackedNodePosition;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.internal.core.SourceRefElement;
import org.eclipse.jdt.internal.ui.text.correction.ASTResolving;
import org.eclipse.jdt.internal.ui.text.correction.AssistContext;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.source.SourceViewer;
import org.springframework.ide.eclipse.quickfix.QuickfixImages;

/**
 * Proposal for completing a class attribute (i.e. ClassName.class)
 * @author Terry Denney
 * @since 3.3.0
 * 
 */
public class ClassCompletionProposal extends AnnotationCompletionProposal {

	private final String className;

	private Annotation annotation;

	private ASTNode oldLiteral;

	private final IPackageFragment packageFragment;

	private final IAnnotation iAnnotation;

	private final JavaContentAssistInvocationContext javaContext;

	public ClassCompletionProposal(String className, IAnnotation a, IPackageFragment packageFragment,
			JavaContentAssistInvocationContext javaContext) {
		super(className + ".class", javaContext.getCompilationUnit(), QuickfixImages.getImage(QuickfixImages.CLASS));
		this.className = className;
		this.iAnnotation = a;
		this.packageFragment = packageFragment;
		this.javaContext = javaContext;
	}

	private void setupASTNodes() {
		ICompilationUnit cu = javaContext.getCompilationUnit();
		SourceViewer sourceViewer = (SourceViewer) javaContext.getViewer();
		int invocationOffset = javaContext.getInvocationOffset();
		AssistContext assistContext = new AssistContext(cu, sourceViewer, invocationOffset, 0);
		ASTNode node = ((SourceRefElement) iAnnotation).findNode(assistContext.getASTRoot());
		annotation = (Annotation) node;
		if (node instanceof NormalAnnotation) {
			NormalAnnotation normalAnnotation = (NormalAnnotation) node;
			@SuppressWarnings("unchecked")
			List<MemberValuePair> pairs = normalAnnotation.values();

			for (MemberValuePair pair : pairs) {
				Expression value = pair.getValue();
				if (value instanceof TypeLiteral) {
					if (isWithinRange(value, invocationOffset)) {
						oldLiteral = value;
					}
				}
				else if (value instanceof ArrayInitializer) {
					ArrayInitializer arrayInit = (ArrayInitializer) value;
					@SuppressWarnings("unchecked")
					List<Expression> expressions = arrayInit.expressions();
					for (Expression expression : expressions) {
						if (expression instanceof TypeLiteral) {
							if (isWithinRange(expression, invocationOffset)) {
								oldLiteral = expression;
							}
						}
						else if (expression instanceof SimpleName) {
							if (isWithinRange(expression, invocationOffset)) {
								oldLiteral = expression;
							}
						}
					}
				}
			}
		}
	}

	private boolean isWithinRange(Expression value, int invocationOffset) {
		int startPosition = value.getStartPosition();
		int length = value.getLength();
		return startPosition < invocationOffset && startPosition + length >= invocationOffset;
	}

	@Override
	protected ASTRewrite getRewrite() throws CoreException {
		setupASTNodes();

		AST ast = annotation.getAST();
		ASTRewrite rewrite = ASTRewrite.create(ast);

		if (oldLiteral == null) {
			return rewrite;
		}

		SimpleName typeName = ast.newSimpleName(className);
		SimpleType type = ast.newSimpleType(typeName);
		TypeLiteral typeLiteral = ast.newTypeLiteral();
		typeLiteral.setType(type);
		final ITrackedNodePosition newValuePosition = rewrite.track(typeLiteral);

		rewrite.replace(oldLiteral, typeLiteral, null);

		if (packageFragment != null) {
			ImportRewrite importRewrite = createImportRewrite(ASTResolving.findParentCompilationUnit(oldLiteral));
			importRewrite.addImport(packageFragment.getElementName() + "." + className);
		}

		setTrackPosition(new ITrackedNodePosition() {

			public int getStartPosition() {
				return newValuePosition.getStartPosition() + newValuePosition.getLength();
			}

			public int getLength() {
				return 0;
			}
		});

		return rewrite;
	}
}
