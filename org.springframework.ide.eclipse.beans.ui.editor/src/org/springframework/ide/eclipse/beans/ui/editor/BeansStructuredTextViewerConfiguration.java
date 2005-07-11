package org.springframework.ide.eclipse.beans.ui.editor;

import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
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
            contentAssistant.enableAutoActivation(true);
            contentAssistant.enableAutoInsert(true);
            contentAssistant.setAutoActivationDelay(100);
            contentAssistant.setProposalSelectorBackground(new Color(BeansEditorPlugin
                    .getActiveWorkbenchShell().getDisplay(), new RGB(255, 255, 255)));
            IContentAssistProcessor caProcessor = new BeansContentAssistProcessor(getEditorPart());
            
            setContentAssistProcessor(contentAssistant, caProcessor,
                    IStructuredPartitionTypes.DEFAULT_PARTITION);
            setContentAssistProcessor(contentAssistant, caProcessor, IXMLPartitions.XML_DEFAULT);
            IContentAssistProcessor noRegionProcessor = new NoRegionContentAssistProcessor();
            setContentAssistProcessor(contentAssistant, noRegionProcessor,
                    IStructuredPartitionTypes.UNKNOWN_PARTITION);
        }
        return ca;
    }
}
