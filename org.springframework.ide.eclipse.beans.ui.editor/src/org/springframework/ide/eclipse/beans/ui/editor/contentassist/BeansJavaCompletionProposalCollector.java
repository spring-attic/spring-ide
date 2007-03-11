/*
 * Copyright 2002-2006 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.springframework.ide.eclipse.beans.ui.editor.contentassist;

import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.ui.text.java.CompletionProposalCollector;

public class BeansJavaCompletionProposalCollector extends
		CompletionProposalCollector {

	private boolean interfaceRequired = false;

	/**
	 * Creates a new instance ready to collect proposals. If the passed
	 * <code>ICompilationUnit</code> is not contained in an
	 * {@link IJavaProject}, no javadoc will be available as
	 * {@link org.eclipse.jface.text.contentassist.ICompletionProposal#getAdditionalProposalInfo() additional info}
	 * on the created proposals.
	 * 
	 * @param cu the compilation unit that the result collector will operate on
	 */
	public BeansJavaCompletionProposalCollector(ICompilationUnit cu,
			boolean interfaceRequired) {
		super(cu);
		this.interfaceRequired = interfaceRequired;
	}

	/**
	 * Creates a new instance ready to collect proposals. Note that proposals
	 * for anonymous types and method declarations are not created when using
	 * this constructor, as those need to know the compilation unit that they
	 * are created on. Use
	 * {@link CompletionProposalCollector#CompletionProposalCollector(ICompilationUnit)}
	 * instead to get all proposals.
	 * <p>
	 * If the passed Java project is <code>null</code>, no javadoc will be
	 * available as
	 * {@link org.eclipse.jface.text.contentassist.ICompletionProposal#getAdditionalProposalInfo() additional info}
	 * on the created (e.g. method and type) proposals.
	 * </p>
	 * 
	 * @param project the project that the result collector will operate on, or
	 * <code>null</code>
	 */
	public BeansJavaCompletionProposalCollector(IJavaProject project) {
		super(project);
		interfaceRequired = false;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Subclasses may replace, but usually should not need to. Consider
	 * replacing
	 * {@linkplain #createJavaCompletionProposal(CompletionProposal) createJavaCompletionProposal}
	 * instead.
	 * </p>
	 */
	@Override
	public void accept(CompletionProposal proposal) {
		if (CompletionProposal.TYPE_REF == proposal.getKind()) {
			if ((!interfaceRequired && !Flags.isInterface(proposal.getFlags()))
					|| (interfaceRequired && Flags.isInterface(proposal
							.getFlags()))) {
				super.accept(proposal);
			}
		}
		if (CompletionProposal.PACKAGE_REF == proposal.getKind()) {
			super.accept(proposal);
		}
	}
}
