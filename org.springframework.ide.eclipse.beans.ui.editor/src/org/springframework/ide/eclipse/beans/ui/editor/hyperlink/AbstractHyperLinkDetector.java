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

package org.springframework.ide.eclipse.beans.ui.editor.hyperlink;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMAttr;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.springframework.ide.eclipse.core.StringUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Node;

/**
 * Detects hyperlinks in XML tags. Includes detection of bean classes and bean
 * properties in attribute values. Resolves bean references (including
 * references to parent beans or factory beans).
 * @author Christian Dupuis
 * @author Torsten Juergeleit
 */
@SuppressWarnings("restriction")
public abstract class AbstractHyperLinkDetector implements IHyperlinkDetector {

    public final IHyperlink[] detectHyperlinks(ITextViewer textViewer,
            IRegion region, boolean canShowMultipleHyperlinks) {
        if (region == null || textViewer == null) {
            return null;
        }

        IDocument document = textViewer.getDocument();
        Node currentNode = BeansEditorUtils.getNodeByOffset(document, region
                .getOffset());
        if (currentNode != null) {
            switch (currentNode.getNodeType()) {
                case Node.ELEMENT_NODE:
                    // at first try to handle selected attribute value
                    Attr currentAttr = BeansEditorUtils.getAttrByOffset(
                            currentNode, region.getOffset());
                    IDOMAttr attr = (IDOMAttr) currentAttr;
                    if (currentAttr != null
                            && region.getOffset() >= attr
                                    .getValueRegionStartOffset()) {
                        if (isLinkableAttr(currentAttr)) {
                            IRegion hyperlinkRegion = getHyperlinkRegion(currentAttr);
                            IHyperlink hyperLink = createHyperlink(currentAttr
                                    .getName(), currentAttr.getNodeValue(),
                                    currentNode.getParentNode(),
                                    hyperlinkRegion, document, currentNode,
                                    textViewer, region);
                            if (hyperLink != null) {
                                return new IHyperlink[] { hyperLink };
                            }
                        }
                    }
                    break;

                case Node.TEXT_NODE:
                    IRegion hyperlinkRegion = getHyperlinkRegion(currentNode);
                    Node parentNode = currentNode.getParentNode();
                    if (parentNode != null) {
                        IHyperlink hyperLink = createHyperlink(parentNode
                                .getNodeName(), currentNode.getNodeValue(),
                                parentNode, hyperlinkRegion, document,
                                currentNode, textViewer, region);
                        if (hyperLink != null) {
                            return new IHyperlink[] { hyperLink };
                        }
                    }
                    break;
            }
        }
        return null;
    }

    /**
     * Returns the text region of given node.
     */
    protected final IRegion getHyperlinkRegion(Node node) {
        if (node != null) {
            switch (node.getNodeType()) {
                case Node.DOCUMENT_TYPE_NODE:
                case Node.TEXT_NODE:
                    IDOMNode docNode = (IDOMNode) node;
                    return new Region(docNode.getStartOffset(), docNode
                            .getEndOffset()
                            - docNode.getStartOffset());

                case Node.ELEMENT_NODE:
                    IDOMElement element = (IDOMElement) node;
                    int endOffset;
                    if (element.hasEndTag() && element.isClosed()) {
                        endOffset = element.getStartEndOffset();
                    }
                    else {
                        endOffset = element.getEndOffset();
                    }
                    return new Region(element.getStartOffset(), endOffset
                            - element.getStartOffset());

                case Node.ATTRIBUTE_NODE:
                    IDOMAttr att = (IDOMAttr) node;
                    // do not include quotes in attribute value region
                    int regOffset = att.getValueRegionStartOffset();
                    int regLength = att.getValueRegionText().length();
                    String attValue = att.getValueRegionText();
                    if (StringUtils.isQuoted(attValue)) {
                        regOffset += 1;
                        regLength = regLength - 2;
                    }
                    return new Region(regOffset, regLength);
            }
        }
        return null;
    }

    /**
     * Returns <code>true</code> if given attribute is openable.
     */
    protected abstract boolean isLinkableAttr(Attr attr);

    protected abstract IHyperlink createHyperlink(String name, String target,
            Node parentNode, IRegion hyperlinkRegion, IDocument document,
            Node node, ITextViewer textViewer, IRegion cursor);

    
}
