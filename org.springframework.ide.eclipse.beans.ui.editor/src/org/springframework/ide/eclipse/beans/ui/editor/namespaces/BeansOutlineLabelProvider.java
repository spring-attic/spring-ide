/*
 * Copyright 2002-2006 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 

package org.springframework.ide.eclipse.beans.ui.editor.namespaces;

import org.eclipse.swt.graphics.Image;
import org.eclipse.wst.xml.core.internal.document.CommentImpl;
import org.eclipse.wst.xml.ui.internal.contentoutline.JFaceNodeLabelProvider;
import org.springframework.ide.eclipse.beans.core.BeansTags;
import org.springframework.ide.eclipse.beans.core.BeansTags.Tag;
import org.springframework.ide.eclipse.beans.ui.BeansUIImages;
import org.springframework.ide.eclipse.beans.ui.editor.outline.BeansContentOutlineConfiguration;
import org.springframework.ide.eclipse.beans.ui.model.BeansModelImages;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

@SuppressWarnings("restriction")
final class BeansOutlineLabelProvider
        extends JFaceNodeLabelProvider {

    private BeansContentOutlineConfiguration configuration;

    public BeansOutlineLabelProvider(
            BeansContentOutlineConfiguration configuration) {
        this.configuration = configuration;
    }

    public Image getImage(Object object) {
        // Create Spring beans label image
        Node node = (Node) object;
        String nodeName = node.getNodeName();
        NamedNodeMap attributes = node.getAttributes();

        // Root elements (alias, import and bean)
        if ("alias".equals(nodeName)) {
            return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_ALIAS);
        }
        if ("import".equals(nodeName)) {
            return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_IMPORT);
        }
        if ("bean".equals(nodeName)) {
            int flags = 0;
            if (attributes.getNamedItem("parent") != null) {
                flags |= BeansModelImages.FLAG_CHILD;
            }
            else if (attributes.getNamedItem("factory-method") != null) {
                flags |= BeansModelImages.FLAG_FACTORY;
            }
            return BeansModelImages.getImage(BeansModelImages.ELEMENT_BEAN,
                    flags);
        }
        if ("beans".equals(nodeName)) {
            return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_CONFIG);
        }

        // Bean elements
        if ("constructor-arg".equals(nodeName)) {
            return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_CONSTRUCTOR);
        }
        if ("property".equals(nodeName)) {
            return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_PROPERTY);
        }

        // Misc elements
        if ("list".equals(nodeName) || "set".equals(nodeName)
                || "map".equals(nodeName) || "props".equals(nodeName)) {
            return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_COLLECTION);
        }
        if ("ref".equals(nodeName)) {
            return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_BEAN_REF);
        }
        if ("description".equals(nodeName)) {
            return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_DESCRIPTION);
        }
        return null;
    }

    public String getText(Object o) {
        // Create Spring beans label text
        Node node = (Node) o;
        NamedNodeMap attrs = node.getAttributes();
        Node attr;
        String text = "";

        // Root elements (alias, import and bean)
        Tag tag = BeansTags.getTag(node);
        if (tag == Tag.IMPORT) {
            attr = attrs.getNamedItem("resource");
            if (attr != null) {
                text = attr.getNodeValue();
            }
        }
        else if (tag == Tag.ALIAS) {
            attr = attrs.getNamedItem("name");
            if (attr != null) {
                text = attr.getNodeValue();
            }
            if (configuration.isShowAttributes()) {
                attr = attrs.getNamedItem("alias");
                if (attr != null) {
                    text += " \"" + attr.getNodeValue() + "\"";
                }
            }
        }
        else if (tag == Tag.BEANS) {
        	text = "beans";
        }
        else if (tag == Tag.BEAN) {
            boolean hasParent = false;
            attr = attrs.getNamedItem("id");
            if (attr != null) {
                text = attr.getNodeValue();
            }
            else {
                attr = attrs.getNamedItem("name");
                if (attr != null) {
                    text = attr.getNodeValue();
                }
                else {
                    attr = attrs.getNamedItem("parent");
                    if (attr != null) {
                        text = "<" + attr.getNodeValue() + ">";
                        hasParent = true;
                    }
                }
            }
            if (configuration.isShowAttributes()) {
                attr = attrs.getNamedItem("class");
                if (attr != null) {
                    if (text.length() > 0) {
                        text += ' ';
                    }
                    text += '[' + attr.getNodeValue() + ']';
                }
                if (!hasParent) {
                    attr = attrs.getNamedItem("parent");
                    if (attr != null) {
                        if (text.length() > 0) {
                            text += ' ';
                        }
                        text += '<' + attr.getNodeValue() + '>';
                    }
                }
            }
        }
        else if (tag == Tag.CONSTRUCTOR_ARG) {
            attr = attrs.getNamedItem("index");
            if (attr != null) {
                text += " {" + attr.getNodeValue() + "}";
            }
            attr = attrs.getNamedItem("type");
            if (attr != null) {
                text += " [" + attr.getNodeValue() + "]";
            }
            attr = attrs.getNamedItem("ref");
            if (attr != null) {
                text += " <" + attr.getNodeValue() + ">";
            }
            attr = attrs.getNamedItem("value");
            if (attr != null) {
                text += " \"" + attr.getNodeValue() + "\"";
            }
        }
        else if (tag == Tag.PROPERTY) {
            attr = attrs.getNamedItem("name");
            if (attr != null) {
                text = attr.getNodeValue();
            }
            if (configuration.isShowAttributes()) {
                attr = attrs.getNamedItem("ref");
                if (attr != null) {
                    text += " <" + attr.getNodeValue() + ">";
                }
                attr = attrs.getNamedItem("value");
                if (attr != null) {
                    text += " \"" + attr.getNodeValue() + "\"";
                }
            }
        }
        else if (tag == Tag.REF || tag == Tag.IDREF) {
            attr = attrs.getNamedItem("bean");
            if (attr != null) {
                text += "<" + attr.getNodeValue() + ">";
            }
            attr = attrs.getNamedItem("local");
            if (attr != null) {
                text += "<" + attr.getNodeValue() + ">";
            }
            attr = attrs.getNamedItem("parent");
            if (attr != null) {
                text += "<" + attr.getNodeValue() + ">";
            }
        }
        else if (tag == Tag.VALUE) {
            text = node.getNodeName();
            if (configuration.isShowAttributes()) {
                attr = attrs.getNamedItem("type");
                if (attr != null) {
                    text += " [" + attr.getNodeValue() + "]";
                }
            }
        }
        else if (tag == Tag.ENTRY) {
            text = node.getNodeName();
            attr = attrs.getNamedItem("key");
            if (attr != null) {
                text += " \"" + attr.getNodeValue() + "\"";
            }
            else {
                attr = attrs.getNamedItem("key-ref");
                if (attr != null) {
                    text += " <" + attr.getNodeValue() + ">";
                }
            }
            if (configuration.isShowAttributes()) {
                attr = attrs.getNamedItem("value");
                if (attr != null) {
                    text += " \"" + attr.getNodeValue() + "\"";
                }
            }
        }
        else if (tag == Tag.PROP) {
            text = node.getNodeName();
            attr = node.getFirstChild();
            if (attr != null && attr.getNodeType() == Node.TEXT_NODE) {
                text += " \"" + attr.getNodeValue() + "\"";
            }
        }
        else if (tag == Tag.COMMENT) {
            text = super.getText(o);
            text += " <";
            text += ((CommentImpl) o).getNodeValue().trim();
            text += '>';
        }
        return text;
    }
}