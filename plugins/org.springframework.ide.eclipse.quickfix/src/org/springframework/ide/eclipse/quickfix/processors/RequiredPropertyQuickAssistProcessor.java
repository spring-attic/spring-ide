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

import java.util.List;

import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.quickassist.IQuickAssistInvocationContext;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.springframework.ide.eclipse.quickfix.proposals.AddPropertyQuickFixProposal;


/**
 * @author Terry Denney
 */
public class RequiredPropertyQuickAssistProcessor extends BeanQuickAssistProcessor {

	private final List<String> missingProperties;

	private final IDOMNode beanNode;

	public RequiredPropertyQuickAssistProcessor(int offset, int length, String text, boolean missingEndQuote,
			List<String> missingProperties, IDOMNode beanNode) {
		super(offset, length, text, missingEndQuote);
		this.missingProperties = missingProperties;
		this.beanNode = beanNode;
	}

	public ICompletionProposal[] computeQuickAssistProposals(IQuickAssistInvocationContext invocationContext) {
		StringBuilder label = new StringBuilder("Add <property> for ");
		for (int i = 0; i < missingProperties.size(); i++) {
			if (i > 0) {
				label.append(", ");
			}
			label.append(missingProperties.get(i));
		}

		return new ICompletionProposal[] { new AddPropertyQuickFixProposal(offset, length, missingEndQuote,
				missingProperties, beanNode, label.toString()) };
	}
}
