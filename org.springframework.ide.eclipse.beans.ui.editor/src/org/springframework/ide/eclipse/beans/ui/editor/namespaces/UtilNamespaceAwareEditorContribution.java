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

package org.springframework.ide.eclipse.beans.ui.editor.namespaces;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.wst.xml.ui.internal.contentoutline.JFaceNodeLabelProvider;
import org.springframework.ide.eclipse.beans.ui.editor.BeansEditorUtils;
import org.springframework.ide.eclipse.beans.ui.editor.INamespaceAwareEditorContribution;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.INamespaceContentAssistProcessor;
import org.springframework.ide.eclipse.beans.ui.editor.outline.BeansContentOutlineConfiguration;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class UtilNamespaceAwareEditorContribution implements
        INamespaceAwareEditorContribution {

    public String getNamespaceURI() {
        return "http://www.springframework.org/schema/util";
    }

    public JFaceNodeLabelProvider getLabelProvider(
            BeansContentOutlineConfiguration configuration,
            ILabelProvider parent) {
        return null;
    }

    public INamespaceContentAssistProcessor getContentAssistProcessor() {
        return null;
    }

    public BeansHyperLinkDetector getHyperLinkDetector() {
        return null;
    }

    public Map<String, Node> getReferenceableElements(Document document) {
        Map<String, Node> nodes = new HashMap<String, Node>();
        NodeList childNodes = document.getDocumentElement().getChildNodes();

        for (int i = 0; i < childNodes.getLength(); i++) {
            Node node = childNodes.item(i);
            if (getNamespaceURI().equals(node.getNamespaceURI())
                    && BeansEditorUtils.hasAttribute(node, "id")) {
                nodes.put(BeansEditorUtils.getAttribute(node, "id"), node);
            }
        }
        return nodes;
    }
}
