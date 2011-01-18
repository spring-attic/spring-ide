/*******************************************************************************
 * Copyright (c) 2009 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.osgi.ui.editor.contentassist.osgi;

import java.util.Set;

import org.eclipse.jdt.core.IType;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.AbstractIdContentAssistCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.IContentAssistCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.IContentAssistContext;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.IContentAssistProposalRecorder;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.springframework.ide.eclipse.core.java.Introspector;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * {@link IContentAssistCalculator} for the 'osgi:reference@id' attribute.
 * @author Christian Dupuis
 * @since 2.2.5
 */
public class ReferenceIdContentAssistCalculator extends AbstractIdContentAssistCalculator {

	public void computeProposals(IContentAssistContext context, IContentAssistProposalRecorder recorder) {
		addBeanIdProposal(context, recorder);
	}

	/**
	 * Looks for 'reference@interface' attributes and interfaces sub-elemet to create id content assist proposals.
	 */
	private void addBeanIdProposal(IContentAssistContext context, IContentAssistProposalRecorder recorder) {
		// Add interface proposals from 'interface' attribute
		createProposalsForClassName(context, recorder, BeansEditorUtils.getAttribute(context.getNode(), "interface"));
		// Add interface proposals from 'interfaces' sub-element
		NodeList children = context.getNode().getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			if ("interfaces".equals(child.getLocalName())) {
				NodeList valueChildren = child.getChildNodes();
				for (int j = 0; j < valueChildren.getLength(); j++) {
					Node valueChild = valueChildren.item(j);
					if ("value".equals(valueChild.getLocalName())) {
						NodeList textChildren = valueChild.getChildNodes();
						for (int k = 0; k < textChildren.getLength(); k++) {
							createProposalsForClassName(context, recorder, textChildren.item(k).getNodeValue());
						}
					}
				}
			}
		}
	}

	/**
	 * Creates id proposals for a given classname. That is for the class itself and all super interfaces.
	 */
	private void createProposalsForClassName(IContentAssistContext context, IContentAssistProposalRecorder recorder,
			String className) {
		IType type = JdtUtils.getJavaType(context.getFile().getProject(), className);
		if (type != null) {
			createBeanIdProposals(context, recorder, type.getFullyQualifiedName());
			Set<IType> allInterfaces = Introspector.getAllImplementedInterfaces(type);
			for (IType interf : allInterfaces) {
				createBeanIdProposals(context, recorder, interf.getFullyQualifiedName());
			}
		}
	}
}
