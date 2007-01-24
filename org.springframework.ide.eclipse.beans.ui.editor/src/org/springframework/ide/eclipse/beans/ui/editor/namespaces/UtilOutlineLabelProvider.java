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

import org.eclipse.swt.graphics.Image;
import org.eclipse.wst.xml.ui.internal.contentoutline.JFaceNodeLabelProvider;
import org.springframework.ide.eclipse.beans.ui.editor.outline.BeansContentOutlineConfiguration;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

@SuppressWarnings("restriction")
final class UtilOutlineLabelProvider
        extends JFaceNodeLabelProvider {

    private BeansContentOutlineConfiguration configuration;

    public UtilOutlineLabelProvider(
            BeansContentOutlineConfiguration configuration) {
        this.configuration = configuration;
    }

    public Image getImage(Object object) {

        Node node = (Node) object;
        String prefix = node.getPrefix();
        String nodeName = node.getNodeName();
        if (prefix != null) {
            nodeName = nodeName.substring(prefix.length() + 1);
        }

        // Root elements (alias, import and bean)
        if ("properties".equals(nodeName)) {
            return UtilUIImages.getImage(UtilUIImages.IMG_OBJS_PROPERTIES);
        }
        else if ("property-path".equals(nodeName)) {
            return UtilUIImages.getImage(UtilUIImages.IMG_OBJS_PROPERTIES);
        }
        else if ("constant".equals(nodeName)) {
            return UtilUIImages.getImage(UtilUIImages.IMG_OBJS_CONSTANT);
        }
        else if ("set".equals(nodeName)) {
            return UtilUIImages.getImage(UtilUIImages.IMG_OBJS_SET);
        }
        else if ("list".equals(nodeName)) {
            return UtilUIImages.getImage(UtilUIImages.IMG_OBJS_LIST);
        }
        else if ("map".equals(nodeName)) {
            return UtilUIImages.getImage(UtilUIImages.IMG_OBJS_MAP);
        }
        return null;
    }

    public String getText(Object o) {

        // Create Spring beans label text
        Node node = (Node) o;
        NamedNodeMap attrs = node.getAttributes();
        Node attr;
        String prefix = node.getPrefix();
        String nodeName = node.getNodeName();
        String shortNodeName = node.getNodeName();
        if (prefix != null) {
            shortNodeName = nodeName.substring(prefix.length() + 1);
        }
        nodeName = "<" + node.getNodeName() + "/>";

        String text = "";
        if ("properties".equals(shortNodeName)) {
            text = nodeName + " ";
            if (this.configuration.isShowAttributes()) {
                attr = attrs.getNamedItem("id");
                if (attr != null) {
                    text += attr.getNodeValue() + " ";
                }
                attr = attrs.getNamedItem("location");
                if (attr != null) {
                    text += "[" + attr.getNodeValue() + "]";
                }
            }
        }
        else if ("property-path".equals(shortNodeName)) {
            text = nodeName + " ";
            if (this.configuration.isShowAttributes()) {
                attr = attrs.getNamedItem("id");
                if (attr != null) {
                    text += attr.getNodeValue() + " ";
                }
                attr = attrs.getNamedItem("path");
                if (attr != null) {
                    text += "[" + attr.getNodeValue() + "]";
                }
            }
        }
        else if ("constant".equals(shortNodeName)) {
            text = nodeName + " ";
            if (this.configuration.isShowAttributes()) {
                attr = attrs.getNamedItem("id");
                if (attr != null) {
                    text += attr.getNodeValue() + " ";
                }
                attr = attrs.getNamedItem("static-field");
                if (attr != null) {
                    text += "[" + attr.getNodeValue() + "]";
                }
            }
        }
        else if ("set".equals(shortNodeName)) {
            text = nodeName + " ";
            if (this.configuration.isShowAttributes()) {
                attr = attrs.getNamedItem("id");
                if (attr != null) {
                    text += attr.getNodeValue() + " ";
                }
                attr = attrs.getNamedItem("set-class");
                if (attr != null) {
                    text += "[" + attr.getNodeValue() + "]";
                }
            }
        }
        else if ("list".equals(shortNodeName)) {
            text = nodeName + " ";
            if (this.configuration.isShowAttributes()) {
                attr = attrs.getNamedItem("id");
                if (attr != null) {
                    text += attr.getNodeValue() + " ";
                }
                attr = attrs.getNamedItem("list-class");
                if (attr != null) {
                    text += "[" + attr.getNodeValue() + "]";
                }
            }
        }
        else if ("map".equals(shortNodeName)) {
            text = nodeName + " ";
            if (this.configuration.isShowAttributes()) {
                attr = attrs.getNamedItem("id");
                if (attr != null) {
                    text += attr.getNodeValue() + " ";
                }
                attr = attrs.getNamedItem("map-class");
                if (attr != null) {
                    text += "[" + attr.getNodeValue() + "]";
                }
            }
        }
        return text;
    }
}