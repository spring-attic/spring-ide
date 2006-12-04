/*
 * Copyright 2002-2006 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.springframework.ide.eclipse.beans.ui.editor.contentassist;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion;
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion;
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegionList;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.eclipse.wst.xml.core.internal.regions.DOMRegionContext;
import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
import org.springframework.ide.eclipse.beans.ui.editor.DelegatingContentAssistProcessor;
import org.springframework.ide.eclipse.beans.ui.editor.templates.BeansTemplateCompletionProcessor;
import org.w3c.dom.Node;

/**
 * Main entry point for the Spring beans xml editor's content assist.
 */
public abstract class AbstractContentAssistProcessor implements
        INamespaceContentAssistProcessor {

    protected DelegatingContentAssistProcessor delegatingContextAssistProcessor;

    private BeansTemplateCompletionProcessor templateProcessor = null;

    public void addAttributeNameProposals(
            DelegatingContentAssistProcessor delegatingContextAssistProcessor,
            ContentAssistRequest request) {
        this.delegatingContextAssistProcessor = delegatingContextAssistProcessor;
        IDOMNode node = (IDOMNode) request.getNode();

        // Find the attribute region and name for which this position should
        // have a value proposed
        IStructuredDocumentRegion open = node
                .getFirstStructuredDocumentRegion();
        ITextRegionList openRegions = open.getRegions();
        int i = openRegions.indexOf(request.getRegion());
        if (i < 0) {
            return;
        }
        ITextRegion nameRegion = null;
        while (i >= 0) {
            nameRegion = openRegions.get(i--);
            if (nameRegion.getType() == DOMRegionContext.XML_TAG_ATTRIBUTE_NAME) {
                break;
            }
        }

        String matchString = request.getMatchString();
        if (matchString == null) {
            matchString = "";
        }
        if (matchString.length() > 0
                && (matchString.startsWith("\"") || matchString.startsWith("'"))) {
            matchString = matchString.substring(1);
        }

        // the name region is REQUIRED to do anything useful
        if (nameRegion != null) {
            String attributeName = open.getText(nameRegion);
            Node attribute = node.getAttributes().getNamedItem(attributeName);
            if (attribute != null) {
                String prefix = attribute.getPrefix();
                String namespace = attribute.getNamespaceURI();
                if (prefix != null) {
                    attributeName = attributeName
                            .substring(prefix.length() + 1);
                }
                computeAttributeNameProposals(request, attributeName,
                        namespace, prefix, node);
            }
        }
        // super.addAttributeNameProposals(request);
    }

    public void addAttributeValueProposals(
            DelegatingContentAssistProcessor delegatingContextAssistProcessor,
            ContentAssistRequest request) {
        this.delegatingContextAssistProcessor = delegatingContextAssistProcessor;
        IDOMNode node = (IDOMNode) request.getNode();

        // Find the attribute region and name for which this position should
        // have a value proposed
        IStructuredDocumentRegion open = node
                .getFirstStructuredDocumentRegion();
        ITextRegionList openRegions = open.getRegions();
        int i = openRegions.indexOf(request.getRegion());
        if (i < 0) {
            return;
        }
        ITextRegion nameRegion = null;
        while (i >= 0) {
            nameRegion = openRegions.get(i--);
            if (nameRegion.getType() == DOMRegionContext.XML_TAG_ATTRIBUTE_NAME) {
                break;
            }
        }

        String matchString = request.getMatchString();
        if (matchString == null) {
            matchString = "";
        }
        if (matchString.length() > 0
                && (matchString.startsWith("\"") || matchString.startsWith("'"))) {
            matchString = matchString.substring(1);
        }

        // the name region is REQUIRED to do anything useful
        if (nameRegion != null) {
            String attributeName = open.getText(nameRegion);
            computeAttributeValueProposals(request, node, matchString,
                    attributeName);
        }
    }

    public void addTagCloseProposals(
            DelegatingContentAssistProcessor delegatingContextAssistProcessor,
            ContentAssistRequest request) {
        this.delegatingContextAssistProcessor = delegatingContextAssistProcessor;
        // add content assist proposals for incomplete tags
        this.addAttributeValueProposals(delegatingContextAssistProcessor,
                request);
    }

    public void addTagInsertionProposals(
            DelegatingContentAssistProcessor delegatingContextAssistProcessor,
            ContentAssistRequest request, int childPosition) {
        this.delegatingContextAssistProcessor = delegatingContextAssistProcessor;
        IDOMNode node = (IDOMNode) request.getNode();
        computeTagInsertionProposals(request, node);
    }

    /**
     * Adds templates to the list of proposals
     * 
     * @param contentAssistRequest
     * @param context
     */
    protected void addTemplates(ContentAssistRequest contentAssistRequest,
            String context) {
        if (contentAssistRequest == null)
            return;

        // if already adding template proposals for a certain context type, do
        // not add again
        // if (!fTemplateContexts.contains(context)) {
        // fTemplateContexts.add(context);
        boolean useProposalList = !contentAssistRequest.shouldSeparate();

        if (getTemplateCompletionProcessor() != null) {
            getTemplateCompletionProcessor().setContextType(context);
            ICompletionProposal[] proposals = getTemplateCompletionProcessor()
                    .computeCompletionProposals(
                            this.delegatingContextAssistProcessor
                                    .getTextViewer(),
                            contentAssistRequest.getReplacementBeginPosition());
            for (int i = 0; i < proposals.length; ++i) {
                if (useProposalList)
                    contentAssistRequest.addProposal(proposals[i]);
                else
                    contentAssistRequest.addMacro(proposals[i]);
            }
        }
    }

    protected abstract void computeAttributeNameProposals(
            ContentAssistRequest request, String prefix, String namespace,
            String namespacePrefix, Node attributeNode);

    protected abstract void computeAttributeValueProposals(
            ContentAssistRequest request, IDOMNode node, String matchString,
            String attributeName);

    protected abstract void computeTagInsertionProposals(
            ContentAssistRequest request, IDOMNode node);

    /**
     * Returns project request is in
     * 
     * @param request
     * @return
     */
    protected IFile getResource(ContentAssistRequest request) {
        IResource resource = null;
        String baselocation = null;

        if (request != null) {
            IStructuredDocumentRegion region = request.getDocumentRegion();
            if (region != null) {
                IDocument document = region.getParentDocument();
                IStructuredModel model = null;
                try {
                    model = org.eclipse.wst.sse.core.StructuredModelManager
                            .getModelManager()
                            .getExistingModelForRead(document);
                    if (model != null) {
                        baselocation = model.getBaseLocation();
                    }
                }
                finally {
                    if (model != null) {
                        model.releaseFromRead();
                    }
                }
            }
        }

        if (baselocation != null) {
            // copied from JSPTranslationAdapter#getJavaProject
            IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
            IPath filePath = new Path(baselocation);
            if (filePath.segmentCount() > 0) {
                resource = root.getFile(filePath);
            }
        }
        return (IFile) resource;
    }

    protected BeansTemplateCompletionProcessor getTemplateCompletionProcessor() {
        if (templateProcessor == null) {
            templateProcessor = new BeansTemplateCompletionProcessor();
        }
        return templateProcessor;
    }
} 
