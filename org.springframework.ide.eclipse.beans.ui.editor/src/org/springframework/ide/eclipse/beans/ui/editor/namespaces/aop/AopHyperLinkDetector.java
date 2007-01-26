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

package org.springframework.ide.eclipse.beans.ui.editor.namespaces.aop;

import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.springframework.ide.eclipse.beans.core.internal.Introspector;
import org.springframework.ide.eclipse.beans.core.internal.Introspector.Statics;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.AbstractHyperLinkDetector;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.ExternalBeanHyperlink;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.JavaElementHyperlink;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.NodeElementHyperlink;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Detects hyperlinks in XML tags. Includes detection of bean classes and bean properties in
 * attribute values. Resolves bean references (including references to parent beans or factory
 * beans).
 * 
 * @author Christian Dupuis
 */
public class AopHyperLinkDetector
        extends AbstractHyperLinkDetector implements IHyperlinkDetector {

    /**
     * Returns <code>true</code> if given attribute is openable.
     */
    protected boolean isLinkableAttr(Attr attr) {
        String attrName = attr.getName();
        if ("method".equals(attrName)) {
            return true;
        }
        else if ("ref".equals(attrName)) {
            return true;
        }
        else if ("pointcut-ref".equals(attrName)) {
            return true;
        }
        else if ("implement-interface".equals(attrName) || "default-impl".equals(attrName)) {
            return true;
        }
        return false;
    }

    protected IHyperlink createHyperlink(String name, String target, Node parentNode,
            IRegion hyperlinkRegion, IDocument document, Node node, ITextViewer textViewer,
            IRegion cursor) {
        if (name == null) {
            return null;
        }
        String parentName = null;
        if (parentNode != null) {
            parentName = parentNode.getLocalName();
        }
        if ("implement-interface".equals(name) || "default-impl".equals(name)) {
            IFile file = BeansEditorUtils.getFile(document);
            IType type = BeansModelUtils.getJavaType(file.getProject(), target);
            if (type != null) {
                return new JavaElementHyperlink(hyperlinkRegion, type);
            }
        }
        else if ("method".equals(name) && "aspect".equals(parentName)) {
            if (BeansEditorUtils.hasAttribute(parentNode, "ref")) {
                String ref = BeansEditorUtils.getAttribute(parentNode, "ref");

                if (ref != null) {
                    IFile file = BeansEditorUtils.getFile(document);
                    String className = BeansEditorUtils.getClassNameForBean(file, node
                            .getOwnerDocument(), ref);
                    IType type = BeansModelUtils.getJavaType(file.getProject(), className);
                    try {
                        IMethod method = Introspector.findMethod(type, target, -1, true,
                                Statics.DONT_CARE);
                        if (method != null) {
                            return new JavaElementHyperlink(hyperlinkRegion, method);
                        }
                    }
                    catch (JavaModelException e) {
                    }
                }
            }
        }
        else if ("pointcut-ref".equals(name) && parentNode != null) {
            IHyperlink hyperlink = searchPointcutElements(target, parentNode, textViewer,
                    hyperlinkRegion);
            if (hyperlink == null && parentNode.getParentNode() != null) {
                hyperlink = searchPointcutElements(target, parentNode.getParentNode(), textViewer,
                        hyperlinkRegion);
            }
            return hyperlink;
        }
        else if ("adivce-ref".equals(name)) {
            Node bean = BeansEditorUtils.getFirstReferenceableNodeById(node.getOwnerDocument(),
                    target);
            if (bean != null) {
                IRegion region = getHyperlinkRegion(bean);
                return new NodeElementHyperlink(hyperlinkRegion, region, textViewer);
            }
            else {
                IFile file = BeansEditorUtils.getFile(document);
                // assume this is an external reference
                Iterator<?> beans = BeansEditorUtils.getBeansFromConfigSets(file).iterator();
                while (beans.hasNext()) {
                    IBean modelBean = (IBean) beans.next();
                    if (modelBean.getElementName().equals(target)) {
                        return new ExternalBeanHyperlink(modelBean, hyperlinkRegion);
                    }
                }
            }
        }
        return null;
    }

    private IHyperlink searchPointcutElements(String name, Node node, ITextViewer textViewer,
            IRegion hyperlinkRegion) {
        NodeList beanNodes = node.getChildNodes();
        for (int i = 0; i < beanNodes.getLength(); i++) {
            Node beanNode = beanNodes.item(i);
            if ("pointcut".equals(beanNode.getLocalName())) {
                if (name.equals(BeansEditorUtils.getAttribute(beanNode, "id"))) {
                    IRegion region = getHyperlinkRegion(beanNode);
                    return new NodeElementHyperlink(hyperlinkRegion, region, textViewer);
                }
            }
        }
        return null;
    }
}
