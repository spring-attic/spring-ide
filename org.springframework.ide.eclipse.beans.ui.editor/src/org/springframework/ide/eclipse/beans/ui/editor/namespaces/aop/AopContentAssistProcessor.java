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

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.AbstractContentAssistProcessor;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.requestor.PointcutReferenceSearchRequestor;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.requestor.PublicMethodSearchRequestor;
import org.springframework.ide.eclipse.beans.ui.editor.namespaces.INamespaceContentAssistProcessor;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansCompletionUtils;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansJavaCompletionUtils;
import org.springframework.ide.eclipse.core.java.Introspector;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.ide.eclipse.core.java.Introspector.Public;
import org.springframework.ide.eclipse.core.java.Introspector.Static;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Main entry point for the Spring beans xml editor's content assist.
 */
@SuppressWarnings("restriction")
public class AopContentAssistProcessor extends AbstractContentAssistProcessor
		implements INamespaceContentAssistProcessor {

	private void addBeanReferenceProposals(ContentAssistRequest request,
			String prefix, Document document, boolean showExternal) {
		BeansCompletionUtils.addBeanReferenceProposals(request, prefix,
				document.getOwnerDocument(), showExternal);
	}

	@Override
	protected void computeAttributeValueProposals(ContentAssistRequest request,
			IDOMNode node, String matchString, String attributeName) {

		String nodeName = node.getNodeName();
		String prefix = node.getPrefix();
		if (prefix != null) {
			nodeName = nodeName.substring(prefix.length() + 1);
		}

		if ("aspect".equals(nodeName)) {
			if ("ref".equals(attributeName)) {
				addBeanReferenceProposals(request, matchString, node
						.getOwnerDocument(), true);
			}
		}
		else if ("advisor".equals(nodeName)) {
			if ("advice-ref".equals(attributeName)) {
				addBeanReferenceProposals(request, matchString, node
						.getOwnerDocument(), true);
			}
		}
		if ("pointcut-ref".equals(attributeName)) {
			addPointcutReferenceProposals(request, matchString, node, node
					.getOwnerDocument());
		}
		if ("default-impl".equals(attributeName)) {
			String implementInterface = BeansEditorUtils.getAttribute(node,
					"implement-interface");
			if (StringUtils.hasText(implementInterface)) {
				addCollectionTypesAttributeValueProposals(request, matchString,
						implementInterface);
			}
			else {
				addClassAttributeValueProposals(request, matchString, false);
			}
		}
		if ("implement-interface".equals(attributeName)) {
			addClassAttributeValueProposals(request, matchString, true);
		}
		if ("method".equals(attributeName)
				&& "aspect".equals(node.getParentNode().getLocalName())
				&& BeansEditorUtils.hasAttribute(node.getParentNode(), "ref")) {
			addMethodAttributeValueProposals(request, matchString, node);
		}
	}

	private void addClassAttributeValueProposals(ContentAssistRequest request,
			String prefix, boolean interfaceRequired) {
		BeansJavaCompletionUtils.addClassValueProposals(request, prefix,
				interfaceRequired);
	}

	private void addCollectionTypesAttributeValueProposals(
			ContentAssistRequest request, final String prefix, String typeName) {
		BeansJavaCompletionUtils.addTypeHierachyAttributeValueProposals(
				request, prefix, typeName);
	}

	private void addPointcutReferenceProposals(ContentAssistRequest request,
			String prefix, IDOMNode node, Document document) {
		if (prefix == null) {
			prefix = "";
		}
		IFile file = BeansEditorUtils.getResource(request);
		if (document != null) {
			PointcutReferenceSearchRequestor requestor = new PointcutReferenceSearchRequestor(
					request);
			searchPointcutElements(prefix, node.getParentNode(), requestor,
					file);
			searchPointcutElements(prefix,
					node.getParentNode().getParentNode(), requestor, file);
		}
	}

	private void searchPointcutElements(String prefix, Node node,
			PointcutReferenceSearchRequestor requestor, IFile file) {
		NodeList beanNodes = node.getChildNodes();
		for (int i = 0; i < beanNodes.getLength(); i++) {
			Node beanNode = beanNodes.item(i);
			if ("pointcut".equals(beanNode.getLocalName())) {
				requestor.acceptSearchMatch(beanNode, file, prefix);
			}
		}
	}

	private void addMethodAttributeValueProposals(ContentAssistRequest request,
			String prefix, IDOMNode node) {

		Node parentNode = node.getParentNode();
		String ref = BeansEditorUtils.getAttribute(parentNode, "ref");

		if (ref != null) {
			IFile file = BeansEditorUtils.getResource(request);
			String className = BeansEditorUtils.getClassNameForBean(file, node
					.getOwnerDocument(), ref);
			IType type = JdtUtils.getJavaType(file.getProject(),
					className);
			if (type != null) {
				try {
					Collection<?> methods = Introspector.findAllMethods(type,
							prefix, -1, Public.YES, Static.DONT_CARE);
					if (methods != null && methods.size() > 0) {
						PublicMethodSearchRequestor requestor = new PublicMethodSearchRequestor(
								request);
						Iterator<?> iterator = methods.iterator();
						while (iterator.hasNext()) {
							requestor.acceptSearchMatch((IMethod) iterator
									.next());
						}
					}
				}
				catch (JavaModelException e1) {
					// do nothing
				}
				catch (CoreException e) {
					// // do nothing
				}
			}
		}
	}

	@Override
	protected void computeAttributeNameProposals(ContentAssistRequest request,
			String prefix, String namespace, String namespacePrefix,
			Node attributeNode) {

	}

	@Override
	protected void computeTagInsertionProposals(ContentAssistRequest request,
			IDOMNode node) {

	}
}
