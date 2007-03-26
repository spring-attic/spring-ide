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
package org.springframework.ide.eclipse.beans.ui.editor.namespaces.webflow;

import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.AbstractContentAssistProcessor;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.requestor.BeanReferenceSearchRequestor;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansJavaCompletionUtils;
import org.w3c.dom.Node;

/**
 * 
 */
@SuppressWarnings("restriction")
public class WebflowConfigContentAssistProcessor extends AbstractContentAssistProcessor {

	/**
	 * 
	 * 
	 * @param node 
	 * @param prefix 
	 * @param showExternal 
	 * @param request 
	 */
	private void addBeanReferenceProposals(ContentAssistRequest request, String prefix, Node node, boolean showExternal) {
		if (prefix == null) {
			prefix = "";
		}

		IFile file = BeansEditorUtils.getResource(request);
		if (node.getOwnerDocument() != null) {
			BeanReferenceSearchRequestor requestor = new BeanReferenceSearchRequestor(request, BeansJavaCompletionUtils
					.getPropertyTypes(node, file.getProject()));
			Map<String, Node> beanNodes = BeansEditorUtils.getReferenceableNodes(node.getOwnerDocument());
			for (Map.Entry<String, Node> n : beanNodes.entrySet()) {
				Node beanNode = n.getValue();
				requestor.acceptSearchMatch(n.getKey(), beanNode, file, prefix);
			}
			if (showExternal) {
				List<?> beans = BeansEditorUtils.getBeansFromConfigSets(file);
				for (int i = 0; i < beans.size(); i++) {
					IBean bean = (IBean) beans.get(i);
					requestor.acceptSearchMatch(bean, file, prefix);
				}
			}
		}
	}

	/**
	 * 
	 * 
	 * @param prefix 
	 * @param request 
	 */
	private void addClassAttributeValueProposals(ContentAssistRequest request, String prefix) {
		BeansJavaCompletionUtils.addClassValueProposals(request, prefix);
	}

	/**
	 * 
	 * 
	 * @param namespace 
	 * @param attributeNode 
	 * @param prefix 
	 * @param namespacePrefix 
	 * @param request 
	 */
	@Override
	protected void computeAttributeNameProposals(ContentAssistRequest request, String prefix, String namespace,
			String namespacePrefix, Node attributeNode) {
	}

	/**
	 * 
	 * 
	 * @param attributeName 
	 * @param node 
	 * @param request 
	 * @param matchString 
	 */
	@Override
	protected void computeAttributeValueProposals(ContentAssistRequest request, IDOMNode node, String matchString,
			String attributeName) {
		String nodeName = node.getNodeName();
		String prefix = node.getPrefix();
		if (prefix != null) {
			nodeName = nodeName.substring(prefix.length() + 1);
		}

		if ("executor".equals(nodeName)) {
			if ("registry-ref".equals(attributeName)) {
				addBeanReferenceProposals(request, matchString, node, true);
			}
		}
		else if ("repository".equals(nodeName)) {
			if ("conversation-manager-ref".equals(attributeName)) {
				addBeanReferenceProposals(request, matchString, node, true);
			}
		}
		else if ("listener".equals(nodeName)) {
			if ("ref".equals(attributeName)) {
				addBeanReferenceProposals(request, matchString, node, true);
			}
		}
		else if ("attribute".equals(nodeName)) {
			if ("type".equals(attributeName)) {
				addClassAttributeValueProposals(request, matchString);
			}
		}
	}

	/**
	 * 
	 * 
	 * @param node 
	 * @param request 
	 */
	@Override
	protected void computeTagInsertionProposals(ContentAssistRequest request, IDOMNode node) {
	}
}
