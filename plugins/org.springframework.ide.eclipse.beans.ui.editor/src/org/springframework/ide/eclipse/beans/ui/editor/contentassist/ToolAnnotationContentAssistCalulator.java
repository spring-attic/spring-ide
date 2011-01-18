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
package org.springframework.ide.eclipse.beans.ui.editor.contentassist;

import java.util.List;

import org.springframework.ide.eclipse.beans.core.namespaces.ToolAnnotationUtils;
import org.springframework.ide.eclipse.beans.ui.editor.namespaces.NamespaceUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * {@link IContentAssistCalculator} that uses the Spring tool annotations to create content proposals.
 * @author Christian Dupuis
 * @since 2.3.0
 */
public class ToolAnnotationContentAssistCalulator implements IContentAssistCalculator {

	/**
	 * {@inheritDoc}
	 */
	public void computeProposals(IContentAssistContext context, IContentAssistProposalRecorder recorder) {
		addAnnotationBasedAttributeValueProposals(context, recorder);
	}

	private void addAnnotationBasedAttributeValueProposals(IContentAssistContext context,
			IContentAssistProposalRecorder recorder) {
		String attributeName = context.getAttributeName();
		List<Element> appInfo = ToolAnnotationUtils.getApplicationInformationElements(context.getNode(), attributeName);
		for (Element elem : appInfo) {
			NodeList children = elem.getChildNodes();
			for (int j = 0; j < children.getLength(); j++) {
				Node annotationNode = children.item(j);
				if (annotationNode.getNodeType() == Node.ELEMENT_NODE) {
					invokeAnnotationBasedContentAssistProcessor(context, recorder, annotationNode);
				}
			}
		}
	}

	private void invokeAnnotationBasedContentAssistProcessor(IContentAssistContext context,
			IContentAssistProposalRecorder recorder, Node annotationNode) {

		IAnnotationBasedContentAssistProcessor[] annotationProcessors = NamespaceUtils
				.getAnnotationBasedContentAssistProcessor(annotationNode.getNamespaceURI());
		for (IAnnotationBasedContentAssistProcessor annotationProcessor : annotationProcessors) {
			annotationProcessor.addAttributeValueProposals(context, recorder, annotationNode);
		}
	}

}
