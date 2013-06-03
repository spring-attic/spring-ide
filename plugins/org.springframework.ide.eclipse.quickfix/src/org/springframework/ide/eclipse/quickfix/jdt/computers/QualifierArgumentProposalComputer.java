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
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.internal.core.SourceField;
import org.eclipse.jdt.internal.core.SourceMethod;
import org.eclipse.jdt.internal.core.SourceRefElement;
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

	// TODO: clean up code

	@Override
	protected List<ICompletionProposal> computeCompletionProposals(SourceMethod method, String value, IAnnotation a,
			JavaContentAssistInvocationContext javaContext) throws JavaModelException {
		// @Override
		// protected List<ICompletionProposal>
		// computeCompletionProposals(SourceMethod method,
		// LocationInformation locationInfo, Annotation annotation,
		// JavaContentAssistInvocationContext javaContext)
		// throws JavaModelException {

		List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();

		ITextViewer viewer = javaContext.getViewer();
		if (viewer instanceof SourceViewer) {
			ICompilationUnit cu = javaContext.getCompilationUnit();
			SourceViewer sourceViewer = (SourceViewer) javaContext.getViewer();
			int invocationOffset = javaContext.getInvocationOffset();
			AssistContext assistContext = new AssistContext(cu, sourceViewer, invocationOffset, 0);
			ASTNode node = ((SourceRefElement) a).findNode(assistContext.getASTRoot());

			if (node == null) {
				node = assistContext.getCoveredNode();
			}

			if (!(a instanceof Annotation)) {
				return Collections.emptyList();
			}
			Annotation annotation = (Annotation) a;

			LocationInformation locationInfo = null;
			if (node instanceof NormalAnnotation) {
				NormalAnnotation normalAnnotation = (NormalAnnotation) node;
				@SuppressWarnings("unchecked")
				List<MemberValuePair> pairs = normalAnnotation.values();

				for (MemberValuePair pair : pairs) {
					Expression expression = pair.getValue();
					if (expression instanceof StringLiteral) {
						locationInfo = getLocationInformation((StringLiteral) expression, javaContext);
					}
				}
			}
			else if (node instanceof SingleMemberAnnotation) {
				SingleMemberAnnotation singleMemberAnnotation = (SingleMemberAnnotation) node;
				Expression expression = singleMemberAnnotation.getValue();
				locationInfo = getLocationInformation((StringLiteral) expression, javaContext);
			}

			if (locationInfo == null) {
				return Collections.emptyList();
			}
			// if (viewer instanceof SourceViewer) {
			//
			// // check for annotation to avoid performance overhead in all
			// other
			// // cases
			// if (ProposalCalculatorUtil.hasAnnotationInParameters(method,
			// "Qualifier")) {

			ISourceRange methodSourceRange = method.getSourceRange();
			assistContext = new AssistContext(javaContext.getCompilationUnit(), sourceViewer,
					methodSourceRange.getOffset(), methodSourceRange.getLength());
			node = assistContext.getCoveringNode();
			if (node instanceof MethodDeclaration) {
				MethodDeclaration methodDecl = (MethodDeclaration) node;
				@SuppressWarnings("unchecked")
				List<SingleVariableDeclaration> parameters = methodDecl.parameters();
				for (SingleVariableDeclaration parameter : parameters) {
					Set<Annotation> annotations = ProposalCalculatorUtil.findAnnotations("Qualifier", invocationOffset,
							parameter);
					for (Annotation an : annotations) {
						if (an.equals(annotation)) {
							// LocationInformation info =
							// getLocationSourceRange(a,
							// javaContext.getViewer(),
							// invocationOffset);
							// int locationOffset = info.getOffset();
							// int locationLength = info.getLength();
							// if (invocationOffset >= locationOffset
							// && invocationOffset <= locationOffset +
							// locationLength) {
							ITypeBinding typeBinding = parameter.getType().resolveBinding();
							proposals.addAll(getMatchingBeansProposal(locationInfo.getFilter(), typeBinding,
									methodDecl, javaContext, locationInfo));
						}
					}
				}
			}
		}

		return proposals;
	}

	@Override
	protected List<ICompletionProposal> computeCompletionProposals(SourceField field, String value, IAnnotation a,
			JavaContentAssistInvocationContext javaContext) throws JavaModelException {
		// @Override
		// protected List<ICompletionProposal>
		// computeCompletionProposals(SourceField field, LocationInformation
		// locationInfo,
		// Annotation annotation, JavaContentAssistInvocationContext
		// javaContext) throws JavaModelException {
		List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();

		ITextViewer viewer = javaContext.getViewer();
		if (viewer instanceof SourceViewer) {
			ICompilationUnit cu = javaContext.getCompilationUnit();
			SourceViewer sourceViewer = (SourceViewer) javaContext.getViewer();
			int invocationOffset = javaContext.getInvocationOffset();
			AssistContext assistContext = new AssistContext(cu, sourceViewer, invocationOffset, 0);
			ASTNode node = ((SourceRefElement) a).findNode(assistContext.getASTRoot());

			if (node == null) {
				node = assistContext.getCoveredNode();
			}

			LocationInformation locationInfo = null;
			if (node instanceof NormalAnnotation) {
				NormalAnnotation normalAnnotation = (NormalAnnotation) node;
				@SuppressWarnings("unchecked")
				List<MemberValuePair> pairs = normalAnnotation.values();

				for (MemberValuePair pair : pairs) {
					Expression expression = pair.getValue();
					if (expression instanceof StringLiteral) {
						locationInfo = getLocationInformation((StringLiteral) expression, javaContext);
					}
				}
			}
			else if (node instanceof SingleMemberAnnotation) {
				SingleMemberAnnotation singleMemberAnnotation = (SingleMemberAnnotation) node;
				Expression expression = singleMemberAnnotation.getValue();
				locationInfo = getLocationInformation((StringLiteral) expression, javaContext);
			}

			if (locationInfo == null) {
				return Collections.emptyList();
			}

			ISourceRange fieldSourceRange = field.getSourceRange();
			assistContext = new AssistContext(javaContext.getCompilationUnit(), sourceViewer,
					fieldSourceRange.getOffset(), fieldSourceRange.getLength());
			node = assistContext.getCoveringNode();
			if (node instanceof FieldDeclaration) {
				// int invocationOffset = javaContext.getInvocationOffset();
				FieldDeclaration fieldDecl = (FieldDeclaration) node;
				// Set<Annotation> annotations =
				// ProposalCalculatorUtil.findAnnotations("Qualifier",
				// invocationOffset,
				// fieldDecl);
				// for (Annotation a : annotations) {
				// LocationInformation info = getLocationSourceRange(a,
				// javaContext.getViewer(), invocationOffset);
				// int locationOffset = info.getOffset();
				// int locationLength = info.getLength();
				// if (invocationOffset >= locationOffset && invocationOffset <=
				// locationOffset + locationLength) {
				ITypeBinding typeBinding = fieldDecl.getType().resolveBinding();
				proposals.addAll(getMatchingBeansProposal(locationInfo.getFilter(), typeBinding, fieldDecl,
						javaContext, locationInfo));
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
