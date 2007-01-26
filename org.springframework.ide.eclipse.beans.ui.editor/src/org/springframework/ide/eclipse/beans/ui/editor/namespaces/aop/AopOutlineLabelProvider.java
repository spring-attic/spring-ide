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

package org.springframework.ide.eclipse.beans.ui.editor.namespaces.aop;

import org.eclipse.swt.graphics.Image;
import org.eclipse.wst.xml.ui.internal.contentoutline.JFaceNodeLabelProvider;
import org.springframework.ide.eclipse.beans.ui.editor.outline.BeansContentOutlineConfiguration;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

@SuppressWarnings("restriction")
public class AopOutlineLabelProvider extends JFaceNodeLabelProvider {

	public Image getImage(Object object) {

		// Create Spring beans label image
		Node node = (Node) object;
		String prefix = node.getPrefix();
		String nodeName = node.getNodeName();
		if (prefix != null) {
			nodeName = nodeName.substring(prefix.length() + 1);
		}

		if ("config".equals(nodeName)) {
			return AopUIImages.getImage(AopUIImages.IMG_OBJS_CONFIG);
		}
		if ("aspect".equals(nodeName)) {
			return AopUIImages.getImage(AopUIImages.IMG_OBJS_ASPECT);
		}
		if ("before".equals(nodeName)) {
			return AopUIImages
					.getImage(AopUIImages.IMG_OBJS_BEFORE_ADVICE);
		}
		if ("after".equals(nodeName)) {
			return AopUIImages
					.getImage(AopUIImages.IMG_OBJS_AFTER_ADVICE);
		}
		if ("around".equals(nodeName)) {
			return AopUIImages
					.getImage(AopUIImages.IMG_OBJS_AROUND_ADVICE);
		}
		if ("after-returning".equals(nodeName)) {
			return AopUIImages
					.getImage(AopUIImages.IMG_OBJS_AFTER_ADVICE);
		}
		if ("after-throwing".equals(nodeName)) {
			return AopUIImages
					.getImage(AopUIImages.IMG_OBJS_AFTER_ADVICE);
		}
		if ("pointcut".equals(nodeName)) {
			return AopUIImages.getImage(AopUIImages.IMG_OBJS_POINTCUT);
		}
		if ("advisor".equals(nodeName)) {
		    return AopUIImages.getImage(AopUIImages.IMG_OBJS_ADVICE);
		}
        if ("aspectj-autoproxy".equals(nodeName)) {
            return AopUIImages.getImage(AopUIImages.IMG_OBJS_CONFIG);
        }
        if ("include".equals(nodeName)) {
            return AopUIImages.getImage(AopUIImages.IMG_OBJS_CONFIG);
        }
        if ("declare-parents".equals(nodeName)) {
            return AopUIImages.getImage(AopUIImages.IMG_OBJS_INTRODUCTION);
        }

		return null;
	}

	public String getText(Object o) {
		// Create Spring beans label text
		Node node = (Node) o;
		NamedNodeMap attrs = node.getAttributes();
		Node attr;
		String text = "";

		String prefix = node.getPrefix();
		String shortNodeName = node.getNodeName();
		String nodeName = node.getNodeName();
		if (prefix != null) {
			shortNodeName = nodeName.substring(prefix.length() + 1);
		}
		nodeName = "<" + node.getNodeName() + "/>";

		if ("config".equals(shortNodeName)) {
			text = nodeName + " ";
		}
		else if ("aspect".equals(shortNodeName)) {
			text = nodeName + " ";
			if (BeansContentOutlineConfiguration.isShowAttributes()) {
				attr = attrs.getNamedItem("id");
				if (attr != null) {
					text += attr.getNodeValue() + " ";
				}
				attr = attrs.getNamedItem("ref");
				if (attr != null) {
					text += "<" + attr.getNodeValue() + "> ";
				}
			}
		}
		else if ("advisor".equals(shortNodeName)) {
		    text = nodeName + " ";
		    if (BeansContentOutlineConfiguration.isShowAttributes()) {
		        attr = attrs.getNamedItem("id");
		        if (attr != null) {
		            text += attr.getNodeValue() + " ";
		        }
		        attr = attrs.getNamedItem("advice-ref");
		        if (attr != null) {
		            text += "<" + attr.getNodeValue() + "> ";
		        }
		        attr = attrs.getNamedItem("pointcut-ref");
		        if (attr != null) {
		            text += "<" + attr.getNodeValue() + "> ";
		        }
		    }
		}
		else if ("before".equals(shortNodeName) || "after".equals(shortNodeName)
				|| "after-returning".equals(shortNodeName)
				|| "after-throwing".equals(shortNodeName)
				|| "around".equals(shortNodeName)) {
			text = nodeName + " ";
			if (BeansContentOutlineConfiguration.isShowAttributes()) {
				attr = attrs.getNamedItem("method");
				if (attr != null) {
					text += attr.getNodeValue() + " ";
				}
				attr = attrs.getNamedItem("pointcut");
				if (attr != null) {
					text += "{" + attr.getNodeValue() + "} ";
				}
			}
		}
		else if ("pointcut".equals(shortNodeName)) {
			text = nodeName + " ";
			if (BeansContentOutlineConfiguration.isShowAttributes()) {
				attr = attrs.getNamedItem("id");
				if (attr != null) {
					text += attr.getNodeValue() + " ";
				}
			}
		}
		else if ("include".equals(shortNodeName)) {
		    text = nodeName + " ";
		    if (BeansContentOutlineConfiguration.isShowAttributes()) {
		        attr = attrs.getNamedItem("name");
		        if (attr != null) {
		            text += "[" + attr.getNodeValue() + "]";
		        }
		    }
		}
		else if ("aspectj-autoproxy".equals(shortNodeName)) {
            text = nodeName;
        }
		else if ("declare-parents".equals(shortNodeName)) {
            text = nodeName;
        }

		return text;
	}
}