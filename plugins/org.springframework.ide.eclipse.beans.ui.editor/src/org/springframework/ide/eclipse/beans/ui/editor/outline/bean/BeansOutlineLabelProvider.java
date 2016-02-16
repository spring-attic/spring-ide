/*******************************************************************************
 * Copyright (c) 2006, 2008 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.editor.outline.bean;

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

/**
 * @author Christian Dupuis
 * @author Torsten Juergeleit
 */
@SuppressWarnings("restriction")
public class BeansOutlineLabelProvider extends JFaceNodeLabelProvider {

	@Override
	public Image getImage(Object object) {
		// Create Spring beans label image
		Node node = (Node) object;
		String nodeName = node.getLocalName();
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
			else if (attributes.getNamedItem("factory-bean") != null
					|| attributes.getNamedItem("factory-method") != null) {
				flags |= BeansModelImages.FLAG_FACTORY;
			}
			Image image = BeansUIImages.getImage(BeansUIImages.IMG_OBJS_BEAN);
			return BeansModelImages.getDecoratedImage(image, flags);
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
		if ("lookup-method".equals(nodeName) || "replaced-method".equals(nodeName)) {
			return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_METHOD_OVERRIDE);
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

	@Override
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
			if (BeansContentOutlineConfiguration.isShowAttributes()) {
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
			if (BeansContentOutlineConfiguration.isShowAttributes()) {
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
			if (BeansContentOutlineConfiguration.isShowAttributes()) {
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
		else if (tag == Tag.LOOKUP_METHOD) {
			attr = attrs.getNamedItem("name");
			if (attr != null) {
				text = attr.getNodeValue();
			}
			if (BeansContentOutlineConfiguration.isShowAttributes()) {
				attr = attrs.getNamedItem("bean");
				if (attr != null) {
					text += " <" + attr.getNodeValue() + ">";
				}
			}
		}
		else if (tag == Tag.REPLACE_METHOD) {
			attr = attrs.getNamedItem("name");
			if (attr != null) {
				text = attr.getNodeValue();
			}
			if (BeansContentOutlineConfiguration.isShowAttributes()) {
				attr = attrs.getNamedItem("replacer");
				if (attr != null) {
					text += " <" + attr.getNodeValue() + ">";
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
			text = node.getLocalName();
			if (BeansContentOutlineConfiguration.isShowAttributes()) {
				attr = attrs.getNamedItem("type");
				if (attr != null) {
					text += " [" + attr.getNodeValue() + "]";
				}
			}
		}
		else if (tag == Tag.ENTRY) {
			text = node.getLocalName();
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
			if (BeansContentOutlineConfiguration.isShowAttributes()) {
				attr = attrs.getNamedItem("value");
				if (attr != null) {
					text += " \"" + attr.getNodeValue() + "\"";
				}
			}
		}
		else if (tag == Tag.PROP) {
			text = node.getLocalName();
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
		else if (tag != Tag.UNKNOWN) {
			text = node.getLocalName();
		}
		
		return text;
	}
}
