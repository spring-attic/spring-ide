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
package org.springframework.ide.eclipse.webflow.ui.editor.contentassist.webflow;

import org.eclipse.core.resources.IFile;
import org.eclipse.swt.graphics.Image;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.IContentAssistCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.IContentAssistContext;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.IContentAssistProposalRecorder;
import org.springframework.ide.eclipse.webflow.core.Activator;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowConfig;
import org.springframework.ide.eclipse.webflow.ui.editor.WebflowNamespaceUtils;
import org.springframework.ide.eclipse.webflow.ui.editor.outline.webflow.WebflowUIImages;

/**
 * {@link IContentAssistCalculator} that is used to propose flow references.
 * @author Christian Dupuis
 * @since 2.0.2
 */
public class SubflowReferenceContentAssistCalculator implements IContentAssistCalculator {

	private static final int RELEVANCE = 10;

	public void computeProposals(IContentAssistContext context,
			IContentAssistProposalRecorder recorder) {

		IFile file = context.getFile();
		for (String flowId : WebflowNamespaceUtils.getWebflowConfigNames()) {
			if (flowId.toLowerCase().startsWith(context.getMatchString().toLowerCase())) {
				acceptSearchMatch(recorder, flowId, file, context.getMatchString());
			}
		}
	}

	private void acceptSearchMatch(IContentAssistProposalRecorder recorder, String flowId,
			IFile file, String prefix) {
		IWebflowConfig config = Activator.getModel().getProject(file.getProject())
				.getConfig(flowId);
		String fileName = "";
		if (config != null) {
			fileName = config.getResource().getProjectRelativePath().toString();
		}
		String displayText = flowId + " - " + fileName;

		Image image = WebflowUIImages.getImage(WebflowUIImages.IMG_OBJS_WEBFLOW);

		recorder.recordProposal(image, RELEVANCE, displayText, flowId, config);
	}

}
