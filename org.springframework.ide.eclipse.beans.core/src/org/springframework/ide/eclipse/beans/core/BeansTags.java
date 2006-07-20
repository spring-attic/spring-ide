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

	public static final int DESCRIPTION = 1;
	public static final int IMPORT = 2;
	public static final int ALIAS = 3;
	public static final int BEANS = 4;
	public static final int BEAN = 5;
	public static final int CONSTRUCTOR_ARG = 6;
	public static final int LOOKUP_METHOD = 7;
	public static final int REPLACE_METHOD = 8;
	public static final int PROPERTY = 9;
	public static final int REF = 10;
	public static final int IDREF = 11;
	public static final int VALUE = 12;
	public static final int NULL = 13;
	public static final int LIST = 14;
	public static final int SET = 15;
	public static final int MAP = 16;
	public static final int PROPS = 17;
	public static final int ENTRY = 18;
	public static final int KEY = 19;
	public static final int PROP = 20;
	public static final int ARG_TYPE = 21;
	public static final int COMMENT = 22;

	public static final int UNKNOWN_TAG = 99;

	private static final Map TAGS = new HashMap();
	static {
		TAGS.put("description", new Integer(DESCRIPTION));
		TAGS.put("import", new Integer(IMPORT));
		TAGS.put("alias", new Integer(ALIAS));
		TAGS.put("beans", new Integer(BEANS));
		TAGS.put("bean", new Integer(BEAN));
		TAGS.put("constructor-arg", new Integer(CONSTRUCTOR_ARG));
		TAGS.put("lookup-method", new Integer(LOOKUP_METHOD));
		TAGS.put("replace-method", new Integer(REPLACE_METHOD));
		TAGS.put("property", new Integer(PROPERTY));
		TAGS.put("ref", new Integer(REF));
		TAGS.put("idref", new Integer(IDREF));
		TAGS.put("value", new Integer(VALUE));
		TAGS.put("null", new Integer(NULL));
		TAGS.put("list", new Integer(LIST));
		TAGS.put("set", new Integer(SET));
		TAGS.put("map", new Integer(MAP));
		TAGS.put("props", new Integer(PROPS));
		TAGS.put("entry", new Integer(ENTRY));
		TAGS.put("key", new Integer(KEY));
		TAGS.put("prop", new Integer(PROP));
		TAGS.put("arg-type", new Integer(ARG_TYPE));
		TAGS.put("#comment", new Integer(COMMENT));
	}

	public static int getTag(String name) {
		if (name != null) {
			Integer tag = (Integer) TAGS.get(name.trim());
			if (tag != null) {
				return tag.intValue();
			}
		}
		return UNKNOWN_TAG;
	}

	public static int getTag(Node node) {
		if (node != null) {
			if (node.getNodeType() == Node.TEXT_NODE) {
				return getTag(node.getParentNode());
			} else if (node.getNodeType() == Node.ELEMENT_NODE
					|| node.getNodeType() == Node.COMMENT_NODE) {
				return getTag(node.getNodeName());
			}
		}
		return UNKNOWN_TAG;
	}

	public static boolean isTag(Node node, int tag) {
		return (getTag(node) == tag);
	}

	public static List getChildElementsByTagName(Element element, String name) {
		ArrayList list = new ArrayList();
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
