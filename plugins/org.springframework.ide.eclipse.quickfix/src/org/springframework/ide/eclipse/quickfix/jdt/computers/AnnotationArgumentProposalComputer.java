/*******************************************************************************
 *  Copyright (c) 2012, 2013 VMware, Inc.
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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.AnnotatableInfo;
import org.eclipse.jdt.internal.core.SourceField;
import org.eclipse.jdt.internal.core.SourceMethod;
import org.eclipse.jdt.internal.core.SourceRefElement;
import org.eclipse.jdt.internal.core.SourceType;
import org.eclipse.jdt.internal.ui.text.java.JavaCompletionProposalComputer;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.source.SourceViewer;
import org.springsource.ide.eclipse.commons.core.SpringCoreUtils;
import org.springsource.ide.eclipse.commons.core.StatusHandler;

/**
 * @author Terry Denney
 * @author Martin Lippert
 * @since 2.6
 */
public class AnnotationArgumentProposalComputer extends JavaCompletionProposalComputer {

	// TODO: clean up code and make sure non attribute arguments are handled
	// better

	@Override
	public List<ICompletionProposal> computeCompletionProposals(ContentAssistInvocationContext context,
			IProgressMonitor monitor) {
		if (context instanceof JavaContentAssistInvocationContext) {
			JavaContentAssistInvocationContext javaContext = (JavaContentAssistInvocationContext) context;

			// check if project is a spring project
			if (SpringCoreUtils.isSpringProject(javaContext.getProject().getProject())) {

				ICompilationUnit cu = javaContext.getCompilationUnit();

				try {
					List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();

					int invocationOffset = context.getInvocationOffset();
					IJavaElement element = cu.getElementAt(invocationOffset);
					cu.makeConsistent(monitor);

					// check for type/method/field specific annotation
					// proposal computers
					if (element instanceof SourceRefElement
							&& ((SourceRefElement) element).getElementInfo() instanceof AnnotatableInfo) {
						SourceRefElement sourceRefElement = (SourceRefElement) element;
						IAnnotation[] annotations = sourceRefElement.getAnnotations();
						for (IAnnotation annotation : annotations) {
							String annotationName = annotation.getElementName();

							if (javaContext.getViewer() instanceof SourceViewer) {
								// SourceViewer sourceViewer = (SourceViewer)
								// javaContext.getViewer();
								// AssistContext assistContext = new
								// AssistContext(cu, sourceViewer,
								// invocationOffset, 0);
								if (annotation instanceof SourceRefElement) {
									if (isWithinRange((SourceRefElement) annotation, invocationOffset)) {
										IMemberValuePair[] memberValuePairs = annotation.getMemberValuePairs();
										for (IMemberValuePair memberValuePair : memberValuePairs) {
											String attributeName = memberValuePair.getMemberName();
											proposals.addAll(computeCompletionProposals(annotationName, attributeName,
													memberValuePair.getValue(), annotation, element, javaContext));
										}
									}

									// ASTNode node = ((SourceRefElement)
									// annotation).findNode(assistContext.getASTRoot());
									//
									// if (node instanceof NormalAnnotation) {
									// NormalAnnotation normalAnnotation =
									// (NormalAnnotation) node;
									// @SuppressWarnings("unchecked")
									// List<MemberValuePair> pairs =
									// normalAnnotation.values();
									//
									// for (MemberValuePair pair : pairs) {
									// Expression value = pair.getValue();
									// if (value != null) {
									// if (isWithinRange(value,
									// invocationOffset)) {
									// String attributeName =
									// pair.getName().getIdentifier();
									// proposals.addAll(computeCompletionProposals(annotationName,
									// attributeName, value, normalAnnotation,
									// element,
									// javaContext));
									// }
									//
									// }
									// }
									// }
									// else if (node instanceof
									// SingleMemberAnnotation) {
									// SingleMemberAnnotation
									// singleMemberAnnotation =
									// (SingleMemberAnnotation) node;
									// Expression value =
									// singleMemberAnnotation.getValue();
									// if (isWithinRange(value,
									// invocationOffset)) {
									// proposals.addAll(computeCompletionProposals(annotationName,
									// null, value,
									// singleMemberAnnotation, element,
									// javaContext));
									// }
									// }
								}
							}

						}
					}

					if (proposals.size() > 0) {
						return proposals;
					}

					// check for remaining registered proposal computers
					for (JavaCompletionProposalComputer computer : AnnotationComputerRegistry.computers) {
						List<ICompletionProposal> completionProposals = computer.computeCompletionProposals(context,
								monitor);
						if (completionProposals != null) {
							proposals.addAll(completionProposals);
						}
					}

					return proposals;

				}
				catch (JavaModelException e) {
					StatusHandler.log(e.getStatus());
				}
			}
		}
		return Collections.emptyList();
	}

	// private boolean isWithinRange(Expression value, int invocationOffset) {
	// int startPosition = value.getStartPosition();
	// int length = value.getLength();
	// return startPosition < invocationOffset && startPosition + length >=
	// invocationOffset;
	// }
	//
	private boolean isWithinRange(SourceRefElement element, int invocationOffset) throws JavaModelException {
		int startPosition = element.getSourceRange().getOffset();
		int length = element.getSourceRange().getLength();
		return startPosition < invocationOffset && startPosition + length >= invocationOffset;
	}

	// private List<ICompletionProposal> computeCompletionProposals(String
	// annotationName, String attributeName,
	// Expression value, Annotation annotation, IJavaElement element,
	// JavaContentAssistInvocationContext javaContext) throws JavaModelException
	// {
	// List<ICompletionProposal> proposals = new
	// ArrayList<ICompletionProposal>();
	//
	// if (attributeName == null) {
	// attributeName = AnnotationComputerRegistry.DEFAULT_ATTRIBUTE_NAME;
	// }
	//
	// if (value instanceof StringLiteral) {
	// StringLiteral stringLiteral = (StringLiteral) value;
	//
	// proposals.addAll(computeCompletionProposalsHelper(element,
	// AnnotationComputerRegistry.getProposalComputer(annotationName,
	// attributeName), stringLiteral,
	// javaContext, annotation));
	// }
	//
	// else if (value instanceof ArrayInitializer) {
	// ArrayInitializer arrayInit = (ArrayInitializer) value;
	//
	// @SuppressWarnings("unchecked")
	// List<Expression> expressions = arrayInit.expressions();
	//
	// Set<AnnotationProposalComputer> computers = AnnotationComputerRegistry
	// .getProposalComputerForArrayAttribute(annotationName);
	// for (Expression expression : expressions) {
	// if (expression instanceof StringLiteral) {
	// proposals.addAll(computeCompletionProposalsHelper(element, computers,
	// (StringLiteral) expression,
	// javaContext, annotation));
	// }
	// }
	// }
	// return proposals;
	// }

	private List<ICompletionProposal> computeCompletionProposals(String annotationName, String attributeName,
			Object value, IAnnotation annotation, IJavaElement element, JavaContentAssistInvocationContext javaContext)
			throws JavaModelException {
		List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();

		if (attributeName == null) {
			attributeName = AnnotationComputerRegistry.DEFAULT_ATTRIBUTE_NAME;
		}

		if (value instanceof String) {
			proposals.addAll(computeCompletionProposalsHelper(element,
					AnnotationComputerRegistry.getProposalComputer(annotationName, attributeName), (String) value,
					javaContext, annotation));
		}

		else if (value instanceof Object[]) {
			Object[] values = (Object[]) value;
			for (Object v : values) {
				if (v instanceof String) {
					proposals.addAll(computeCompletionProposalsHelper(element, AnnotationComputerRegistry
							.getProposalComputerForArrayAttribute(annotationName, attributeName), (String) v,
							javaContext, annotation));
				}
			}
		}

		// else if (value instanceof ArrayInitializer) {
		// ArrayInitializer arrayInit = (ArrayInitializer) value;
		//
		// @SuppressWarnings("unchecked")
		// List<Expression> expressions = arrayInit.expressions();
		//
		// Set<AnnotationProposalComputer> computers =
		// AnnotationComputerRegistry
		// .getProposalComputerForArrayAttribute(annotationName);
		// for (Expression expression : expressions) {
		// if (expression instanceof StringLiteral) {
		// // TODO
		// // proposals.addAll(computeCompletionProposalsHelper(element,
		// // computers, (StringLiteral) expression,
		// // javaContext, annotation));
		// }
		// }
		// }
		return proposals;
	}

	// private List<ICompletionProposal>
	// computeCompletionProposalsHelper(IJavaElement element,
	// Set<AnnotationProposalComputer> computers, StringLiteral stringLiteral,
	// JavaContentAssistInvocationContext javaContext, Annotation annotation)
	// throws JavaModelException {
	// List<ICompletionProposal> proposals = new
	// ArrayList<ICompletionProposal>();
	//
	// for (AnnotationProposalComputer computer : computers) {
	// if (element instanceof SourceField) {
	// proposals.addAll(computer.computeCompletionProposals((SourceField)
	// element,
	// getLocationInformation(stringLiteral, javaContext), annotation,
	// javaContext));
	// }
	// if (element instanceof SourceMethod) {
	// proposals.addAll(computer.computeCompletionProposals((SourceMethod)
	// element,
	// getLocationInformation(stringLiteral, javaContext), annotation,
	// javaContext));
	// }
	// if (element instanceof SourceType) {
	// proposals.addAll(computer.computeCompletionProposals((SourceType)
	// element,
	// getLocationInformation(stringLiteral, javaContext), annotation,
	// javaContext));
	// }
	// }
	//
	// return proposals;
	// }
	//
	private List<ICompletionProposal> computeCompletionProposalsHelper(IJavaElement element,
			Set<AnnotationProposalComputer> computers, String value, JavaContentAssistInvocationContext javaContext,
			IAnnotation annotation) throws JavaModelException {
		List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();

		for (AnnotationProposalComputer computer : computers) {
			if (element instanceof SourceField) {
				proposals.addAll(computer.computeCompletionProposals((SourceField) element, value, annotation,
						javaContext));
			}
			if (element instanceof SourceMethod) {
				proposals.addAll(computer.computeCompletionProposals((SourceMethod) element, value, annotation,
						javaContext));
			}
			if (element instanceof SourceType) {
				proposals.addAll(computer.computeCompletionProposals((SourceType) element, value, annotation,
						javaContext));
			}
		}

		return proposals;
	}

	// private LocationInformation getLocationInformation(StringLiteral value,
	// JavaContentAssistInvocationContext javaContext) {
	// int startPos = value.getStartPosition();
	// int invocationOffset = javaContext.getInvocationOffset();
	// String literalValue = value.getLiteralValue();
	// int length = invocationOffset - startPos;
	// if (length > literalValue.length()) {
	// length = literalValue.length();
	// invocationOffset = startPos + length;
	// }
	// String filter = literalValue.substring(0, length);
	//
	// return new LocationInformation(startPos, invocationOffset, filter,
	// value);
	// }
	//
}
