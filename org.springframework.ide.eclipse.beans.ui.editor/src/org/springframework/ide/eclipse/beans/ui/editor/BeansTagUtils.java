package org.springframework.ide.eclipse.beans.ui.editor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public final class BeansTagUtils {

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

	public static final int UNKNOWN_TAG = 99;

	private static Map tags = new HashMap();
	static {
		tags.put("description", Integer.valueOf(DESCRIPTION));
		tags.put("import", Integer.valueOf(IMPORT));
		tags.put("alias", Integer.valueOf(ALIAS));
		tags.put("beans", Integer.valueOf(BEANS));
		tags.put("bean", Integer.valueOf(BEAN));
		tags.put("constructor-arg", Integer.valueOf(CONSTRUCTOR_ARG));
		tags.put("lookup-method", Integer.valueOf(LOOKUP_METHOD));
		tags.put("replace-method", Integer.valueOf(REPLACE_METHOD));
		tags.put("property", Integer.valueOf(PROPERTY));
		tags.put("ref", Integer.valueOf(REF));
		tags.put("idref", Integer.valueOf(IDREF));
		tags.put("value", Integer.valueOf(VALUE));
		tags.put("null", Integer.valueOf(NULL));
		tags.put("list", Integer.valueOf(LIST));
		tags.put("set", Integer.valueOf(SET));
		tags.put("map", Integer.valueOf(MAP));
		tags.put("props", Integer.valueOf(PROPS));
		tags.put("entry", Integer.valueOf(ENTRY));
		tags.put("key", Integer.valueOf(KEY));
		tags.put("prop", Integer.valueOf(PROP));
		tags.put("arg-type", Integer.valueOf(ARG_TYPE));
	}

	public static int getTag(Node node) {
		if (node != null) {
			if (node.getNodeType() == Node.TEXT_NODE) {
				return getTag(node.getParentNode());
			} else if (node.getNodeType() == Node.ELEMENT_NODE) {
				Integer tag = (Integer) tags.get(node.getNodeName());
				if (tag != null) {
					return tag.intValue();
				}
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
