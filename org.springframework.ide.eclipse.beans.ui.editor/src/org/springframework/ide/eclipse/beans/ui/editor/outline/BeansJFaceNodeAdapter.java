/*
 * Copyright 2002-2005 the original author or authors.
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

package org.springframework.ide.eclipse.beans.ui.editor.outline;

import java.util.ArrayList;

import org.eclipse.swt.graphics.Image;
import org.eclipse.wst.sse.ui.internal.contentoutline.IJFaceNodeAdapter;
import org.eclipse.wst.xml.ui.internal.contentoutline.JFaceNodeAdapter;
import org.eclipse.wst.xml.ui.internal.contentoutline.JFaceNodeAdapterFactory;
import org.springframework.ide.eclipse.beans.ui.BeansUIImages;
import org.w3c.dom.Node;

/**
 * Adapts a DOM node to a JFace viewer.
 */
public class BeansJFaceNodeAdapter extends JFaceNodeAdapter {

	public static final Class ADAPTER_KEY = IJFaceNodeAdapter.class;

	public BeansJFaceNodeAdapter(JFaceNodeAdapterFactory adapterFactory) {
		super(adapterFactory);
	}

	public Object[] getChildren(Object object) {
		Node node = (Node) object;
		if (node.getNodeType() == Node.DOCUMENT_NODE) {
			for (Node child = node.getFirstChild(); child != null;
											  child = child.getNextSibling()) {
				if (child.getNodeType() == Node.ELEMENT_NODE &&
										 "beans".equals(child.getNodeName())) {
					ArrayList children = new ArrayList();
					for (Node n = child.getFirstChild(); n != null;
													  n = n.getNextSibling()) {
						if (n.getNodeType() == Node.ELEMENT_NODE) {
							String nodeName = n.getNodeName();
							if ("alias".equals(nodeName) ||
												   "import".equals(nodeName) ||
												   "bean".equals(nodeName)) {
								children.add(n);
							}
						}
					}
					return children.toArray();
				}
			}
		}
		ArrayList children = new ArrayList();
		for (Node child = node.getFirstChild(); child != null;
											  child = child.getNextSibling()) {
			Node n = child;
			if (n.getNodeType() != Node.TEXT_NODE) {
				children.add(n);
			}
		}
		return children.toArray();
	}

	/**
	 * Fetches the label text specific to this object instance.
	 */
	public String getLabelText(Object object) {
		Node node = (Node) object;
		String nodeName = node.getNodeName();

		// Root elements (alias, import and bean)
		if ("alias".equals(nodeName)) {
			Node aliasNode = node.getAttributes().getNamedItem("alias");
			String alias = "";
			if (aliasNode != null) {
				alias = aliasNode.getNodeValue();
			}
			Node nameNode = node.getAttributes().getNamedItem("name");
			String name = "";
			if (nameNode != null) {
				name = nameNode.getNodeValue();
			}
			return alias + "=" + name;
		}
		if ("import".equals(nodeName)) {
			Node resourceNode = node.getAttributes().getNamedItem("resource");
			String resource = "";
			if (resourceNode != null) {
				resource = resourceNode.getNodeValue();
			}
			return resource;
		}
		if ("bean".equals(nodeName)) {
			Node idNode = node.getAttributes().getNamedItem("id");
			String id;
			if (idNode != null) {
				id = idNode.getNodeValue();
			} else {
				id = "";
			}
			Node clazzNode = node.getAttributes().getNamedItem("class");
			String clazz;
			if (clazzNode != null) {
				clazz = " [" + clazzNode.getNodeValue() + "]";
			} else {
				clazz = "";
			}
			Node parentNode = node.getAttributes().getNamedItem("parent");
			String parent;
			if (parentNode != null) {
				parent = " <" + parentNode.getNodeValue() + ">";
			} else {
				parent = "";
			}
			return id + clazz + parent;
		}

		// Bean elements
		if ("property".equals(nodeName)) {
			Node nameNode = node.getAttributes().getNamedItem("name");
			String name = "";
			if (nameNode != null) {
				name = nameNode.getNodeValue();
			}
			return name;
		}

		// Misc elements
		if ("value".equals(nodeName)) {
			Node typeNode = node.getAttributes().getNamedItem("type");
			String type = "";
			if (typeNode != null) {
				type = " [" + typeNode.getNodeValue() + "]";
			}
			Node valueNode = node.getFirstChild();
			String value = "";
			if (valueNode != null &&
								   valueNode.getNodeType() == Node.TEXT_NODE) {
				value = " \"" + valueNode.getNodeValue() + "\"";
			}
			return type + value;
		}
		if ("entry".equals(nodeName)) {
			Node keyNode = node.getAttributes().getNamedItem("key");
			String key = "";
			if (keyNode != null) {
				key = " \"" + keyNode.getNodeValue() + "\"";
			} else {
				keyNode = node.getAttributes().getNamedItem("key-ref");
				if (keyNode != null) {
					key = " <" + keyNode.getNodeValue() + ">";
				}
			}
			return "entry" + key;
		}
		if ("prop".equals(nodeName)) {
			Node keyNode = node.getAttributes().getNamedItem("key");
			String key = "";
			if (keyNode != null) {
				key = " \"" + keyNode.getNodeValue() + "\"";
			}
			return "prop" + key;
		}
		if ("ref".equals(nodeName)) {
			Node beanNode = node.getAttributes().getNamedItem("bean");
			if (beanNode == null) {
				beanNode = node.getAttributes().getNamedItem("local");
			}
			String bean = "";
			if (beanNode != null) {
				bean = beanNode.getNodeValue();
			}
			return bean;
		}
		if ("description".equals(nodeName)) {
			Node valueNode = node.getFirstChild();
			String value = "";
			if (valueNode != null &&
								   valueNode.getNodeType() == Node.TEXT_NODE) {
				value = " \"" + valueNode.getNodeValue() + "\"";
			}
			return "Description" + value;
		}
		return nodeName;
	}

	/**
	 * Fetches the label image specific to this object instance.
	 */
	public Image getLabelImage(Object object) {
		Node node = (Node) object;
		String nodeName = node.getNodeName();

		// Root elements (alias, import and bean)
		if ("alias".equals(nodeName)) {
			return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_ALIAS);
		}
		if ("import".equals(nodeName)) {
			return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_IMPORT);
		}
		if ("bean".equals(nodeName)) {
			Node parentNode = node.getAttributes().getNamedItem("parent");
			if (parentNode != null) {
				return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_CHILD_BEAN);
			} else {
				return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_ROOT_BEAN);
			}
		}

		// Bean elements
		if ("constructor-arg".equals(nodeName)) {
			return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_CONSTRUCTOR);
		}
		if ("property".equals(nodeName)) {
			return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_PROPERTY);
		}

		// Misc elements
		if ("list".equals(nodeName) || "set".equals(nodeName) ||
						  "map".equals(nodeName) || "props".equals(nodeName) ||
						  "entry".equals(nodeName) || "key".equals(nodeName)) {
			return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_COLLECTION);
		}
		if ("ref".equals(nodeName)) {
			return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_BEAN_REF);
		}
		if ("description".equals(nodeName)) {
			return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_DESCRIPTION);
		}
		return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_VALUE);
	}

	public Object getParent(Object object) {
		Node node = (Node) object;
		return node.getParentNode();
	}

	public boolean hasChildren(Object object) {
		Node node = (Node) object;
		for (Node child = node.getFirstChild(); child != null;
											  child = child.getNextSibling()) {
			if (child.getNodeType() != Node.TEXT_NODE)
				return true;
		}
		return false;
	}

	/**
	 * Allowing the INodeAdapter to compare itself against the type allows it
	 * to return true in more than one case.
	 */
	public boolean isAdapterForType(Object type) {
		return type.equals(ADAPTER_KEY);
	}
}
