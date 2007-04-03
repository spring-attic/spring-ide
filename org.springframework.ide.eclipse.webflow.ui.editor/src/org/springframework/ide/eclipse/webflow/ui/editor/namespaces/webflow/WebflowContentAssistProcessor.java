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

import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
import org.springframework.ide.eclipse.beans.core.internal.Introspector;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.AbstractContentAssistProcessor;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.requestor.BeanReferenceSearchRequestor;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansJavaCompletionUtils;
import org.springframework.ide.eclipse.webflow.core.Activator;
import org.springframework.ide.eclipse.webflow.core.internal.model.WebflowModelUtils;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowConfig;
import org.springframework.ide.eclipse.webflow.core.util.BeanActionMethodSearchRequestor;
import org.springframework.ide.eclipse.webflow.core.util.BeanMethodSearchRequestor;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 
 */
@SuppressWarnings("restriction")
public class WebflowContentAssistProcessor extends
		AbstractContentAssistProcessor {

	/**
	 * @param prefix
	 * @param request
	 */
	private void addBeanReferenceProposals(ContentAssistRequest request,
			String prefix) {
		if (prefix == null) {
			prefix = "";
		}

		IFile file = BeansEditorUtils.getResource(request);
		BeanReferenceSearchRequestor requestor = new BeanReferenceSearchRequestor(
				request);

		IWebflowConfig config = Activator.getModel().getProject(
				file.getProject()).getConfig(file);
		if (config != null) {
			Set<IBean> beans = WebflowModelUtils.getBeans(config);
			for (IBean bean : beans) {
				requestor.acceptSearchMatch(bean, file, prefix);
			}
		}
	}

	/**
	 * @param node
	 * @param prefix
	 * @param request
	 */
	private void addStateReferenceProposals(ContentAssistRequest request,
			String prefix, Node node) {
		if (prefix == null) {
			prefix = "";
		}

		Node flowNode = WebflowNamespaceUtils.locateFlowRootNode(node);
		NodeList nodes = flowNode.getChildNodes();
		if (nodes.getLength() > 0) {
			StateReferenceSearchRequestor requestor = new StateReferenceSearchRequestor(
					request);
			IFile file = BeansEditorUtils.getResource(request);
			for (int i = 0; i < nodes.getLength(); i++) {
				requestor.acceptSearchMatch(nodes.item(i), file, prefix);
			}
		}
	}

	/**
	 * @param prefix
	 * @param request
	 */
	private void addClassAttributeValueProposals(ContentAssistRequest request,
			String prefix) {
		BeansJavaCompletionUtils.addClassValueProposals(request, prefix);
	}

	/**
	 * @param type
	 * @param prefix
	 * @param request
	 */
	private void addActionMethodAttributeValueProposals(
			ContentAssistRequest request, String prefix, IType type) {
		try {
			Set<IMethod> methods = Introspector.getAllMethods(type);
			if (methods != null) {
				BeanActionMethodSearchRequestor requestor = new BeanActionMethodSearchRequestor(
						request);
				for (IMethod method : methods) {
					requestor.acceptSearchMatch(method, prefix);
				}
			}
		}
		catch (JavaModelException e) {
		}
		catch (CoreException e) {
		}
	}

	/**
	 * @param type
	 * @param prefix
	 * @param request
	 */
	private void addMethodAttributeValueProposals(ContentAssistRequest request,
			String prefix, IType type) {
		try {
			Set<IMethod> methods = Introspector.getAllMethods(type);
			if (methods != null) {
				BeanMethodSearchRequestor requestor = new BeanMethodSearchRequestor(
						request);
				for (IMethod method : methods) {
					requestor.acceptSearchMatch(method, prefix);
				}
			}
		}
		catch (JavaModelException e) {
		}
		catch (CoreException e) {
		}
	}

	/**
	 * @param prefix
	 * @param request
	 */
	private void addExceptionTypesAttributeValueProposals(
			ContentAssistRequest request, final String prefix) {
		BeansJavaCompletionUtils.addTypeHierachyAttributeValueProposals(
				request, prefix, Throwable.class.getName());
	}

	/**
	 * @param attributeName
	 * @param node
	 * @param request
	 * @param matchString
	 */
	@Override
	protected void computeAttributeValueProposals(ContentAssistRequest request,
			IDOMNode node, String matchString, String attributeName) {
		String nodeName = node.getLocalName();
		if ("action".equals(nodeName)) {
			// bean
			if ("bean".equals(attributeName)) {
				addBeanReferenceProposals(request, matchString);
			}
			// method
			else if ("method".equals(attributeName)
					&& BeansEditorUtils.hasAttribute(node, "bean")) {
				String className = null;
				IWebflowConfig config = Activator.getModel().getProject(
						BeansEditorUtils.getResource(request).getProject())
						.getConfig(BeansEditorUtils.getResource(request));
				if (config != null) {
					Set<IBean> beans = WebflowModelUtils.getBeans(config);
					for (IBean bean : beans) {
						if (bean.getElementName().equals(
								BeansEditorUtils.getAttribute(node, "bean"))) {
							className = BeansModelUtils
									.getBeanClass(bean, null);
						}
					}
					IType type = BeansModelUtils.getJavaType(BeansEditorUtils
							.getResource(request).getProject(), className);
					if (type != null) {
						addActionMethodAttributeValueProposals(request,
								matchString, type);
					}
				}
			}
		}
		else if ("bean-action".equals(nodeName)) {
			// bean
			if ("bean".equals(attributeName)) {
				addBeanReferenceProposals(request, matchString);
			}
			// method
			else if ("method".equals(attributeName)
					&& BeansEditorUtils.hasAttribute(node, "bean")) {
				String className = null;
				IWebflowConfig config = Activator.getModel().getProject(
						BeansEditorUtils.getResource(request).getProject())
						.getConfig(BeansEditorUtils.getResource(request));
				if (config != null) {
					Set<IBean> beans = WebflowModelUtils.getBeans(config);
					for (IBean bean : beans) {
						if (bean.getElementName().equals(
								BeansEditorUtils.getAttribute(node, "bean"))) {
							className = BeansModelUtils
									.getBeanClass(bean, null);
						}
					}
					IType type = BeansModelUtils.getJavaType(BeansEditorUtils
							.getResource(request).getProject(), className);
					if (type != null) {
						addMethodAttributeValueProposals(request, matchString,
								type);
					}
				}
			}
		}
		else if ("transition".equals(nodeName)) {
			// to
			if ("to".equals(attributeName)) {
				addStateReferenceProposals(request, matchString, node);
			}
			// on-exception
			else if ("on-exception".equals(attributeName)) {
				addExceptionTypesAttributeValueProposals(request, matchString);
			}

		}
		else if ("argument".equals(nodeName)) {
			// parameter-type
			if ("parameter-type".equals(attributeName)) {
				addClassAttributeValueProposals(request, matchString);
			}
		}
		else if ("mapping".equals(nodeName)) {
			// to
			if ("to".equals(attributeName)) {
				addClassAttributeValueProposals(request, matchString);
			}
			// from
			else if ("from".equals(attributeName)) {
				addClassAttributeValueProposals(request, matchString);
			}
		}
		else if ("start-state".equals(nodeName)) {
			// idref
			if ("idref".equals(attributeName)) {
				addStateReferenceProposals(request, matchString, node);
			}
		}
		else if ("var".equals(nodeName)) {
			// bean
			if ("bean".equals(attributeName)) {
				addBeanReferenceProposals(request, matchString);
			}
			// class
			else if ("class".equals(attributeName)) {
				addClassAttributeValueProposals(request, matchString);
			}
		}
		else if ("if".equals(nodeName)) {
			// then
			if ("then".equals(attributeName)) {
				addStateReferenceProposals(request, matchString, node);
			}
			// else
			else if ("else".equals(attributeName)) {
				addStateReferenceProposals(request, matchString, node);
			}
		}
		else if ("exception-handler".equals(nodeName)) {
			// bean
			if ("bean".equals(attributeName)) {
				addBeanReferenceProposals(request, matchString);
			}
		}
		else if ("attribute".equals(nodeName)) {
			// type
			if ("type".equals(attributeName)) {
				addClassAttributeValueProposals(request, matchString);
			}
		}
	}

	/**
	 * @param node
	 * @param request
	 */
	@Override
	protected void computeTagInsertionProposals(ContentAssistRequest request,
			IDOMNode node) {
	}

	/**
	 * @param namespace
	 * @param attributeNode
	 * @param prefix
	 * @param namespacePrefix
	 * @param request
	 */
	@Override
	protected void computeAttributeNameProposals(ContentAssistRequest request,
			String prefix, String namespace, String namespacePrefix,
			Node attributeNode) {
	}
}
