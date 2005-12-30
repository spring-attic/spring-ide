/*
 * Copyright 2002-2005 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.springframework.ide.eclipse.beans.ui.editor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredPartitionTypes;
import org.eclipse.wst.xml.core.internal.provisional.text.IXMLPartitions;
import org.eclipse.wst.xml.ui.StructuredTextViewerConfigurationXML;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.BeansContentAssistProcessor;
import org.springframework.ide.eclipse.beans.ui.editor.hover.BeansTextHoverProcessor;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.BeansHyperLinkDetector;

public class BeansStructuredTextViewerConfiguration extends
		StructuredTextViewerConfigurationXML {

	public IContentAssistProcessor[] getContentAssistProcessors(
			ISourceViewer sourceViewer, String partitionType) {

		IContentAssistProcessor[] processors;

		if (partitionType == IStructuredPartitionTypes.DEFAULT_PARTITION
				|| partitionType == IXMLPartitions.XML_DEFAULT) {
			processors = new IContentAssistProcessor[] { new BeansContentAssistProcessor() };
		} else {
			processors = super.getContentAssistProcessors(sourceViewer, partitionType);
		}

		// Modify the behaviour of this configuration's content assist
		// TODO Is this the right approach?
		IContentAssistant ca = super.getContentAssistant(sourceViewer);
		if (ca != null && ca instanceof ContentAssistant) {
			ContentAssistant contentAssistant = (ContentAssistant) ca;
			contentAssistant.enableAutoActivation(true);
			contentAssistant.setAutoActivationDelay(0);
			contentAssistant.setProposalSelectorBackground(new Color(
					BeansEditorPlugin.getActiveWorkbenchShell().getDisplay(),
					new RGB(255, 255, 255)));
			contentAssistant.setRestoreCompletionProposalSize(BeansEditorPlugin
					.getDefault().getDialogSettings());
		}
		return processors;
	}

	public IHyperlinkDetector[] getHyperlinkDetectors(ISourceViewer sourceViewer) {
		List allDetectors = new ArrayList(0);
		allDetectors.add(new BeansHyperLinkDetector());
		IHyperlinkDetector[] superDetectors = super
				.getHyperlinkDetectors(sourceViewer);
		for (int m = 0; m < superDetectors.length; m++) {
			IHyperlinkDetector detector = superDetectors[m];
			if (!allDetectors.contains(detector)) {
				allDetectors.add(detector);
			}
		}
		return (IHyperlinkDetector[]) allDetectors
				.toArray(new IHyperlinkDetector[0]);
	}

	public ITextHover getTextHover(ISourceViewer sourceViewer,
			String contentType, int stateMask) {
		if ((contentType == IStructuredPartitionTypes.DEFAULT_PARTITION)
				|| (contentType == IXMLPartitions.XML_DEFAULT)) {
			/*hoverDescs = super.get
			int i = 0;
			while (i < hoverDescs.length) {
				if (hoverDescs[i].isEnabled()
						&& EditorUtility.computeStateMask(hoverDescs[i]
								.getModifierString()) == stateMask) {
					String hoverType = hoverDescs[i].getId();
					if (TextHoverManager.COMBINATION_HOVER
							.equalsIgnoreCase(hoverType))
						return new BeansTextHoverProcessor(this.editorPart);
				}
				i++;
			}*/
			return new BeansTextHoverProcessor();
		}
		return super.getTextHover(sourceViewer, contentType, stateMask);
	}
}
