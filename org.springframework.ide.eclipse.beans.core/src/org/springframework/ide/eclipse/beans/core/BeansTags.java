/*******************************************************************************
 * Copyright (c) 2005, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public final class BeansTags {

	public enum Tag { DESCRIPTION, IMPORT, ALIAS, BEANS, BEAN, CONSTRUCTOR_ARG,
		LOOKUP_METHOD, REPLACE_METHOD, PROPERTY, REF, IDREF, VALUE, NULL,
		LIST, SET, MAP, PROPS, ENTRY, KEY, PROP, ARG_TYPE, COMMENT,

		UNKNOWN
	}

	private static final Map<String, Tag> TAGS = new HashMap<String, Tag>();

	static {
		TAGS.put("description", Tag.DESCRIPTION);
		TAGS.put("import", Tag.IMPORT);
		TAGS.put("alias", Tag.ALIAS);
		TAGS.put("beans", Tag.BEANS);
		TAGS.put("bean", Tag.BEAN);
		TAGS.put("constructor-arg", Tag.CONSTRUCTOR_ARG);
		TAGS.put("lookup-method", Tag.LOOKUP_METHOD);
		TAGS.put("replaced-method", Tag.REPLACE_METHOD);
		TAGS.put("property", Tag.PROPERTY);
		TAGS.put("ref", Tag.REF);
		TAGS.put("idref", Tag.IDREF);
		TAGS.put("value", Tag.VALUE);
		TAGS.put("null", Tag.NULL);
		TAGS.put("list", Tag.LIST);
		TAGS.put("set", Tag.SET);
		TAGS.put("map", Tag.MAP);
		TAGS.put("props", Tag.PROPS);
		TAGS.put("entry", Tag.ENTRY);
		TAGS.put("key", Tag.KEY);
		TAGS.put("prop", Tag.PROP);
		TAGS.put("arg-type", Tag.ARG_TYPE);
		TAGS.put("#comment", Tag.COMMENT);
	}

	public static Tag getTag(String name) {
		if (name != null) {
			name = name.trim();
			if (TAGS.containsKey(name)) {
				return TAGS.get(name);
			}
		}
		return Tag.UNKNOWN;
	}

	public static Tag getTag(Node node) {
		if (node != null) {
			if (node.getNodeType() == Node.TEXT_NODE) {
				return getTag(node.getParentNode());
			} else if (node.getNodeType() == Node.ELEMENT_NODE
					|| node.getNodeType() == Node.COMMENT_NODE) {
				return getTag(node.getLocalName());
			}
		}
		return Tag.UNKNOWN;
	}

	public static boolean isTag(Node node, Tag tag) {
		return (getTag(node) == tag);
	}

	public static List<Node> getChildElementsByTagName(Element element,
			String name) {
		List<Node> list = new ArrayList<Node>();
		NodeList children = element.getChildNodes();
		if (children != null) {
			for (int i = 0; i < children.getLength(); i++) {
				Node child = children.item(i);
				if (child.getNodeType() == 1
						&& name.equals(child.getNodeName())) {
					list.add(child);
				}
			}

		}
		return list;
	}

	public static Element getFirstChildElementByTagName(Element element,
			String name) {
		NodeList children = element.getChildNodes();
		if (children != null) {
			for (int i = 0; i < children.getLength(); i++) {
				Node child = children.item(i);
				if (child.getNodeType() == 1
						&& name.equals(child.getNodeName())) {
					return (Element) child;
				}
			}

		}
		return null;
	}
}
