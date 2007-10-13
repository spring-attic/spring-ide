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
package org.springframework.ide.eclipse.beans.ui.editor.namespaces.aop;

import java.util.Iterator;

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
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.AbstractHyperlinkDetector_;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.ExternalBeanHyperlink;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.JavaElementHyperlink;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.NodeElementHyperlink;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.springframework.ide.eclipse.core.java.Introspector;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.ide.eclipse.core.java.Introspector.Public;
import org.springframework.ide.eclipse.core.java.Introspector.Static;
import org.w3c.dom.Attr;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Detects hyperlinks in XML tags. Includes detection of bean classes and bean
 * properties in attribute values. Resolves bean references (including
 * references to parent beans or factory beans).
 * 
 * @author Christian Dupuis
 */
public class AopHyperLinkDetector extends AbstractHyperlinkDetector_ implements
		IHyperlinkDetector {

	/**
	 * Returns <code>true</code> if given attribute is openable.
	 */
	@Override
	protected boolean isLinkableAttr(Attr attr) {
		String attrName = attr.getName();
		return ("method".equals(attrName) || "ref".equals(attrName)
				|| "pointcut-ref".equals(attrName)
				|| "advice-ref".equals(attrName) || "delegate-ref".equals(attrName)
				|| "implement-interface".equals(attrName) || "default-impl"
				.equals(attrName));
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
			parentName = parentNode.getLocalName();
		}
		if ("implement-interface".equals(name) || "default-impl".equals(name)) {
			IFile file = BeansEditorUtils.getFile(document);
			IType type = JdtUtils.getJavaType(file.getProject(), target);
			if (type != null) {
				return new JavaElementHyperlink(hyperlinkRegion, type);
			}
		}
		else if ("method".equals(name) && "aspect".equals(parentName)) {
			if (BeansEditorUtils.hasAttribute(parentNode, "ref")) {
				String ref = BeansEditorUtils.getAttribute(parentNode, "ref");

				if (ref != null) {
					IFile file = BeansEditorUtils.getFile(document);
					String className = BeansEditorUtils.getClassNameForBean(
							file, node.getOwnerDocument(), ref);
					IType type = JdtUtils.getJavaType(file.getProject(),
							className);
					try {
						IMethod method = Introspector.findMethod(type, target,
								-1, Public.DONT_CARE, Static.DONT_CARE);
						if (method != null) {
							return new JavaElementHyperlink(hyperlinkRegion,
									method);
						}
					}
					catch (JavaModelException e) {
					}
				}
			}
		}
		else if ("pointcut-ref".equals(name) && parentNode != null) {
			IHyperlink hyperlink = searchPointcutElements(target, parentNode,
					textViewer, hyperlinkRegion);
			if (hyperlink == null && parentNode.getParentNode() != null) {
				hyperlink = searchPointcutElements(target, parentNode
						.getParentNode(), textViewer, hyperlinkRegion);
			}
			return hyperlink;
		}
		else if ("advice-ref".equals(name) || "ref".equals(name)) {
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

	private IHyperlink searchPointcutElements(String name, Node node,
			ITextViewer textViewer, IRegion hyperlinkRegion) {
		NodeList beanNodes = node.getChildNodes();
		for (int i = 0; i < beanNodes.getLength(); i++) {
			Node beanNode = beanNodes.item(i);
			if ("pointcut".equals(beanNode.getLocalName())) {
				if (name.equals(BeansEditorUtils.getAttribute(beanNode, "id"))) {
					IRegion region = getHyperlinkRegion(beanNode);
					return new NodeElementHyperlink(hyperlinkRegion, region,
							textViewer);
				}
			}
		}
		return null;
	}
}
