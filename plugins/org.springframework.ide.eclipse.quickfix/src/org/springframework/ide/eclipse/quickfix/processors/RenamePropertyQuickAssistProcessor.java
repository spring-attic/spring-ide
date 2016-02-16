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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.quickassist.IQuickAssistInvocationContext;
import org.springframework.ide.eclipse.quickfix.proposals.RenamePropertyQuickfixProposal;


/**
 * @author Terry Denney
 * @author Christian Dupuis
 */
public class RenamePropertyQuickAssistProcessor extends BeanQuickAssistProcessor {

	private final RenamePropertyQuickfixProposal proposal;

	public RenamePropertyQuickAssistProcessor(int offset, int length, String className, String text, IProject project,
			boolean missingEndQuote, IFile beanFile) {
		super(offset, length, text, missingEndQuote);

		this.proposal = new RenamePropertyQuickfixProposal(offset, length, text, className, missingEndQuote, beanFile,
				project);
	}

	public ICompletionProposal[] computeQuickAssistProposals(IQuickAssistInvocationContext invocationContext) {
		return new ICompletionProposal[] { proposal };
	}

}
