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

import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.internal.core.SourceField;
import org.eclipse.jdt.internal.core.SourceMethod;
import org.eclipse.jdt.internal.core.SourceType;
import org.eclipse.jdt.internal.ui.text.java.JavaCompletionProposalComputer;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

/**
 * @author Terry Denney
 * @author Martin Lippert
 * @since 2.6
 */
public abstract class AnnotationProposalComputer extends JavaCompletionProposalComputer {

	/**
	 * Default is to return empty list of proposals. Subclass should overwrite
	 * if proposals are available for methods
	 * @param method
	 * @param javaContext
	 * @return list of completion proposal for the method and javaContext
	 * @throws JavaModelException
	 */
	protected List<ICompletionProposal> computeCompletionProposals(SourceMethod method,
			LocationInformation locationInfo, Annotation annotation, JavaContentAssistInvocationContext javaContext)
			throws JavaModelException {
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
	protected List<ICompletionProposal> computeCompletionProposals(SourceType type, LocationInformation locationInfo,
			Annotation annotation, JavaContentAssistInvocationContext javaContext) throws JavaModelException {
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
	protected List<ICompletionProposal> computeCompletionProposals(SourceField field, LocationInformation locationInfo,
			Annotation annotation, JavaContentAssistInvocationContext javaContext) throws JavaModelException {
		return new ArrayList<ICompletionProposal>();
	}

}
