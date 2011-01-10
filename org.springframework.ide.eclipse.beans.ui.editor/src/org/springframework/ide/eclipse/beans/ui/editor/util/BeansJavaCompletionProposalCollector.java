/*******************************************************************************
 * Copyright (c) 2006, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.editor.util;

import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.ui.text.java.CompletionProposalCollector;

/**
 * {@link CompletionProposalCollector} used with JDT's {@link SearchEngine}.
 * @author Christian Dupuis
 * @author Torsten Juergeleit
 * @since 1.3.6
 */
class BeansJavaCompletionProposalCollector extends
		CompletionProposalCollector {
	
	private int flags;

	public BeansJavaCompletionProposalCollector(ICompilationUnit cu, int flags) {
		super(cu);
		this.flags = flags;
	}

	@Override
	public void accept(CompletionProposal proposal) {
		boolean accepted = false;
		if (CompletionProposal.TYPE_REF == proposal.getKind()) {
			if ((flags & BeansJavaCompletionUtils.FLAG_CLASS) != 0 && !Flags.isInterface(proposal.getFlags())) {
				super.accept(proposal);
				accepted = true;
				
			}
			if (!accepted && (flags & BeansJavaCompletionUtils.FLAG_INTERFACE) != 0 && Flags.isInterface(proposal.getFlags())) {
				super.accept(proposal);
				accepted = true;
				
			}
		}
		if (CompletionProposal.PACKAGE_REF == proposal.getKind()) {
			if (!accepted && (flags & BeansJavaCompletionUtils.FLAG_PACKAGE) != 0) {
				super.accept(proposal);
			}
		}
	}
}
