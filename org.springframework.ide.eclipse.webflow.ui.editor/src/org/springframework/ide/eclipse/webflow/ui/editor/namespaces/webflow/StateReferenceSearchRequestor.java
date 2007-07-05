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
package org.springframework.ide.eclipse.webflow.ui.editor.namespaces.webflow;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.BeansJavaCompletionProposal;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.w3c.dom.Node;

/**
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public class StateReferenceSearchRequestor {

	public static final int RELEVANCE = 10;

	protected Set<String> beans;

	protected ContentAssistRequest request;
	
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

	public StateReferenceSearchRequestor(ContentAssistRequest request) {
		this.request = request;
		this.beans = new HashSet<String>();
	}

	public void acceptSearchMatch(Node node, IFile file, String prefix) {
		String id = BeansEditorUtils.getAttribute(node, "id");
		if (!beans.contains(id) && node != null && id != null && id.toLowerCase().startsWith(prefix.toLowerCase()) && VALID_NODE_NAMES.contains(node.getLocalName())) {
			String fileName = file.getProjectRelativePath().toString();
			String displayText = id + " - " + fileName;
			Image image = labelProvider.getImage(node);
			BeansJavaCompletionProposal proposal = new BeansJavaCompletionProposal(id, request.getReplacementBeginPosition(),
					request.getReplacementLength(), id.length(), image, displayText, null,
					StateReferenceSearchRequestor.RELEVANCE, node);
			request.addProposal(proposal);
			beans.add(id);
		}
	}
}
