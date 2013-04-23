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
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.SourceField;
import org.eclipse.jdt.internal.core.SourceMethod;
import org.eclipse.jdt.internal.core.SourceRefElement;
import org.eclipse.jdt.internal.core.SourceType;
import org.eclipse.jdt.internal.ui.text.java.JavaCompletionProposalComputer;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.springsource.ide.eclipse.commons.core.SpringCoreUtils;
import org.springsource.ide.eclipse.commons.core.StatusHandler;

/**
 * @author Terry Denney
 * @author Martin Lippert
 * @since 2.6
 */
public class AnnotationArgumentProposalComputer extends JavaCompletionProposalComputer {

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

					// check for type/method/field specific annotation proposal
					// computers
					if (element instanceof SourceRefElement) {
						IAnnotation[] annotations = ((SourceRefElement) element).getAnnotations();
						for (IAnnotation annotation : annotations) {
							String annotationName = annotation.getElementName();
							for (AnnotationProposalComputer computer : AnnotationComputerRegistry
									.getProposalComputer(annotationName)) {
								if (element instanceof SourceField) {
									proposals.addAll(computer.computeCompletionProposals((SourceField) element,
											annotation, javaContext));
								}
								if (element instanceof SourceMethod) {
									proposals.addAll(computer.computeCompletionProposals((SourceMethod) element,
											annotation, javaContext));
								}
								if (element instanceof SourceType) {
									proposals.addAll(computer.computeCompletionProposals((SourceType) element,
											annotation, javaContext));
								}
							}
						}
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

}
