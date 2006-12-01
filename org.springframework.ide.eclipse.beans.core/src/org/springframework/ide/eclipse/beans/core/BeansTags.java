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

package org.springframework.ide.eclipse.beans.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public final class BeansTags {

	// TODO Update for Spring 2.0
	public enum Tag { DESCRIPTION, IMPORT, ALIAS, BEANS, BEAN, CONSTRUCTOR_ARG,
		LOOKUP_METHOD, REPLACE_METHOD, PROPERTY, REF, IDREF, VALUE, NULL,
		LIST, SET, MAP, PROPS, ENTRY, KEY, PROP, ARG_TYPE, COMMENT,

		UNKNOWN
	}

	private static final Map<String, Tag> TAGS = new HashMap<String, Tag>();
	// TODO Update for Spring 2.0
	static {
		TAGS.put("description", Tag.DESCRIPTION);
		TAGS.put("import", Tag.IMPORT);
		TAGS.put("alias", Tag.ALIAS);
		TAGS.put("beans", Tag.BEANS);
		TAGS.put("bean", Tag.BEAN);
		TAGS.put("constructor-arg", Tag.CONSTRUCTOR_ARG);
		TAGS.put("lookup-method", Tag.LOOKUP_METHOD);
		TAGS.put("replace-method", Tag.REPLACE_METHOD);
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
				return getTag(node.getNodeName());
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
