/*
 * Copyright 2002-2005 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.springframework.ide.eclipse.beans.ui.editor.hyperlink;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.sse.core.internal.provisional.IndexedRegion;
import org.eclipse.wst.sse.core.internal.provisional.StructuredModelManager;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMAttr;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.springframework.ide.eclipse.beans.core.internal.Introspector;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.ui.editor.BeansEditorUtils;
import org.springframework.ide.eclipse.core.StringUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Detects hyperlinks in XML tags. Includes detection of bean classes and bean
 * properties in attribute values. Resolves bean references (including
 * references to parent beans or factory beans).
 */
public class BeansHyperLinkDetector implements IHyperlinkDetector {

	public IHyperlink[] detectHyperlinks(ITextViewer textViewer,
			IRegion region, boolean canShowMultipleHyperlinks) {
		if (region == null || textViewer == null) {
			return null;
		}

		IDocument document = textViewer.getDocument();
		Node currentNode = getCurrentNode(document, region.getOffset());
		if (currentNode != null) {
			short nodeType = currentNode.getNodeType();
			if (nodeType == Node.DOCUMENT_TYPE_NODE) {
				// nothing to do
			} else if (nodeType == Node.ELEMENT_NODE) {
				// element nodes
				Attr currentAttr = getCurrentAttrNode(currentNode, region
						.getOffset());
				if (currentAttr != null && this.isLinkableAttr(currentAttr)) {
					IRegion hyperlinkRegion = getHyperlinkRegion(currentAttr);
					IHyperlink hyperLink = createHyperlink(currentAttr
							.getName(), currentAttr.getNodeValue(), currentNode
							.getParentNode(), hyperlinkRegion, document,
							currentNode, textViewer, region);
					if (hyperLink != null) {
						return new IHyperlink[] { hyperLink };
					}
				}
			} else if (nodeType == Node.TEXT_NODE) {
				IRegion hyperlinkRegion = getHyperlinkRegion(currentNode);
				Node parentNode = currentNode.getParentNode();
				if (parentNode != null) {
					String name = parentNode.getNodeName();
					String target = currentNode.getNodeValue();
					IHyperlink hyperLink = createHyperlink(name, target,
							parentNode, hyperlinkRegion, document, currentNode,
							textViewer, region);
					if (hyperLink != null) {
						return new IHyperlink[] { hyperLink };
					}
				}
			}
		}
		return null;
	}

	/**
	 * Returns the attribute node within node at offset
	 */
	private Attr getCurrentAttrNode(Node node, int offset) {
		if ((node instanceof IndexedRegion)
				&& ((IndexedRegion) node).contains(offset)
				&& (node.hasAttributes())) {
			NamedNodeMap attrs = node.getAttributes();
			// go through each attribute in node and if attribute contains
			// offset, return that attribute
			for (int i = 0; i < attrs.getLength(); ++i) {
				// assumption that if parent node is of type IndexedRegion,
				// then its attributes will also be of type IndexedRegion
				IndexedRegion attRegion = (IndexedRegion) attrs.item(i);
				if (attRegion.contains(offset)) {
					return (Attr) attrs.item(i);
				}
			}
		}
		return null;
	}

	/**
	 * Returns the node the cursor is currently on in the document. null if no
	 * node is selected
	 * 
	 * @param offset
	 * @return Node either element, doctype, text, or null
	 */
	private Node getCurrentNode(IDocument document, int offset) {
		// get the current node at the offset (returns either: element,
		// doctype, text)
		IndexedRegion inode = null;
		IStructuredModel sModel = null;
		try {
			sModel = StructuredModelManager.getModelManager()
					.getExistingModelForRead(document);
			inode = sModel.getIndexedRegion(offset);
			if (inode == null)
				inode = sModel.getIndexedRegion(offset - 1);
		} finally {
			if (sModel != null)
				sModel.releaseFromRead();
		}

		if (inode instanceof Node) {
			return (Node) inode;
		}
		return null;
	}

	private IRegion getHyperlinkRegion(Node node) {
		IRegion hyperRegion = null;

		if (node != null) {
			short nodeType = node.getNodeType();
			if (nodeType == Node.DOCUMENT_TYPE_NODE
					|| nodeType == Node.ELEMENT_NODE
					|| nodeType == Node.TEXT_NODE) {
				// handle doc type node
				IDOMNode docNode = (IDOMNode) node;
				hyperRegion = new Region(docNode.getStartOffset(), docNode
						.getEndOffset()
						- docNode.getStartOffset());
			} else if (nodeType == Node.ATTRIBUTE_NODE) {
				// handle attribute nodes
				IDOMAttr att = (IDOMAttr) node;
				// do not include quotes in attribute value region
				int regOffset = att.getValueRegionStartOffset();
				int regLength = att.getValueRegionText().length();
				String attValue = att.getValueRegionText();
				if (StringUtils.isQuoted(attValue)) {
					regOffset = ++regOffset;
					regLength = regLength - 2;
				}
				hyperRegion = new Region(regOffset, regLength);
			}
		}
		return hyperRegion;
	}

	/**
	 * Checks to see if the given attribute is openable. Attribute is openable
	 * if it is a namespace declaration attribute or if the attribute value is
	 * of type URI.
	 * 
	 * @return true if this attribute is "openOn-able" false otherwise
	 */
	private boolean isLinkableAttr(Attr attr) {
		String attrName = attr.getName();

		if ("class".equals(attrName)) {
			return true;
		} else if ("name".equals(attrName)
				&& "property".equals(attr.getOwnerElement().getNodeName())) {
			return true;
		} else if ("init-method".equals(attrName)) {
			return true;
		} else if ("destroy-method".equals(attrName)) {
			return true;
		} else if ("factory-method".equals(attrName)) {
			return true;
		} else if ("factory-bean".equals(attrName)) {
			return true;
		} else if ("parent".equals(attrName)) {
			return true;
		} else if ("depends-on".equals(attrName)) {
			return true;
		} else if ("bean".equals(attrName) || "local".equals(attrName)
				|| "parent".equals(attrName) || "ref".equals(attrName)
				|| "alias".equals(attrName)) {
			return true;
		} else if ("value".equals(attrName)) {
			return true;
		}

		return false;
	}

	/**
	 * Create the appropriate hyperlink
	 */
	private IHyperlink createHyperlink(String name, String target,
			Node parentNode, IRegion hyperlinkRegion, IDocument document,
			Node node, ITextViewer textViewer, IRegion cursor) {
		IHyperlink link = null;

		if (name != null) {
			String parentName = null;
			if (parentNode != null) {
				parentName = parentNode.getNodeName();
			}
			if ("class".equals(name) || "value".equals(name)) {
				IFile file = this.getResource(document);
				IType type = BeansModelUtils.getJavaType(file.getProject(),
						target);
				if (type != null) {
					link = new JavaElementHyperlink(hyperlinkRegion, type);
				}
			} else if ("name".equals(name)
					&& "property".equals(node.getNodeName())) {

				List propertyPaths = new ArrayList();
				hyperlinkRegion = extractPropertyPathFromCursorPosition(
						hyperlinkRegion, cursor, target, propertyPaths);

				if ("bean".equals(parentName)) {
					IFile file = this.getResource(document);
					List classNames = BeansEditorUtils.getClassNamesOfBean(
							file, parentNode);

					IMethod method = extractMethodFromPropertyPathElements(
							propertyPaths, classNames, file, 0);
					if (method != null) {
						link = new JavaElementHyperlink(hyperlinkRegion, method);
					}
				}
			} else if ("init-method".equals(name)
					|| "destroy-method".equals(name)) {
				NamedNodeMap attributes = node.getAttributes();
				if (attributes != null
						&& attributes.getNamedItem("class") != null) {
					String className = attributes.getNamedItem("class")
							.getNodeValue();
					IFile file = this.getResource(document);
					IType type = BeansModelUtils.getJavaType(file.getProject(),
							className);

					try {
						IMethod method = Introspector.findMethod(type, target,
								0, true, Introspector.STATIC_IRRELVANT);
						if (method != null) {
							link = new JavaElementHyperlink(hyperlinkRegion,
									method);
						}
					} catch (JavaModelException e) {
					}
				}
			} else if ("factory-method".equals(name)) {
				NamedNodeMap attributes = node.getAttributes();
				String className = null;
				if (attributes != null
						&& attributes.getNamedItem("factory-bean") != null) {
					Node factoryBean = attributes.getNamedItem("factory-bean");
					if (factoryBean != null) {
						String factoryBeanId = factoryBean.getNodeValue();
						// TODO add factoryBean support for beans defined
						// outside of the current
						// xml file
						Document doc = node.getOwnerDocument();
						Element bean = doc.getElementById(factoryBeanId);
						if (bean != null && bean instanceof Node) {
							NamedNodeMap attribute = ((Node) bean)
									.getAttributes();
							className = attribute.getNamedItem("class")
									.getNodeValue();
						}
					}
				} else if (attributes != null
						&& attributes.getNamedItem("class") != null) {
					className = attributes.getNamedItem("class").getNodeValue();
				}
				try {
					IFile file = this.getResource(document);
					IType type = BeansModelUtils.getJavaType(file.getProject(),
							className);
					IMethod method = Introspector.findMethod(type, target, -1,
							true, Introspector.STATIC_YES);
					if (method != null) {
						link = new JavaElementHyperlink(hyperlinkRegion, method);
					}
				} catch (JavaModelException e) {
				}
			} else if ("factory-bean".equals(name) || "depends-on".equals(name)
					|| "bean".equals(name) || "local".equals(name)
					|| "parent".equals(name) || "ref".equals(name)
					|| "alias".equals(name)) {
				Document doc = node.getOwnerDocument();
				Element bean = doc.getElementById(target);
				if (bean != null) {
					IRegion region = getHyperlinkRegion(bean);
					link = new NodeElementHyperlink(hyperlinkRegion, region,
							textViewer);
				} else {
					IFile file = this.getResource(document);
					// assume this is an external reference
					Iterator beans = BeansEditorUtils.getBeansFromConfigSets(
							file).iterator();
					while (beans.hasNext()) {
						IBean modelBean = (IBean) beans.next();
						if (modelBean.getElementName().equals(target)) {
							link = new ExternalBeanHyperlink(modelBean,
									hyperlinkRegion);
						}
					}
				}
			}
		}
		return link;
	}

	/**
	 * Returns project request is in
	 * 
	 * @param request
	 * @return
	 */
	private IFile getResource(IDocument document) {
		IFile resource = null;
		String baselocation = null;

		if (document != null) {
			IStructuredModel model = null;
			try {
				model = StructuredModelManager.getModelManager()
						.getExistingModelForRead(document);
				if (model != null) {
					baselocation = model.getBaseLocation();
				}
			} finally {
				if (model != null)
					model.releaseFromRead();
			}
		}

		if (baselocation != null) {
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			IPath filePath = new Path(baselocation);
			if (filePath.segmentCount() > 0) {
				resource = root.getFile(filePath);
			}
		}
		return resource;
	}

	private IRegion extractPropertyPathFromCursorPosition(
			IRegion hyperlinkRegion, IRegion cursor, String target,
			List propertyPaths) {

		int cursorIndexInTarget = cursor.getOffset()
				- hyperlinkRegion.getOffset();
		String preTarget = target.substring(0, cursorIndexInTarget);
		if (!preTarget.endsWith(".")) {
			int regionOffset = hyperlinkRegion.getOffset()
					+ preTarget.lastIndexOf(".") + 1;
			int segmentCount = new StringTokenizer(preTarget, ".")
					.countTokens();
			StringTokenizer tok = new StringTokenizer(target, ".");

			for (int i = 0; i < segmentCount; i++) {
				propertyPaths.add(tok.nextToken());
			}

			int regionLenght = ((String) propertyPaths.get(segmentCount - 1))
					.length();

			return new Region(regionOffset, regionLenght);
		} else {
			return hyperlinkRegion;
		}
	}

	private IMethod extractMethodFromPropertyPathElements(List propertyPath,
			List types, IFile file, int counter) {
		IMethod method = null;
		if (propertyPath != null && propertyPath.size() > 0) {
			if (propertyPath.size() > (counter + 1)) {

				if (types != null) {
					IType returnType = null;
					for (int i = 0; i < types.size(); i++) {
						IType type = (IType) types.get(i);
						try {
							IMethod getMethod = Introspector
									.getReadableProperty(type,
											(String) propertyPath.get(counter));
							returnType = BeansEditorUtils
									.getTypeForMethodReturnType(getMethod,
											type, file);
						} catch (JavaModelException e) {
						}
					}

					if (returnType != null) {
						List newTypes = new ArrayList();
						newTypes.add(returnType);
						method = extractMethodFromPropertyPathElements(
								propertyPath, newTypes, file, (counter + 1));
					}

				}
			} else {
				for (int i = 0; i < types.size(); i++) {
					IType type = (IType) types.get(i);
					try {
						method = Introspector.getWritableProperty(type,
								(String) propertyPath.get(counter));

					} catch (JavaModelException e) {
					}
				}
			}
		}
		return method;
	}
}
