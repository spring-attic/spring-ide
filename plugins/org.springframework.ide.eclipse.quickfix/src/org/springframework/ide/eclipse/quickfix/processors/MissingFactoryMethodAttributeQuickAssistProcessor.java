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

import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.quickassist.IQuickAssistInvocationContext;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.springframework.ide.eclipse.quickfix.proposals.AddFactoryMethodQuickFixProposal;


/**
 * @author Terry Denney
 */
public class MissingFactoryMethodAttributeQuickAssistProcessor extends BeanQuickAssistProcessor {

	private final IDOMNode beanNode;

	public MissingFactoryMethodAttributeQuickAssistProcessor(int offset, int length, String text,
			boolean missingEndQuote, IDOMNode beanNode) {
		super(offset, length, text, missingEndQuote);
		this.beanNode = beanNode;
	}

	public ICompletionProposal[] computeQuickAssistProposals(IQuickAssistInvocationContext invocationContext) {
		return new ICompletionProposal[] { new AddFactoryMethodQuickFixProposal(offset, length, missingEndQuote,
				beanNode) };
	}

}
