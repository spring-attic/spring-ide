/*******************************************************************************
 * Copyright (c) 2007, 2008 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.editor.contentassist.aop;

import org.eclipse.core.resources.IFile;
import org.eclipse.swt.graphics.Image;
import org.springframework.ide.eclipse.beans.ui.BeansUIImages;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.IContentAssistCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.IContentAssistContext;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.IContentAssistProposalRecorder;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * {@link IContentAssistCalculator} implementation that calculates content assist proposals for
 * <code>pointcut-ref</code> attributes.
 * @author Christian Dupuis
 * @since 2.0.2
 */
public class PointcutReferenceContentAssistCalculator implements IContentAssistCalculator {

	private static final int RELEVANCE = 20;

	public void computeProposals(IContentAssistContext context,
			IContentAssistProposalRecorder recorder) {
		addPointcutReferenceProposals(context, recorder);
	}

	private void addPointcutReferenceProposals(IContentAssistContext context,
			IContentAssistProposalRecorder recorder) {
		String prefix = context.getMatchString();
		if (prefix == null) {
			prefix = "";
		}
		IFile file = context.getFile();
		if (context.getDocument() != null) {
			searchPointcutElements(recorder, prefix, context.getParentNode(), file);
			searchPointcutElements(recorder, prefix, context.getParentNode().getParentNode(), file);
		}
	}

	private void searchPointcutElements(IContentAssistProposalRecorder recorder, String prefix,
			Node node, IFile file) {
		NodeList children = node.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			if ("pointcut".equals(child.getLocalName())) {
				NamedNodeMap attributes = child.getAttributes();
				Node idAttribute = attributes.getNamedItem("id");
				if (idAttribute != null && idAttribute.getNodeValue() != null
						&& idAttribute.getNodeValue().startsWith(prefix)) {
					acceptSearchMatch(recorder, child, file);
				}
			}
		}
	}

	public void acceptSearchMatch(IContentAssistProposalRecorder recorder, Node pointcutNode,
			IFile file) {

		NamedNodeMap attributes = pointcutNode.getAttributes();
		Node idAttribute = attributes.getNamedItem("id");
		Node parentNode = pointcutNode.getParentNode();

		String pointcutName = idAttribute.getNodeValue();
		String replaceText = pointcutName;
		String fileName = file.getProjectRelativePath().toString();

		StringBuilder buf = new StringBuilder();
		buf.append(pointcutName);

		if (parentNode != null) {
			buf.append(" [");
			buf.append(parentNode.getNodeName());
			buf.append("]");
		}
		if (fileName != null) {
			buf.append(" - ");
			buf.append(fileName);
		}

		String displayText = buf.toString();
		Image image = BeansUIImages.getImage(BeansUIImages.IMG_OBJS_POINTCUT);
		
		recorder.recordProposal(image, RELEVANCE, displayText, replaceText, pointcutName);
	}
}
