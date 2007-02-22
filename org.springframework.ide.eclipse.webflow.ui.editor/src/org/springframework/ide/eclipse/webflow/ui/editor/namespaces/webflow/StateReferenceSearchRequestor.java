/*
 * Copyright 2002-2007 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
					null, StateReferenceSearchRequestor.RELEVANCE);
			request.addProposal(proposal);
			beans.add(id);
		}
	}
}