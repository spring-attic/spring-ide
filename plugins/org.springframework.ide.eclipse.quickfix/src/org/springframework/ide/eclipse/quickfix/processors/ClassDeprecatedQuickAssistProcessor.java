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
package org.springframework.ide.eclipse.quickfix.processors;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.quickassist.IQuickAssistInvocationContext;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.ide.eclipse.quickfix.proposals.RemoveDeprecatedQuickFixProposal;


/**
 * @author Terry Denney
 */
public class ClassDeprecatedQuickAssistProcessor extends BeanQuickAssistProcessor {

	private final String className;

	private final IProject project;

	public ClassDeprecatedQuickAssistProcessor(int offset, int length, String text, boolean missingEndQuote,
			String className, IProject project) {
		super(offset, length, text, missingEndQuote);
		this.className = className;
		this.project = project;
	}

	public ICompletionProposal[] computeQuickAssistProposals(IQuickAssistInvocationContext invocationContext) {
		IType type = JdtUtils.getJavaType(project.getProject(), className);
		if (!type.isReadOnly()) {
			return new ICompletionProposal[] { new RemoveDeprecatedQuickFixProposal(offset, length, missingEndQuote,
					className, type) };
		}
		return new ICompletionProposal[0];
	}

}
