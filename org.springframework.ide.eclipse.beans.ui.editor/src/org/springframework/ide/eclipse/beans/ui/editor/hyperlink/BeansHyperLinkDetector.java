/*
 * Copyright 2002-2004 the original author or authors.
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

package org.springframework.ide.eclipse.beans.ui.editor.hyperlink;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.sse.core.internal.provisional.IndexedRegion;
import org.eclipse.wst.sse.core.internal.provisional.StructuredModelManager;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMAttr;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.springframework.ide.eclipse.beans.core.internal.Introspector;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.core.StringUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Detects hyperlinks in XML tags. Includes detection of bean classes and bean
 * properties in attribute values. Resolves bean references (including
 * references to parent beans or factory beans).
 */
public class BeansHyperLinkDetector implements IHyperlinkDetector {

    private IEditorPart editor;

    public BeansHyperLinkDetector(IEditorPart editor) {
        this.editor = editor;
    }

    public IHyperlink[] detectHyperlinks(ITextViewer textViewer, IRegion region,
            boolean canShowMultipleHyperlinks) {
        if (region == null || textViewer == null) {
            return null;
        }

        IDocument document = textViewer.getDocument();
        Node currentNode = getCurrentNode(document, region.getOffset());
        if (currentNode != null) {
            short nodeType = currentNode.getNodeType();
            if (nodeType == Node.DOCUMENT_TYPE_NODE) {
                // nothing to do
            }
            else if (nodeType == Node.ELEMENT_NODE) {
                // element nodes
                Attr currentAttr = getCurrentAttrNode(currentNode, region.getOffset());
                if (currentAttr != null && this.isLinkableAttr(currentAttr)) {
                    IRegion hyperlinkRegion = getHyperlinkRegion(currentAttr);
                    IHyperlink hyperLink = createHyperlink(currentAttr, hyperlinkRegion, document,
                            currentNode, textViewer);
                    if (hyperLink != null) {
                        return new IHyperlink[] { hyperLink };
                    }
                }
            }
        }
        return null;
    }

    /**
     * Returns the attribute node within node at offset
     */
    private Attr getCurrentAttrNode(Node node, int offset) {
        if ((node instanceof IndexedRegion) && ((IndexedRegion) node).contains(offset)
                && (node.hasAttributes())) {
            NamedNodeMap attrs = node.getAttributes();
            // go through each attribute in node and if attribute contains
            // offset, return that attribute
            for (int i = 0; i < attrs.getLength(); ++i) {
                // assumption that if parent node is of type IndexedRegion,
                // then its attributes will also be of type IndexedRegion
                IndexedRegion attRegion = (IndexedRegion) attrs.item(i);
                if (attRegion.contains(offset)) {
                    return (Attr) attrs.item(i);
                }
            }
        }
        return null;
    }

    /**
     * Returns the node the cursor is currently on in the document. null if no node is selected
     * 
     * @param offset
     * @return Node either element, doctype, text, or null
     */
    private Node getCurrentNode(IDocument document, int offset) {
        // get the current node at the offset (returns either: element,
        // doctype, text)
        IndexedRegion inode = null;
        IStructuredModel sModel = null;
        try {
            sModel = StructuredModelManager.getModelManager().getExistingModelForRead(document);
            inode = sModel.getIndexedRegion(offset);
            if (inode == null)
                inode = sModel.getIndexedRegion(offset - 1);
        }
        finally {
            if (sModel != null)
                sModel.releaseFromRead();
        }

        if (inode instanceof Node) {
            return (Node) inode;
        }
        return null;
    }

    private IRegion getHyperlinkRegion(Node node) {
        IRegion hyperRegion = null;

        if (node != null) {
            short nodeType = node.getNodeType();
            if (nodeType == Node.DOCUMENT_TYPE_NODE || nodeType == Node.ELEMENT_NODE) {
                // handle doc type node
                IDOMNode docNode = (IDOMNode) node;
                hyperRegion = new Region(docNode.getStartOffset(), docNode.getEndOffset()
                        - docNode.getStartOffset());
            }
            else if (nodeType == Node.ATTRIBUTE_NODE) {
                // handle attribute nodes
                IDOMAttr att = (IDOMAttr) node;
                // do not include quotes in attribute value region
                int regOffset = att.getValueRegionStartOffset();
                int regLength = att.getValueRegionText().length();
                String attValue = att.getValueRegionText();
                if (StringUtils.isQuoted(attValue)) {
                    regOffset = ++regOffset;
                    regLength = regLength - 2;
                }
                hyperRegion = new Region(regOffset, regLength);
            }
        }
        return hyperRegion;
    }

    /**
     * Checks to see if the given attribute is openable. Attribute is openable if it is a namespace
     * declaration attribute or if the attribute value is of type URI.
     * 
     * @return true if this attribute is "openOn-able" false otherwise
     */
    private boolean isLinkableAttr(Attr attr) {
        String attrName = attr.getName();

        if ("class".equals(attrName)) {
            return true;
        }
        else if ("name".equals(attrName) && "property".equals(attr.getOwnerElement().getNodeName())) {
            return true;
        }
        else if ("init-method".equals(attrName)) {
            return true;
        }
        else if ("destroy-method".equals(attrName)) {
            return true;
        }
        else if ("factory-method".equals(attrName)) {
            return true;
        }
        else if ("factory-bean".equals(attrName)) {
            return true;
        }
        else if ("parent".equals(attrName)) {
            return true;
        }
        else if ("depends-on".equals(attrName)) {
            return true;
        }

        return false;
    }

    /**
     * Create the appropriate hyperlink
     */
    private IHyperlink createHyperlink(Attr attr, IRegion hyperlinkRegion, IDocument document,
            Node node, ITextViewer textViewer) {
        IHyperlink link = null;

        if (attr != null) {
            String name = attr.getName();
            String target = attr.getNodeValue();
            Node parentNode = attr.getOwnerElement();
            String parentName = null;
            if (parentNode != null) {
                parentName = parentNode.getNodeName();
            }
            if ("class".equals(name)) {
                IFile file = ((IFileEditorInput) this.editor.getEditorInput()).getFile();
                IType type = BeansModelUtils.getJavaType(file.getProject(), target);
                if (type != null) {
                    link = new JavaElementHyperlink(hyperlinkRegion, type);
                }
            }
            else if ("name".equals(name) && "property".equals(parentName)) {
                Node parentParentNode = parentNode.getParentNode();
                if ("bean".equals(parentParentNode.getNodeName())) {
                    NamedNodeMap attributes = parentParentNode.getAttributes();
                    if (attributes != null && attributes.getNamedItem("class") != null) {

                        String className = attributes.getNamedItem("class").getNodeValue();
                        IFile file = ((IFileEditorInput) this.editor.getEditorInput()).getFile();
                        IType type = BeansModelUtils.getJavaType(file.getProject(), className);
                        if (type != null) {

                            // TODO Add support for nested nested property paths, e.g.
                            // "stuff1[0].stuff2"
                            // From "BeansConfigValidator.java":
                            // PropertyTokenHolder tokens = getPropertyNameTokens(
                            // nestedPropertyName);
                            // String getterName = "get" + StringUtils.capitalize(
                            // tokens.actualName);
                            // IMethod getter = Introspector.findMethod(type, getterName,
                            // 0, true, false);
                            // if (getter != null) {

                            String methodName = "set" + StringUtils.capitalize(target);
                            try {
                                IMethod method = Introspector.findMethod(type, methodName, 1, true,
                                        Introspector.STATIC_NO);
                                if (method != null) {
                                    link = new JavaElementHyperlink(hyperlinkRegion, method);
                                }
                            }
                            catch (JavaModelException e) {
                            }
                        }
                    }
                }
            }
            else if ("init-method".equals(name) || "destroy-method".equals(name)) {
                NamedNodeMap attributes = parentNode.getAttributes();
                if (attributes != null && attributes.getNamedItem("class") != null) {
                    String className = attributes.getNamedItem("class").getNodeValue();
                    IFile file = ((IFileEditorInput) this.editor.getEditorInput()).getFile();
                    IType type = BeansModelUtils.getJavaType(file.getProject(), className);

                    try {
                        IMethod method = Introspector.findMethod(type, target, 0, true,
                                Introspector.STATIC_IRRELVANT);
                        if (method != null) {
                            link = new JavaElementHyperlink(hyperlinkRegion, method);
                        }
                    }
                    catch (JavaModelException e) {
                    }
                }
            }
            else if ("factory-method".equals(name)) {
                NamedNodeMap attributes = parentNode.getAttributes();
                String className = null;
                if (attributes != null && attributes.getNamedItem("factory-bean") != null) {
                    Node factoryBean = attributes.getNamedItem("factory-bean");
                    if (factoryBean != null) {
                        String factoryBeanId = factoryBean.getNodeValue();
                        // TODO add factoryBean support for beans defined outside of the current
                        // xml file
                        Document doc = node.getOwnerDocument();
                        Element bean = doc.getElementById(factoryBeanId);
                        if (bean != null && bean instanceof Node) {
                            NamedNodeMap attribute = ((Node) bean).getAttributes();
                            className = attribute.getNamedItem("class").getNodeValue();
                        }
                    }
                }
                else if (attributes != null && attributes.getNamedItem("class") != null) {
                    className = attributes.getNamedItem("class").getNodeValue();
                }
                try {
                    IFile file = ((IFileEditorInput) this.editor.getEditorInput()).getFile();
                    IType type = BeansModelUtils.getJavaType(file.getProject(), className);
                    IMethod method = Introspector.findMethod(type, target, -1, true,
                            Introspector.STATIC_YES);
                    if (method != null) {
                        link = new JavaElementHyperlink(hyperlinkRegion, method);
                    }
                }
                catch (JavaModelException e) {
                }
            }
            else if ("factory-bean".equals(name) || "depends-on".equals(name)
                    || "parent".equals(name)) {
                // TODO add factoryBean support for beans defined outside of the current
                // xml file
                Document doc = node.getOwnerDocument();
                Element bean = doc.getElementById(target);
                if (bean != null) {
                    IRegion region = getHyperlinkRegion(bean);
                    link = new NodeElementHyperlink(hyperlinkRegion, region, textViewer);
                }
                else {
                    // TODO handle external lookup
                }
            }
        }
        return link;
    }
}
