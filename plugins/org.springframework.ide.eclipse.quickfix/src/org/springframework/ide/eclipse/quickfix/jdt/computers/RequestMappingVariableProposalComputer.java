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
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.internal.core.SourceMethod;
import org.eclipse.jdt.internal.core.SourceRefElement;
import org.eclipse.jdt.internal.core.SourceType;
import org.eclipse.jdt.internal.ui.text.correction.AssistContext;
import org.eclipse.jdt.ui.SharedASTProvider;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.source.SourceViewer;
import org.springframework.ide.eclipse.quickfix.jdt.proposals.RequestMappingVariableCompletionProposal;
import org.springframework.ide.eclipse.quickfix.jdt.util.ProposalCalculatorUtil;

/**
 * @author Terry Denney
 * @author Martin Lippert
 * @since 2.6
 */
public class RequestMappingVariableProposalComputer extends AnnotationProposalComputer {

	// TODO: clean up code and restructure to not find ASTNode till proposal is
	// invoked

	@Override
	protected List<ICompletionProposal> computeCompletionProposals(SourceMethod method, String value,
			IAnnotation annotation, JavaContentAssistInvocationContext javaContext) throws JavaModelException {
		// // TODO Auto-generated method stub
		// return super.computeCompletionProposals(type, value, annotation,
		// javaContext);
		// }
		// @Override
		// protected List<ICompletionProposal>
		// computeCompletionProposals(SourceMethod method,
		// LocationInformation locationInfo, Annotation annotation,
		// JavaContentAssistInvocationContext javaContext)
		// throws JavaModelException {
		return computeCompletionProposalsHelper(method, value, annotation, javaContext);
	}

	@Override
	protected List<ICompletionProposal> computeCompletionProposals(SourceType type, String value,
			IAnnotation annotation, JavaContentAssistInvocationContext javaContext) throws JavaModelException {
		// // TODO Auto-generated method stub
		// return super.computeCompletionProposals(type, value, annotation,
		// javaContext);
		// }
		// @Override
		// protected List<ICompletionProposal>
		// computeCompletionProposals(SourceType type, LocationInformation
		// locationInfo,
		// Annotation annotation, JavaContentAssistInvocationContext
		// javaContext) throws JavaModelException {
		return computeCompletionProposalsHelper(type, value, annotation, javaContext);
	}

	// private List<ICompletionProposal>
	// computeCompletionProposalsHelper(IMember element,
	// LocationInformation locationInfo, Annotation annotation,
	// JavaContentAssistInvocationContext javaContext)
	// throws JavaModelException {
	private List<ICompletionProposal> computeCompletionProposalsHelper(IMember element, String value, IAnnotation a,
			JavaContentAssistInvocationContext javaContext) throws JavaModelException {
		List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();

		ITextViewer viewer = javaContext.getViewer();
		if (viewer instanceof SourceViewer) {
			ICompilationUnit cu = javaContext.getCompilationUnit();
			SourceViewer sourceViewer = (SourceViewer) javaContext.getViewer();
			int invocationOffset = javaContext.getInvocationOffset();
			AssistContext assistContext = new AssistContext(cu, sourceViewer, invocationOffset, 0,
					SharedASTProvider.WAIT_NO);
			ASTNode node = ((SourceRefElement) a).findNode(assistContext.getASTRoot());

			if (node == null) {
				node = assistContext.getCoveredNode();
			}

			if (!(node instanceof Annotation)) {
				return proposals;
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
				if (expression instanceof StringLiteral) {
					locationInfo = getLocationInformation((StringLiteral) expression, javaContext);
				}
			}

			if (locationInfo == null) {
				return proposals;
			}

			int locationOffset = locationInfo.getOffset();
			int locationLength = locationInfo.getLength();
			String content = locationInfo.getFilter();

			if (invocationOffset >= locationOffset && invocationOffset <= locationOffset + locationLength) {
				int startIndex;
				int index = 0;
				boolean found = false;
				while (!found) {
					startIndex = content.indexOf("{");
					if (startIndex < 0 || startIndex + locationOffset >= invocationOffset) {
						break;
					}

					content = content.substring(startIndex + 1);
					index += startIndex + 1;
					if (!(content.contains("{"))) {
						found = true;
					}
				}

				locationOffset += 1; // ignore opening quote

				if (found) {
					ISourceRange sourceRange = element.getSourceRange();
					//Now get the 'real' ast.
					assistContext = new AssistContext(javaContext.getCompilationUnit(), sourceViewer,
							sourceRange.getOffset(), sourceRange.getLength(), SharedASTProvider.WAIT_YES);

					node = assistContext.getCoveringNode();
					Annotation annotation = (Annotation) ((SourceRefElement) a).findNode(assistContext.getASTRoot());

					if (content.endsWith("}")) {
						content = content.substring(0, content.length() - 1);
					}

					if (node instanceof MethodDeclaration) {
						MethodDeclaration methodDecl = (MethodDeclaration) node;
						proposals.addAll(getProposals(methodDecl, annotation, content, locationOffset, index,
								javaContext));
					}

					else if (node instanceof TypeDeclaration) {
						TypeDeclaration typeDecl = (TypeDeclaration) node;
						MethodDeclaration[] methodDecls = typeDecl.getMethods();
						for (MethodDeclaration methodDecl : methodDecls) {
							proposals.addAll(getProposals(methodDecl, annotation, content, locationOffset, index,
									javaContext));
						}
					}
				}
			}
		}

		return proposals;
	}

	private List<ICompletionProposal> getProposals(MethodDeclaration methodDecl, Annotation annotation, String filter,
			int valueOffset, int variableOffset, JavaContentAssistInvocationContext javaContext) {
		List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();

		@SuppressWarnings("unchecked")
		List<SingleVariableDeclaration> params = methodDecl.parameters();
		for (SingleVariableDeclaration param : params) {
			ITypeBinding typeBinding = param.getType().resolveBinding();
			if (!ProposalCalculatorUtil.isKnownRequestMappingParamType(javaContext.getProject().getProject(),
					typeBinding)) {
				Set<Annotation> pathVariables = ProposalCalculatorUtil.findAnnotations("PathVariable", param);
				// boolean differentVariableName = false;
				if (pathVariables.size() > 0) {
					for (Annotation pathVariable : pathVariables) {
						if (pathVariable instanceof SingleMemberAnnotation) {
							Expression expression = ((SingleMemberAnnotation) pathVariable).getValue();
							if (expression instanceof StringLiteral) {
								String variableName = ((StringLiteral) expression).getLiteralValue();
								if (variableName.startsWith(filter)) {
									proposals.add(new RequestMappingVariableCompletionProposal(param, variableName,
											valueOffset + variableOffset, filter.length(), annotation, methodDecl,
											javaContext));
									// differentVariableName = true;
								}
							}
						}
						else if (pathVariable instanceof MarkerAnnotation) {
							String paramName = param.getName().getFullyQualifiedName();
							if (paramName.startsWith(filter)) {
								proposals.add(new RequestMappingVariableCompletionProposal(param, paramName,
										valueOffset + variableOffset, filter.length(), annotation, methodDecl,
										javaContext));
							}
						}
					}
				}
			}
		}

		return proposals;
	}

}
