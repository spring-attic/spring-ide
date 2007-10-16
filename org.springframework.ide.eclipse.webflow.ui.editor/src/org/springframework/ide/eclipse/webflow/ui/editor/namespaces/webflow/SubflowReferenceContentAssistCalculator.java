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

import org.eclipse.core.resources.IFile;
import org.eclipse.swt.graphics.Image;
import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.BeansJavaCompletionProposal;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.IContentAssistCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.springframework.ide.eclipse.webflow.core.Activator;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowConfig;

/**
 * {@link IContentAssistCalculator} that is used to propose flow references.
 * @author Christian Dupuis
 * @since 2.0.2
 */
@SuppressWarnings("restriction")
public class SubflowReferenceContentAssistCalculator implements
		IContentAssistCalculator {

	private static final int RELEVANCE = 10;

	public void computeProposals(ContentAssistRequest request,
			String matchString, String attributeName, String namespace,
			String namepacePrefix) {

		IFile file = BeansEditorUtils.getFile(request);
		for (String flowId : WebflowNamespaceUtils.getWebflowConfigNames()) {
			if (flowId.toLowerCase().startsWith(matchString.toLowerCase())) {
				acceptSearchMatch(request, flowId, file, matchString);
			}
		}
	}

	private void acceptSearchMatch(ContentAssistRequest request, String flowId,
			IFile file, String prefix) {
		IWebflowConfig config = Activator.getModel().getProject(
				file.getProject()).getConfig(flowId);
		String fileName = "";
		if (config != null) {
			fileName = config.getResource().getProjectRelativePath().toString();
		}
		String displayText = flowId + " - " + fileName;

		Image image = WebflowUIImages
				.getImage(WebflowUIImages.IMG_OBJS_WEBFLOW);
		BeansJavaCompletionProposal proposal = new BeansJavaCompletionProposal(
				flowId, request.getReplacementBeginPosition(), request
						.getReplacementLength(), flowId.length(), image,
				displayText, null, RELEVANCE, config);

		request.addProposal(proposal);
	}
}
