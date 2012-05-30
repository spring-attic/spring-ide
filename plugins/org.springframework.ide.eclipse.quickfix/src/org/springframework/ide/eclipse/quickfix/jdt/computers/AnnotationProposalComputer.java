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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.internal.core.SourceField;
import org.eclipse.jdt.internal.core.SourceMethod;
import org.eclipse.jdt.internal.core.SourceType;
import org.eclipse.jdt.internal.ui.text.java.JavaCompletionProposalComputer;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.springframework.ide.eclipse.core.SpringCoreUtils;
import org.springsource.ide.eclipse.commons.core.StatusHandler;


/**
 * @author Terry Denney
 * @author Martin Lippert
 * @since 2.6
 */
public abstract class AnnotationProposalComputer extends JavaCompletionProposalComputer {

	@Override
	public List<ICompletionProposal> computeCompletionProposals(ContentAssistInvocationContext context,
			IProgressMonitor monitor) {
		if (context instanceof JavaContentAssistInvocationContext) {
			JavaContentAssistInvocationContext javaContext = (JavaContentAssistInvocationContext) context;

			// check if project is a spring project
			if (SpringCoreUtils.isSpringProject(javaContext.getProject().getProject())) {

				ICompilationUnit cu = javaContext.getCompilationUnit();

				try {
					int invocationOffset = context.getInvocationOffset();
					IJavaElement element = cu.getElementAt(invocationOffset);
					if (element instanceof SourceField) {
						return computeCompletionProposals((SourceField) element, javaContext);
					}
					if (element instanceof SourceMethod) {
						return computeCompletionProposals((SourceMethod) element, javaContext);
					}
					if (element instanceof SourceType) {
						return computeCompletionProposals((SourceType) element, javaContext);
					}
				}
				catch (JavaModelException e) {
					StatusHandler.log(e.getStatus());
				}
			}
		}
		return Collections.emptyList();
	}

	protected LocationInformation getLocationSourceRange(Annotation annotation, ITextViewer viewer, int invocationOffset) {
		return getLocationSourceRange(annotation, viewer, invocationOffset, null);
	}

	protected LocationInformation getLocationSourceRange(Annotation annotation, ITextViewer viewer,
			int invocationOffset, String valuePairName) {
		if (annotation instanceof NormalAnnotation) {
			NormalAnnotation normalAnnotation = (NormalAnnotation) annotation;
			List<MemberValuePair> pairs = normalAnnotation.values();
			if (pairs.size() == 0) {
				int startPos = normalAnnotation.getStartPosition();

				// TODO: make this more robust with different spacing in code
				if (startPos + normalAnnotation.getLength() - 1 == invocationOffset) {
					return new LocationInformation(startPos + normalAnnotation.getLength() - 1, 0, "");
				}
			}
			else {
				for (MemberValuePair pair : pairs) {
					if (valuePairName == null || valuePairName.equals(pair.getName().getFullyQualifiedName())) {
						Expression value = pair.getValue();
						LocationInformation location = getLocationSourceRange(value, annotation, invocationOffset);
						if (location != null) {
							return location;
						}
					}
				}
			}
		}
		else if (annotation instanceof SingleMemberAnnotation) {
			Expression value = ((SingleMemberAnnotation) annotation).getValue();
			LocationInformation location = getLocationSourceRange(value, annotation, invocationOffset);
			if (location != null) {
				return location;
			}
		}
		return new LocationInformation(annotation.getStartPosition(), -1, "");
	}

	private LocationInformation getLocationSourceRange(Expression value, Annotation annotation, int invocationOffset) {
		if (value == null) {
			return null;
		}

		int startPos = value.getStartPosition();
		int length = value.getLength();
		if (startPos <= invocationOffset && startPos + length >= invocationOffset) {
			if (value instanceof StringLiteral) {
				StringLiteral string = (StringLiteral) value;
				String currentString = string.getLiteralValue();
				startPos += 1; // skip the open quotes
				int filterLength = invocationOffset - startPos;
				String filter;
				if (filterLength < currentString.length()) {
					filter = currentString.substring(0, filterLength);
				}
				else {
					filter = currentString;
				}

				return new LocationInformation(startPos, currentString.length(), filter, true);
			}
			else if (value instanceof SimpleName) {
				SimpleName name = (SimpleName) value;
				return new LocationInformation(startPos, length, name.getIdentifier());
			}
		}

		return null;
	}

	/**
	 * Default is to return empty list of proposals. Subclass should overwrite
	 * if proposals are available for methods
	 * @param method
	 * @param javaContext
	 * @return list of completion proposal for the method and javaContext
	 * @throws JavaModelException
	 */
	protected List<ICompletionProposal> computeCompletionProposals(SourceMethod method,
			JavaContentAssistInvocationContext javaContext) throws JavaModelException {
		return new ArrayList<ICompletionProposal>();
	}

	/**
	 * Default is to return empty list of proposals. Subclass should overwrite
	 * if proposals are available for methods
	 * @param type
	 * @param javaContext
	 * @return list of completion proposal for the method and javaContext
	 * @throws JavaModelException
	 */
	protected List<ICompletionProposal> computeCompletionProposals(SourceType type,
			JavaContentAssistInvocationContext javaContext) throws JavaModelException {
		return new ArrayList<ICompletionProposal>();
	}

	/**
	 * Default is to return empty list of proposals. Subclass should overwrite
	 * if proposals are available for fields
	 * @param field
	 * @param javaContext
	 * @return list of completion proposal for the field and javaContext
	 * @throws JavaModelException
	 */
	protected List<ICompletionProposal> computeCompletionProposals(SourceField field,
			JavaContentAssistInvocationContext javaContext) throws JavaModelException {
		return new ArrayList<ICompletionProposal>();
	}

}
