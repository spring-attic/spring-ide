/*******************************************************************************
 * Copyright (c) 2005, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.webflow.ui.editor.contentassist.webflow;

import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.graphics.Image;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.IContentAssistCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.IContentAssistContext;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.IContentAssistProposalRecorder;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.springframework.ide.eclipse.webflow.ui.editor.WebflowNamespaceUtils;
import org.springframework.ide.eclipse.webflow.ui.editor.outline.webflow.WebflowOutlineLabelProvider;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * {@link IContentAssistCalculator} that is used to propose state references.
 * @author Christian Dupuis
 * @since 2.0.2
 */
public class StateReferenceContentAssistCalculator implements IContentAssistCalculator {

	public static final int RELEVANCE = 10;

	private static final Set<String> VALID_NODE_NAMES;

	private static final ILabelProvider labelProvider = new WebflowOutlineLabelProvider();

	static {
		VALID_NODE_NAMES = new LinkedHashSet<String>();
		VALID_NODE_NAMES.add("action-state");
		VALID_NODE_NAMES.add("decision-state");
		VALID_NODE_NAMES.add("subflow-state");
		VALID_NODE_NAMES.add("end-state");
		VALID_NODE_NAMES.add("view-state");
		VALID_NODE_NAMES.add("inline-flow");
	}

	public void acceptSearchMatch(IContentAssistProposalRecorder recorder, Node node, String id,
			IFile file, String prefix) {
		String fileName = file.getProjectRelativePath().toString();
		String displayText = id + " - " + fileName;

		Image image = labelProvider.getImage(node);
		recorder.recordProposal(image, RELEVANCE, displayText, id, node);
	}

	public void computeProposals(IContentAssistContext context,
			IContentAssistProposalRecorder recorder) {

		Node flowNode = WebflowNamespaceUtils.locateFlowRootNode(context.getNode());
		NodeList nodes = flowNode.getChildNodes();
		if (nodes.getLength() > 0) {
			IFile file = context.getFile();
			for (int i = 0; i < nodes.getLength(); i++) {
				Node node = nodes.item(i);
				String id = BeansEditorUtils.getAttribute(node, "id");
				if (node != null && id != null
						&& id.toLowerCase().startsWith(context.getMatchString().toLowerCase())
						&& VALID_NODE_NAMES.contains(node.getLocalName())) {
					acceptSearchMatch(recorder, nodes.item(i), id, file, context.getMatchString());
				}
			}
		}
	}
}
