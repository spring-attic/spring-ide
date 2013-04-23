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
import java.util.Set;

import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.internal.core.SourceField;
import org.eclipse.jdt.internal.core.SourceMethod;
import org.eclipse.jdt.internal.ui.text.correction.AssistContext;
import org.eclipse.jdt.internal.ui.text.java.JavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.source.SourceViewer;
import org.springframework.ide.eclipse.beans.ui.BeansUIImages;
import org.springframework.ide.eclipse.quickfix.jdt.util.ProposalCalculatorUtil;

/**
 * @author Terry Denney
 * @author Martin Lippert
 * @since 2.6
 */
public class QualifierArgumentProposalComputer extends AnnotationProposalComputer {

	@Override
	protected List<ICompletionProposal> computeCompletionProposals(SourceMethod method, IAnnotation annotation,
			JavaContentAssistInvocationContext javaContext) throws JavaModelException {

		List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();

		ITextViewer viewer = javaContext.getViewer();
		if (viewer instanceof SourceViewer) {

			// check for annotation to avoid performance overhead in all other
			// cases
			if (ProposalCalculatorUtil.hasAnnotationInParameters(method, "Qualifier")) {
				SourceViewer sourceViewer = (SourceViewer) viewer;
				ISourceRange methodSourceRange = method.getSourceRange();
				AssistContext assistContext = new AssistContext(javaContext.getCompilationUnit(), sourceViewer,
						methodSourceRange.getOffset(), methodSourceRange.getLength());
				ASTNode node = assistContext.getCoveringNode();
				if (node instanceof MethodDeclaration) {
					int invocationOffset = javaContext.getInvocationOffset();
					MethodDeclaration methodDecl = (MethodDeclaration) node;
					@SuppressWarnings("unchecked")
					List<SingleVariableDeclaration> parameters = methodDecl.parameters();
					for (SingleVariableDeclaration parameter : parameters) {
						Set<Annotation> annotations = ProposalCalculatorUtil.findAnnotations("Qualifier",
								invocationOffset, parameter);
						for (Annotation a : annotations) {
							LocationInformation info = getLocationSourceRange(a, javaContext.getViewer(),
									invocationOffset);
							int locationOffset = info.getOffset();
							int locationLength = info.getLength();
							if (invocationOffset >= locationOffset
									&& invocationOffset <= locationOffset + locationLength) {
								ITypeBinding typeBinding = parameter.getType().resolveBinding();
								proposals.addAll(getMatchingBeansProposal(info.getFilter(), typeBinding, methodDecl,
										javaContext, info));
							}
						}
					}
				}
			}
		}

		return proposals;
	}

	@Override
	protected List<ICompletionProposal> computeCompletionProposals(SourceField field, IAnnotation annotation,
			JavaContentAssistInvocationContext javaContext) throws JavaModelException {
		List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();

		ITextViewer viewer = javaContext.getViewer();
		if (viewer instanceof SourceViewer) {
			SourceViewer sourceViewer = (SourceViewer) viewer;
			ISourceRange fieldSourceRange = field.getSourceRange();
			AssistContext assistContext = new AssistContext(javaContext.getCompilationUnit(), sourceViewer,
					fieldSourceRange.getOffset(), fieldSourceRange.getLength());
			ASTNode node = assistContext.getCoveringNode();
			if (node instanceof FieldDeclaration) {
				int invocationOffset = javaContext.getInvocationOffset();
				FieldDeclaration fieldDecl = (FieldDeclaration) node;
				Set<Annotation> annotations = ProposalCalculatorUtil.findAnnotations("Qualifier", invocationOffset,
						fieldDecl);
				for (Annotation a : annotations) {
					LocationInformation info = getLocationSourceRange(a, javaContext.getViewer(), invocationOffset);
					int locationOffset = info.getOffset();
					int locationLength = info.getLength();
					if (invocationOffset >= locationOffset && invocationOffset <= locationOffset + locationLength) {
						ITypeBinding typeBinding = fieldDecl.getType().resolveBinding();
						proposals.addAll(getMatchingBeansProposal(info.getFilter(), typeBinding, fieldDecl,
								javaContext, info));
					}
				}

			}
		}

		return proposals;
	}

	private List<JavaCompletionProposal> getMatchingBeansProposal(String filter, ITypeBinding typeBinding,
			BodyDeclaration decl, JavaContentAssistInvocationContext javaContext, LocationInformation info) {
		int locationOffset = info.getOffset();
		int locationLength = info.getLength();
		boolean isQuoted = info.isQuoted();

		List<JavaCompletionProposal> proposals = new ArrayList<JavaCompletionProposal>();
		if (typeBinding != null) {
			Set<String> matchingBeans = ProposalCalculatorUtil.getMatchingBeans(javaContext, typeBinding);
			for (String matchingBean : matchingBeans) {
				if (matchingBean.startsWith(filter)) {
					if (isQuoted) {
						proposals.add(new JavaCompletionProposal(matchingBean, locationOffset, locationLength,
								BeansUIImages.getImage(BeansUIImages.IMG_OBJS_BEAN), matchingBean, 0));
					}
					else {
						proposals.add(new JavaCompletionProposal("\"" + matchingBean + "\"", locationOffset,
								locationLength, BeansUIImages.getImage(BeansUIImages.IMG_OBJS_BEAN), matchingBean, 0));
					}
				}
			}
		}
		return proposals;
	}

}
