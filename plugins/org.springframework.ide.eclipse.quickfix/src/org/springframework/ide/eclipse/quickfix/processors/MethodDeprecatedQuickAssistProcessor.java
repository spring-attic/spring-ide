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

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.quickassist.IQuickAssistInvocationContext;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.ide.eclipse.quickfix.proposals.RemoveDeprecatedQuickFixProposal;


/**
 * @author Terry Denney
 */
public class MethodDeprecatedQuickAssistProcessor extends BeanQuickAssistProcessor {

	private final String className;

	private final String methodName;

	private final IMethod method;

	public MethodDeprecatedQuickAssistProcessor(int offset, int length, String text, boolean missingEndQuote,
			String className, String methodName, IMethod method) {
		super(offset, length, text, missingEndQuote);
		this.className = className;
		this.methodName = methodName;
		this.method = method;
	}

	public ICompletionProposal[] computeQuickAssistProposals(IQuickAssistInvocationContext invocationContext) {
		IType type = JdtUtils.getJavaType(method.getJavaProject().getProject(), className);
		if (!type.isReadOnly() && method != null) {

			return new ICompletionProposal[] { new RemoveDeprecatedQuickFixProposal(offset, length, missingEndQuote,
					className, methodName, method) };
		}

		return new ICompletionProposal[0];
	}

}
