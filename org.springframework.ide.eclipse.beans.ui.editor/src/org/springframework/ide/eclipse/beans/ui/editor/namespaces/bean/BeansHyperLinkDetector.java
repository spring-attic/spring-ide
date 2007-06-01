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
package org.springframework.ide.eclipse.beans.ui.editor.namespaces.bean;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.AbstractHyperLinkDetector;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.ExternalBeanHyperlink;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.JavaElementHyperlink;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.NodeElementHyperlink;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.springframework.ide.eclipse.core.java.Introspector;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.ide.eclipse.core.java.Introspector.Public;
import org.springframework.ide.eclipse.core.java.Introspector.Static;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Detects hyperlinks in XML tags. Includes detection of bean classes and bean
 * properties in attribute values. Resolves bean references (including
 * references to parent beans or factory beans).
 * 
 * @author Christian Dupuis
 * @author Torsten Juergeleit
 */
public class BeansHyperLinkDetector extends AbstractHyperLinkDetector implements
		IHyperlinkDetector {

	/**
	 * Returns <code>true</code> if given attribute is openable.
	 */
	@Override
	protected boolean isLinkableAttr(Attr attr) {
		String attrName = attr.getName();
		String ownerName = attr.getOwnerElement().getNodeName();
		if ("class".equals(attrName)) {
			return true;
		}
		else if ("name".equals(attrName) && "property".equals(ownerName)) {
			return true;
		}
		else if ("init-method".equals(attrName)) {
			return true;
		}
		else if ("destroy-method".equals(attrName)) {
			return true;
		}
		else if ("factory-method".equals(attrName)) {
			return true;
		}
		else if ("factory-bean".equals(attrName)) {
			return true;
		}
		else if ("parent".equals(attrName)) {
			return true;
		}
		else if ("depends-on".equals(attrName)) {
			return true;
		}
		else if ("bean".equals(attrName) || "local".equals(attrName)
				|| "parent".equals(attrName) || "ref".equals(attrName)
				|| ("name".equals(attrName) && "alias".equals(ownerName))) {
			return true;
		}
		else if ("value".equals(attrName)) {
			return true;
		}
		else if ("value-ref".equals(attrName) || "key-ref".equals(attrName)) {
			return true;
		}
		else if ("http://www.springframework.org/schema/p".equals(attr
				.getNamespaceURI())
				&& attrName.endsWith("-ref")) {
			return true;
		}
		return false;
	}

	@Override
	protected IHyperlink createHyperlink(String name, String target,
			Node parentNode, IRegion hyperlinkRegion, IDocument document,
			Node node, ITextViewer textViewer, IRegion cursor) {
		if (name == null) {
			return null;
		}
		String parentName = null;
		if (parentNode != null) {
			parentName = parentNode.getNodeName();
		}
		if ("class".equals(name) || "value".equals(name)) {
			IFile file = BeansEditorUtils.getFile(document);
			IType type = JdtUtils.getJavaType(file.getProject(), target);
			if (type != null) {
				return new JavaElementHyperlink(hyperlinkRegion, type);
			}
		}
		else if ("name".equals(name) && "property".equals(node.getNodeName())) {
			List<String> propertyPaths = new ArrayList<String>();
			hyperlinkRegion = BeansEditorUtils
					.extractPropertyPathFromCursorPosition(hyperlinkRegion,
							cursor, target, propertyPaths);
			if ("bean".equals(parentName)) {
				IFile file = BeansEditorUtils.getFile(document);
				List<?> classNames = BeansEditorUtils.getClassNamesOfBean(file,
						parentNode);

				IMethod method = BeansEditorUtils
						.extractMethodFromPropertyPathElements(propertyPaths,
								classNames, file, 0);
				if (method != null) {
					return new JavaElementHyperlink(hyperlinkRegion, method);
				}
			}
		}
		else if ("init-method".equals(name) || "destroy-method".equals(name)) {
			NamedNodeMap attributes = node.getAttributes();
			if (attributes != null && attributes.getNamedItem("class") != null) {
				String className = attributes.getNamedItem("class")
						.getNodeValue();
				IFile file = BeansEditorUtils.getFile(document);
				IType type = JdtUtils.getJavaType(file.getProject(),
						className);
				try {
					IMethod method = Introspector.findMethod(type, target, 0,
							Public.DONT_CARE, Static.DONT_CARE);
					if (method != null) {
						return new JavaElementHyperlink(hyperlinkRegion, method);
					}
				}
				catch (JavaModelException e) {
				}
			}
		}
		else if ("factory-method".equals(name)) {
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
						NamedNodeMap attribute = ((Node) bean).getAttributes();
						className = attribute.getNamedItem("class")
								.getNodeValue();
					}
				}
			}
			else if (attributes != null
					&& attributes.getNamedItem("class") != null) {
				className = attributes.getNamedItem("class").getNodeValue();
			}
			try {
				IFile file = BeansEditorUtils.getFile(document);
				IType type = JdtUtils.getJavaType(file.getProject(),
						className);
				IMethod method = Introspector.findMethod(type, target, -1,
						Public.DONT_CARE, Static.YES);
				if (method != null) {
					return new JavaElementHyperlink(hyperlinkRegion, method);
				}
			}
			catch (JavaModelException e) {
			}
		}
		else if ("factory-bean".equals(name) || "depends-on".equals(name)
				|| "bean".equals(name) || "local".equals(name)
				|| "parent".equals(name) || "ref".equals(name)
				|| "name".equals(name) || "key-ref".equals(name)
				|| "value-ref".equals(name) || name.endsWith("-ref")) {
			Node bean = BeansEditorUtils.getFirstReferenceableNodeById(node
					.getOwnerDocument(), target);
			if (bean != null) {
				IRegion region = getHyperlinkRegion(bean);
				return new NodeElementHyperlink(hyperlinkRegion, region,
						textViewer);
			}
			else {
				IFile file = BeansEditorUtils.getFile(document);
				// assume this is an external reference
				Iterator<?> beans = BeansEditorUtils.getBeansFromConfigSets(
						file).iterator();
				while (beans.hasNext()) {
					IBean modelBean = (IBean) beans.next();
					if (modelBean.getElementName().equals(target)) {
						return new ExternalBeanHyperlink(modelBean,
								hyperlinkRegion);
					}
				}
			}
		}
		return null;
	}
}
