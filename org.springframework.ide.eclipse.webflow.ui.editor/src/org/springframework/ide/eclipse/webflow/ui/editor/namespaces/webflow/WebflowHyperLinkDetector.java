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
package org.springframework.ide.eclipse.webflow.ui.editor.namespaces.webflow;

import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.AbstractHyperLinkDetector;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.ExternalBeanHyperlink;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.JavaElementHyperlink;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.NodeElementHyperlink;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.springframework.ide.eclipse.core.java.Introspector;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.ide.eclipse.webflow.core.Activator;
import org.springframework.ide.eclipse.webflow.core.internal.model.WebflowModelUtils;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowConfig;
import org.w3c.dom.Attr;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Detects hyperlinks in XML tags. Includes detection of bean classes and bean
 * properties in attribute values. Resolves bean references (including
 * references to parent beans or factory beans).
 * @author Christian Dupuis
 */
public class WebflowHyperLinkDetector extends AbstractHyperLinkDetector
		implements IHyperlinkDetector {

	/**
	 * 
	 */
	private static final Set<String> VALID_ATTRIBUTES;

	static {
		VALID_ATTRIBUTES = new LinkedHashSet<String>();
		VALID_ATTRIBUTES.add("bean");
		VALID_ATTRIBUTES.add("method");
		VALID_ATTRIBUTES.add("to");
		VALID_ATTRIBUTES.add("on-exception");
		VALID_ATTRIBUTES.add("from");
		VALID_ATTRIBUTES.add("to");
		VALID_ATTRIBUTES.add("idref");
		VALID_ATTRIBUTES.add("class");
		VALID_ATTRIBUTES.add("then");
		VALID_ATTRIBUTES.add("else");
		VALID_ATTRIBUTES.add("type");
	}

	/**
	 * Returns <code>true</code> if given attribute is openable.
	 * @param attr
	 * @return
	 */
	@Override
	protected boolean isLinkableAttr(Attr attr) {
		return VALID_ATTRIBUTES.contains(attr.getLocalName());
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.beans.ui.editor.hyperlink.AbstractHyperLinkDetector#createHyperlink(java.lang.String,
	 * java.lang.String, org.w3c.dom.Node, org.eclipse.jface.text.IRegion,
	 * org.eclipse.jface.text.IDocument, org.w3c.dom.Node,
	 * org.eclipse.jface.text.ITextViewer, org.eclipse.jface.text.IRegion)
	 */
	@Override
	protected IHyperlink createHyperlink(String name, String target,
			Node parentNode, IRegion hyperlinkRegion, IDocument document,
			Node node, ITextViewer textViewer, IRegion cursor) {
		if (name == null) {
			return null;
		}
		if ("bean".equals(name)) {
			IFile file = BeansEditorUtils.getFile(document);
			IWebflowConfig config = Activator.getModel().getProject(
					file.getProject()).getConfig(file);
			if (config != null) {
				Set<IBean> beans = WebflowModelUtils.getBeans(config);
				for (IBean bean : beans) {
					if (bean.getElementName().equals(target)) {
						return new ExternalBeanHyperlink(bean, hyperlinkRegion);
					}
				}
			}
		}
		else if (("to".equals(name) && "transition".equals(node.getLocalName()))
				|| "then".equals(name)
				|| "else".equals(name)
				|| "idref".equals(name)) {
			Node flowNode = WebflowNamespaceUtils.locateFlowRootNode(node);
			NodeList nodes = flowNode.getChildNodes();
			if (nodes.getLength() > 0) {
				for (int i = 0; i < nodes.getLength(); i++) {
					String id = BeansEditorUtils.getAttribute(nodes.item(i),
							"id");
					if (target.equals(id)) {
						IRegion region = getHyperlinkRegion(nodes.item(i));
						return new NodeElementHyperlink(hyperlinkRegion,
								region, textViewer);
					}
				}
			}
		}
		else if (("to".equals(name) && !"transition"
				.equals(node.getLocalName()))
				|| "on-exception".equals(name)
				|| "type".equals(name)
				|| "type".equals(name) || "class".equals(name)) {
			IFile file = BeansEditorUtils.getFile(document);
			IType type = JdtUtils.getJavaType(file.getProject(), target);
			if (type != null) {
				return new JavaElementHyperlink(hyperlinkRegion, type);
			}
		}
		else if ("method".equals(name)
				&& BeansEditorUtils.hasAttribute(node, "bean")) {
			String bean = BeansEditorUtils.getAttribute(node, "bean");
			IFile file = BeansEditorUtils.getFile(document);
			String className = null;
			IWebflowConfig config = Activator.getModel().getProject(
					file.getProject()).getConfig(file);
			if (config != null) {
				Set<IBean> beans = WebflowModelUtils.getBeans(config);
				for (IBean modelBean : beans) {
					if (modelBean.getElementName().equals(bean)) {
						className = BeansModelUtils.getBeanClass(modelBean,
								null);
					}
				}
				IType type = JdtUtils.getJavaType(file.getProject(),
						className);
				if (type != null) {
					try {
						Set<IMethod> methods = Introspector.getAllMethods(type);
						if (methods != null) {
							for (IMethod method : methods) {
								if (method.getElementName().equals(target)) {
									return new JavaElementHyperlink(
											hyperlinkRegion, method);
								}
							}
						}
					}
					catch (JavaModelException e) {
					}
				}
			}
		}
		return null;
	}
}
