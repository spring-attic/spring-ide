package org.springframework.ide.eclipse.beans.ui.editor;

import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredPartitionTypes;
import org.eclipse.wst.xml.core.internal.provisional.text.IXMLPartitions;
import org.eclipse.wst.xml.ui.internal.contentassist.NoRegionContentAssistProcessor;
import org.eclipse.wst.xml.ui.internal.provisional.StructuredTextViewerConfigurationXML;

public class BeansStructuredTextViewerConfiguration
								 extends StructuredTextViewerConfigurationXML {
	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
		IContentAssistant ca = super.getContentAssistant(sourceViewer);
		if (ca != null && ca instanceof ContentAssistant) {
			ContentAssistant contentAssistant = (ContentAssistant) ca;
			IContentAssistProcessor caProcessor =
							  new BeansContentAssistProcessor(getEditorPart());
			IContentAssistProcessor noRegionProcessor = new NoRegionContentAssistProcessor();
			setContentAssistProcessor(contentAssistant, caProcessor,
								  IStructuredPartitionTypes.DEFAULT_PARTITION);
			setContentAssistProcessor(contentAssistant, caProcessor,
									  IXMLPartitions.XML_DEFAULT);
			setContentAssistProcessor(contentAssistant, noRegionProcessor,
								  IStructuredPartitionTypes.UNKNOWN_PARTITION);
		}
		return ca;
	}
}
