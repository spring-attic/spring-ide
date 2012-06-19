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
package org.springframework.ide.eclipse.quickfix.jdt.computers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.internal.ui.text.correction.AssistContext;
import org.eclipse.jdt.internal.ui.text.java.JavaCompletionProposalComputer;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.source.ISourceViewer;
import org.springframework.ide.eclipse.core.SpringCoreUtils;
import org.springframework.ide.eclipse.quickfix.Activator;
import org.springframework.ide.eclipse.quickfix.jdt.proposals.QualifierCompletionProposal;
import org.springsource.ide.eclipse.commons.core.StatusHandler;


/**
 * @author Terry Denney
 * @author Martin Lippert
 * @since 2.6
 */
public class QualifierCompletionProposalComputer extends JavaCompletionProposalComputer {

	@Override
	public List<ICompletionProposal> computeCompletionProposals(ContentAssistInvocationContext context,
			IProgressMonitor monitor) {

		List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();

		if (context instanceof JavaContentAssistInvocationContext) {
			JavaContentAssistInvocationContext javaContext = (JavaContentAssistInvocationContext) context;

			// check if project is a spring project
			if (SpringCoreUtils.isSpringProject(javaContext.getProject().getProject())) {

				ITextViewer viewer = javaContext.getViewer();
				IDocument document = viewer.getDocument();

				try {
					int invocationOffset = context.getInvocationOffset();
					int start = invocationOffset;
					int end = invocationOffset;

					while (start != 0 && Character.isUnicodeIdentifierPart(document.getChar(start - 1))) {
						start--;
					}

					if (start > 0) {
						if (document.getChar(start - 1) == '@') {
							String annotation = document.get(start, end - start);
							if ("qualifier".startsWith(annotation.toLowerCase())) {
								if (viewer instanceof ISourceViewer) {
									AssistContext assistContext = new AssistContext(javaContext.getCompilationUnit(),
											(ISourceViewer) viewer, start - 1, end - start + 1);
									ASTNode annotationNode = assistContext.getCoveredNode();
									BodyDeclaration decl = getParentDeclaration(annotationNode);

									if (decl instanceof FieldDeclaration) {
										// FieldDeclaration fieldDecl =
										// (FieldDeclaration) decl;
										// ITypeBinding typeBinding =
										// fieldDecl.getType().resolveBinding();
										proposals
												.add(new QualifierCompletionProposal(annotationNode, decl, javaContext));
										// proposals.addAll(getMatchingBeansProposal(annotationNode,
										// fieldDecl,
										// typeBinding, javaContext, start,
										// end
										// - start));
									}
									else if (decl instanceof MethodDeclaration) {
										// MethodDeclaration methodDecl =
										// (MethodDeclaration) decl;
										SingleVariableDeclaration variableDecl = getParentVariableDeclaration(annotationNode);
										if (variableDecl != null) {
											// ITypeBinding typeBinding =
											// variableDecl.getType().resolveBinding();
											proposals.add(new QualifierCompletionProposal(annotationNode, decl,
													javaContext));
											// proposals.addAll(getMatchingBeansProposal(annotationNode,
											// methodDecl,
											// typeBinding, javaContext,
											// start,
											// end - start));
										}
									}
								}
							}
						}
					}
				}
				catch (BadLocationException e) {
					StatusHandler.log(new Status(Status.ERROR, Activator.PLUGIN_ID, e.getMessage(), e));
				}
			}
		}
		return proposals;
	}

	private BodyDeclaration getParentDeclaration(ASTNode node) {
		if (node == null || node instanceof BodyDeclaration) {
			return (BodyDeclaration) node;
		}
		return getParentDeclaration(node.getParent());
	}

	private SingleVariableDeclaration getParentVariableDeclaration(ASTNode node) {
		if (node == null || node instanceof SingleVariableDeclaration) {
			return (SingleVariableDeclaration) node;
		}
		return getParentVariableDeclaration(node.getParent());
	}

	// private List<ICompletionProposal> getMatchingBeansProposal(ASTNode
	// annotationNode, BodyDeclaration decl,
	// ITypeBinding typeBinding, JavaContentAssistInvocationContext javaContext,
	// int offset, int length) {
	// List<ICompletionProposal> proposals = new
	// ArrayList<ICompletionProposal>();
	//
	// if (typeBinding != null) {
	// Set<String> matchingBeans =
	// ProposalCalculatorUtil.getMatchingBeans(javaContext, typeBinding);
	// for (String matchingBean : matchingBeans) {
	// proposals.add(new QualifierCompletionProposal(matchingBean,
	// annotationNode, decl, javaContext));
	// }
	// }
	// return proposals;
	// }

}
