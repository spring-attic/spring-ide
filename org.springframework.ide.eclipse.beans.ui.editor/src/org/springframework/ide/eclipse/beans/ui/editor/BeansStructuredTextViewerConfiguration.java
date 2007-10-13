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
package org.springframework.ide.eclipse.beans.ui.editor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;
import org.eclipse.wst.sse.core.text.IStructuredPartitions;
import org.eclipse.wst.xml.core.text.IXMLPartitions;
import org.eclipse.wst.xml.ui.StructuredTextViewerConfigurationXML;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.DelegatingContentAssistProcessor;
import org.springframework.ide.eclipse.beans.ui.editor.hover.BeansTextHoverProcessor;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.DelegatingHyperlinkDetector_;

public class BeansStructuredTextViewerConfiguration extends
		StructuredTextViewerConfigurationXML {

	@Override
	public IContentAssistProcessor[] getContentAssistProcessors(
			ISourceViewer sourceViewer, String partitionType) {

		IContentAssistProcessor[] processors;

		if (partitionType == IStructuredPartitions.DEFAULT_PARTITION
				|| partitionType == IXMLPartitions.XML_DEFAULT) {
			processors = new IContentAssistProcessor[] { new DelegatingContentAssistProcessor() };
		}
		else {
			processors = super.getContentAssistProcessors(sourceViewer,
					partitionType);
		}
		return processors;
	}

	@Override
	public IHyperlinkDetector[] getHyperlinkDetectors(ISourceViewer sourceViewer) {
		if (sourceViewer == null
				|| !fPreferenceStore
						.getBoolean(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_HYPERLINKS_ENABLED))
			return null;

		List<IHyperlinkDetector> allDetectors = new ArrayList<IHyperlinkDetector>();
		allDetectors.add(new DelegatingHyperlinkDetector_());
		IHyperlinkDetector[] superDetectors = super
				.getHyperlinkDetectors(sourceViewer);
		for (IHyperlinkDetector detector : superDetectors) {
			if (!allDetectors.contains(detector)) {
				allDetectors.add(detector);
			}
		}
		return allDetectors.toArray(new IHyperlinkDetector[0]);
	}

	@Override
	public ITextHover getTextHover(ISourceViewer sourceViewer,
			String contentType, int stateMask) {
		if ((contentType == IStructuredPartitions.DEFAULT_PARTITION)
				|| (contentType == IXMLPartitions.XML_DEFAULT)) {
			return new BeansTextHoverProcessor();
		}
		return super.getTextHover(sourceViewer, contentType, stateMask);
	}
}
