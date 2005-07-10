package org.springframework.ide.eclipse.beans.ui.editor.outline;

import java.util.ArrayList;

import org.eclipse.swt.graphics.Image;
import org.eclipse.wst.sse.core.internal.provisional.INodeAdapterFactory;
import org.eclipse.wst.sse.ui.internal.contentoutline.IJFaceNodeAdapter;
import org.eclipse.wst.xml.ui.internal.contentoutline.JFaceNodeAdapter;
import org.springframework.ide.eclipse.beans.ui.BeansUIImages;
import org.w3c.dom.Node;

/**
 * Adapts a DOM node to a JFace viewer.
 */
public class BeansJFaceNodeAdapter extends JFaceNodeAdapter {

	public static final Class ADAPTER_KEY = IJFaceNodeAdapter.class;

	public BeansJFaceNodeAdapter(INodeAdapterFactory adapterFactory) {
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
	public String getLabelText(Object node) {
		return getNodeName(node);
	}

	/**
	 * Fetches the label image specific to this object instance.
	 */
	public Image getLabelImage(Object object) {
		Node node = (Node) object;
		String nodeName = node.getNodeName();

		// Root elements (alias, import and bean)
		if ("alias".equals(nodeName)) {
//			return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_ROOT_BEAN);
		}
		if ("import".equals(nodeName)) {
//			return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_ROOT_BEAN);
		}
		if ("bean".equals(nodeName)) {
			return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_ROOT_BEAN);
		}

		// Bean elements
		if ("constructor-arg".equals(nodeName)) {
			return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_CONSTRUCTOR);
		}
		if ("property".equals(nodeName)) {
			return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_PROPERTY);
		}

		// Misc elements
		if ("value".equals(nodeName)) {
//			return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_ROOT_BEAN);
		}
		if ("ref".equals(nodeName)) {
//			return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_ROOT_BEAN);
		}
		if ("description".equals(nodeName)) {
//			return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_ROOT_BEAN);
		}
		return super.getLabelImage(node);
	}

	private String getNodeName(Object object) {
		Node node = (Node) object;
		String nodeName = node.getNodeName();

		// Root elements (alias, import and bean)
		if ("alias".equals(nodeName)) {
			Node aliasNode = node.getAttributes().getNamedItem("alias");
			String alias = "";
			if (aliasNode != null) {
				alias = " \"" + aliasNode.getNodeValue() + "\"";
			}
			Node nameNode = node.getAttributes().getNamedItem("name");
			String name = "";
			if (nameNode != null) {
				name = " <" + nameNode.getNodeValue() + ">";
			}
			return "Alias" + alias + name;
		}
		if ("import".equals(nodeName)) {
			Node resourceNode = node.getAttributes().getNamedItem("resource");
			String resource = "";
			if (resourceNode != null) {
				resource = " \"" + resourceNode.getNodeValue() + "\"";
			}
			return "Import" + resource;
		}
		if ("bean".equals(nodeName)) {
			Node idNode = node.getAttributes().getNamedItem("id");
			String id;
			if (idNode != null) {
				id = " \"" + idNode.getNodeValue()+ "\"";
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
			return "Bean" + id + clazz;
		}

		// Bean elements
		if ("property".equals(nodeName)) {
			Node nameNode = node.getAttributes().getNamedItem("name");
			String name = "";
			if (nameNode != null) {
				name = " \"" + nameNode.getNodeValue()+ "\"";
			}
			Node valueNode = node.getAttributes().getNamedItem("value");
			String value = "";
			if (valueNode != null) {
				value = " [" + valueNode.getNodeValue() + "]";
			}
			return "Property" + name + value;
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
			return "Value" + type + value;
		}
		if ("ref".equals(nodeName)) {
			Node beanNode = node.getAttributes().getNamedItem("bean");
			if (beanNode == null) {
				beanNode = node.getAttributes().getNamedItem("local");
			}
			String bean = "";
			if (beanNode != null) {
				bean = " <" + beanNode.getNodeValue() + ">";
			}
			return "Ref" + bean;
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
