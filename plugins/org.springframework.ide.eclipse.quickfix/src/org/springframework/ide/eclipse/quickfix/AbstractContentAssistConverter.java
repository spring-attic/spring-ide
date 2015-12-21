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
package org.springframework.ide.eclipse.quickfix;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.internal.ui.text.correction.NameMatcher;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.BeanReferenceContentAssistCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.ClassContentAssistCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.IContentAssistCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.IContentAssistContext;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.bean.PropertyNameContentAssistCalculator;

/**
 * Abstract class for bridging between quickfixes and content assist calculators
 * @author Terry Denney
 * @author Leo Dos Santos
 * @author Christian Dupuis
 * @since 2.0
 */
public abstract class AbstractContentAssistConverter {

	private final IDOMNode node;

	private final IFile file;

	private final String attributeName;

	public AbstractContentAssistConverter(IDOMNode node, String attributeName, IFile file) {
		this.node = node;
		this.attributeName = attributeName;
		this.file = file;
	}

	private Set<ContentAssistProposalWrapper> computeNameMatchedProposals(String toBeMatched, boolean exactMatch,
			String prefix, IContentAssistCalculator calculator) {
		Set<ContentAssistProposalWrapper> proposals = computeProposals(prefix, calculator);

		Set<ContentAssistProposalWrapper> result = new HashSet<ContentAssistProposalWrapper>();

		for (ContentAssistProposalWrapper proposal : proposals) {
			String name = proposal.getName();
			if (isMatched(toBeMatched, name, exactMatch)) {
				result.add(proposal);
			}
		}
		return result;
	}

	private Set<ContentAssistProposalWrapper> computeProposals(String prefix, final IContentAssistCalculator calculator) {
		final QuickFixContentAssistProposalRecorder recorder = new QuickFixContentAssistProposalRecorder();
		final IContentAssistContext context = createContext(node, attributeName, file, prefix);

		calculator.computeProposals(context, recorder);
		return recorder.getProposals();
	}

	abstract protected IContentAssistContext createContext(IDOMNode node, String attributeName, IFile file,
			String prefix);

	public Set<ContentAssistProposalWrapper> getClassAttributeProposals(String toBeMatched, boolean exactMatch) {
		return computeNameMatchedProposals(toBeMatched, exactMatch, toBeMatched, new ClassContentAssistCalculator());
	}

	// public Set<ContentAssistProposalWrapper>
	// getClassAttributeProposals(String toBeMatched, boolean exactMatch) {
	// return computeNameMatchedProposals(toBeMatched, exactMatch, toBeMatched,
	// new ClassContentAssistCalculator());
	// }
	//
	// public Set<ContentAssistProposalWrapper> getFactoryMethodProposals(String
	// toBeMatched, boolean exactMatch) {
	// return getMethodProposals(toBeMatched, exactMatch, new
	// FactoryMethodContentAssistCalculator());
	// }
	//
	// public Set<ContentAssistProposalWrapper>
	// getInitDestroyMethodProposals(String toBeMatched, boolean exactMatch) {
	// return getMethodProposals(toBeMatched, exactMatch, new
	// InitDestroyMethodContentAssistCalculator());
	// }
	//
	public Set<ContentAssistProposalWrapper> getMethodProposals(String toBeMatched, boolean exactMatch,
			IContentAssistCalculator calculator) {
		return computeNameMatchedProposals(toBeMatched, exactMatch, "", calculator);
	}

	public Set<ContentAssistProposalWrapper> getPropertyProposals(String toBeMatched, boolean exactMatch) {
		return computeNameMatchedProposals(toBeMatched, exactMatch, "", new PropertyNameContentAssistCalculator());
	}

	public Set<ContentAssistProposalWrapper> getReferenceableBeanDescriptions(String toBeMatched, boolean exactMatch) {
		return getMethodProposals(toBeMatched, exactMatch, new BeanReferenceContentAssistCalculator());
	}

	private boolean isMatched(String toBeMatched, String name, boolean exactMatch) {
		if (exactMatch) {
			return name.equals(toBeMatched);
		}

		return NameMatcher.isSimilarName(toBeMatched, name);
	}

}
