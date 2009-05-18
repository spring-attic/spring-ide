/*******************************************************************************
 * Copyright (c) 2005, 2009 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.batch.ui.editor.contentassist.batch;

import org.eclipse.core.resources.IFile;
import org.eclipse.swt.graphics.Image;
import org.springframework.ide.eclipse.batch.BatchUIImages;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.IContentAssistCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.IContentAssistContext;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.IContentAssistProposalRecorder;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * {@link IContentAssistCalculator} implementation that can be used to locate references to
 * <code>step<step> elements.
 * @author Leo Dos Santos
 * @since 2.2.5
 */
public class StepReferenceContentAssistCalculator implements IContentAssistCalculator {

	private static final int RELEVANCE = 20;

	public void computeProposals(IContentAssistContext context,
			IContentAssistProposalRecorder recorder) {
		String prefix = context.getMatchString();
		if (prefix == null) {
			prefix = "";
		}
		IFile file = context.getFile();
		if (context.getDocument() != null && context.getDocument().getDocumentElement() != null) {
			searchStepElements(recorder, prefix, context.getDocument().getDocumentElement(), file);
		}
	}

	private void searchStepElements(IContentAssistProposalRecorder recorder, String prefix,
			Node node, IFile file) {
		NodeList children = node.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			if ("step".equals(child.getLocalName())) {
				NamedNodeMap attributes = child.getAttributes();
				Node id = attributes.getNamedItem("id");
				if (id != null && id.getNodeValue() != null && id.getNodeValue().startsWith(prefix)) {
					acceptStepMatch(recorder, child, file);
				}
			}
			if (child.hasChildNodes()) {
				searchStepElements(recorder, prefix, child, file);
			}
		}
	}

	private void acceptStepMatch(IContentAssistProposalRecorder recorder, Node stepNode, IFile file) {
		NamedNodeMap attrs = stepNode.getAttributes();
		Node id = attrs.getNamedItem("id");

		String stepName = id.getNodeValue();
		String replaceText = stepName;
		String fileName = file.getProjectRelativePath().toString();
		StringBuilder buf = new StringBuilder();
		buf.append(stepName);

		if (fileName != null) {
			buf.append(" - ");
			buf.append(fileName);
		}

		String displayText = buf.toString();
		Image image = BatchUIImages.getImage(BatchUIImages.IMG_OBJS_BATCH);
		recorder.recordProposal(image, RELEVANCE, displayText, replaceText, stepName);
	}

}
