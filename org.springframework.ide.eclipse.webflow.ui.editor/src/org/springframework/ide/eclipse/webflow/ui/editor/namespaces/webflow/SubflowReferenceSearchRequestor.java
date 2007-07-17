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
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.swt.graphics.Image;
import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.BeansJavaCompletionProposal;
import org.springframework.ide.eclipse.webflow.core.Activator;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowConfig;

/**
 * Search requestor that is used to check if a certain sub flow can be used as
 * reference proposal.
 * @author Christian Dupuis
 * @since 2.0.1
 */
@SuppressWarnings("restriction")
public class SubflowReferenceSearchRequestor {

	public static final int RELEVANCE = 10;

	protected Set<String> flows;

	protected ContentAssistRequest request;

	public SubflowReferenceSearchRequestor(ContentAssistRequest request) {
		this.request = request;
		this.flows = new HashSet<String>();
	}

	public void acceptSearchMatch(String flowId, IFile file, String prefix) {
		if (!flows.contains(flowId) && flowId != null
				&& flowId.toLowerCase().startsWith(prefix.toLowerCase())) {
			IWebflowConfig config = Activator.getModel().getProject(
					file.getProject()).getConfig(flowId);
			String fileName = "";
			if (config != null) {
				fileName = config.getResource().getProjectRelativePath()
						.toString();
			}
			String displayText = flowId + " - " + fileName;
			Image image = WebflowUIImages
					.getImage(WebflowUIImages.IMG_OBJS_WEBFLOW);
			BeansJavaCompletionProposal proposal = new BeansJavaCompletionProposal(
					flowId, request.getReplacementBeginPosition(), request
							.getReplacementLength(), flowId.length(), image,
					displayText, null,
					SubflowReferenceSearchRequestor.RELEVANCE, config);
			request.addProposal(proposal);
			flows.add(flowId);
		}
	}
}
