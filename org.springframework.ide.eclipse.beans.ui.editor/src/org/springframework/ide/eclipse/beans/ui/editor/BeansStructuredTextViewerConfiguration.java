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
import org.eclipse.wst.sse.ui.internal.taginfo.AnnotationHoverProcessor;
import org.eclipse.wst.sse.ui.internal.taginfo.ProblemAnnotationHoverProcessor;
import org.eclipse.wst.sse.ui.internal.taginfo.TextHoverManager;
import org.eclipse.wst.sse.ui.internal.util.EditorUtility;
import org.eclipse.wst.xml.core.internal.provisional.text.IXMLPartitions;
import org.eclipse.wst.xml.ui.internal.contentassist.NoRegionContentAssistProcessor;
import org.eclipse.wst.xml.ui.internal.provisional.StructuredTextViewerConfigurationXML;
import org.eclipse.wst.xml.ui.internal.taginfo.XMLBestMatchHoverProcessor;
import org.eclipse.wst.xml.ui.internal.taginfo.XMLTagInfoHoverProcessor;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.BeansContentAssistProcessor;
import org.springframework.ide.eclipse.beans.ui.editor.hover.BeansTextHoverProcessor;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.BeansHyperLinkDetector;

public class BeansStructuredTextViewerConfiguration
        extends StructuredTextViewerConfigurationXML {

    public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {

        IContentAssistant ca = super.getContentAssistant(sourceViewer);
        if (ca != null && ca instanceof ContentAssistant) {
            ContentAssistant contentAssistant = (ContentAssistant) ca;
            contentAssistant.enableAutoActivation(true);
            contentAssistant.enableAutoInsert(true);
            contentAssistant.setAutoActivationDelay(0);
            contentAssistant.setProposalSelectorBackground(new Color(BeansEditorPlugin
                    .getActiveWorkbenchShell().getDisplay(), new RGB(255, 255, 255)));
            contentAssistant.setRestoreCompletionProposalSize(BeansEditorPlugin.getDefault()
                    .getDialogSettings());
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

    public IHyperlinkDetector[] getHyperlinkDetectors(ISourceViewer sourceViewer) {
        List allDetectors = new ArrayList(0);
        allDetectors.add(new BeansHyperLinkDetector(getEditorPart()));

        IHyperlinkDetector[] superDetectors = super.getHyperlinkDetectors(sourceViewer);
        for (int m = 0; m < superDetectors.length; m++) {
            IHyperlinkDetector detector = superDetectors[m];
            if (!allDetectors.contains(detector)) {
                allDetectors.add(detector);
            }
        }
        return (IHyperlinkDetector[]) allDetectors.toArray(new IHyperlinkDetector[0]);
    }

    public ITextHover getTextHover(ISourceViewer sourceViewer, String contentType, int stateMask) {
        // look for appropriate text hover processor to return based on
        // content type and state mask
        if ((contentType == IStructuredPartitionTypes.DEFAULT_PARTITION)
                || (contentType == IXMLPartitions.XML_DEFAULT)) {
            // check which of xml's text hover is handling stateMask
            TextHoverManager.TextHoverDescriptor[] hoverDescs = getTextHovers();
            int i = 0;
            while (i < hoverDescs.length) {
                if (hoverDescs[i].isEnabled()
                        && EditorUtility.computeStateMask(hoverDescs[i].getModifierString()) == stateMask) {
                    String hoverType = hoverDescs[i].getId();
                    if (TextHoverManager.COMBINATION_HOVER.equalsIgnoreCase(hoverType))
                        return new BeansTextHoverProcessor(this.editorPart);
                }
                i++;
            }
        }
        return super.getTextHover(sourceViewer, contentType, stateMask);
    }
}
