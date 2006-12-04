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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
import org.springframework.ide.eclipse.beans.core.internal.Introspector;
import org.springframework.ide.eclipse.beans.core.internal.Introspector.Statics;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.ui.editor.BeansEditorUtils;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.AbstractContentAssistProcessor;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.INamespaceContentAssistProcessor;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.requestor.BeanReferenceSearchRequestor;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.requestor.PointcutReferenceSearchRequestor;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.requestor.PublicMethodSearchRequestor;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Main entry point for the Spring beans xml editor's content assist.
 */
public class AopContentAssistProcessor
        extends AbstractContentAssistProcessor implements
        INamespaceContentAssistProcessor {

    private void addBeanReferenceProposals(ContentAssistRequest request,
            String prefix, Document document, boolean showExternal) {
        if (prefix == null) {
            prefix = "";
        }
        IFile file = getResource(request);
        if (document != null) {
            BeanReferenceSearchRequestor requestor = new BeanReferenceSearchRequestor(
                    request);
            Map<String, Node> beanNodes = BeansEditorUtils.getReferenceableNodes(document);
            for (Map.Entry<String, Node> node : beanNodes.entrySet()) {
                Node beanNode = node.getValue();
                requestor.acceptSearchMatch(node.getKey(), beanNode, file, prefix);
            }
            if (showExternal) {
                List beans = BeansEditorUtils.getBeansFromConfigSets(file);
                for (int i = 0; i < beans.size(); i++) {
                    IBean bean = (IBean) beans.get(i);
                    requestor.acceptSearchMatch(bean, file, prefix);
                }
            }
        }
    }

    protected void computeAttributeValueProposals(ContentAssistRequest request,
            IDOMNode node, String matchString, String attributeName) {

        String nodeName = node.getNodeName();
        String prefix = node.getPrefix();
        if (prefix != null) {
            nodeName = nodeName.substring(prefix.length() + 1);
        }

        if ("aspect".equals(nodeName)) {
            if ("ref".equals(attributeName)) {
                addBeanReferenceProposals(request, matchString, node
                        .getOwnerDocument(), true);
            }
        }
        else if ("advisor".equals(nodeName)) {
            if ("advice-ref".equals(attributeName)) {
                addBeanReferenceProposals(request, matchString, node
                        .getOwnerDocument(), true);
            }
        }
        if ("pointcut-ref".equals(attributeName)) {
            addPointcutReferenceProposals(request, matchString, node, node
                    .getOwnerDocument());
        }
        if ("method".equals(attributeName)
                && "aspect".equals(node.getParentNode().getLocalName())
                && BeansEditorUtils.hasAttribute(node.getParentNode(), "ref")) {
            addMethodAttributeValueProposals(request, matchString, node);
        }
    }

    private void addPointcutReferenceProposals(ContentAssistRequest request,
            String prefix, IDOMNode node, Document document) {
        if (prefix == null) {
            prefix = "";
        }
        IFile file = getResource(request);
        if (document != null) {
            PointcutReferenceSearchRequestor requestor = new PointcutReferenceSearchRequestor(
                    request);
            searchPointcutElements(prefix, node.getParentNode(), requestor,
                    file);
            searchPointcutElements(prefix,
                    node.getParentNode().getParentNode(), requestor, file);
        }
    }

    private void searchPointcutElements(String prefix, Node node,
            PointcutReferenceSearchRequestor requestor, IFile file) {
        NodeList beanNodes = node.getChildNodes();
        for (int i = 0; i < beanNodes.getLength(); i++) {
            Node beanNode = beanNodes.item(i);
            if ("pointcut".equals(beanNode.getLocalName())) {
                requestor.acceptSearchMatch(beanNode, file, prefix);
            }
        }
    }

    private void addMethodAttributeValueProposals(ContentAssistRequest request,
            String prefix, IDOMNode node) {

        Node parentNode = node.getParentNode();
        String ref = BeansEditorUtils.getAttribute(parentNode, "ref");
        
        if (ref != null) {
            IFile file = (IFile) getResource(request);
            String className = BeansEditorUtils.getClassNameForBean(file, node
                    .getOwnerDocument(), ref);
            IType type = BeansModelUtils.getJavaType(file.getProject(),
                    className);
            if (type != null) {
                try {
                    Collection methods = Introspector.findAllMethods(type,
                            prefix, -1, true, Statics.DONT_CARE);
                    if (methods != null && methods.size() > 0) {
                        PublicMethodSearchRequestor requestor = new PublicMethodSearchRequestor(
                                request);
                        Iterator iterator = methods.iterator();
                        while (iterator.hasNext()) {
                            requestor.acceptSearchMatch((IMethod) iterator
                                    .next());
                        }
                    }
                }
                catch (JavaModelException e1) {
                    // do nothing
                }
                catch (CoreException e) {
                    // // do nothing
                }
            }
        }
    }

    protected void computeAttributeNameProposals(ContentAssistRequest request,
            String prefix, String namespace, String namespacePrefix,
            Node attributeNode) {

    }

    @Override
    protected void computeTagInsertionProposals(ContentAssistRequest request,
            IDOMNode node) {

    }
}
