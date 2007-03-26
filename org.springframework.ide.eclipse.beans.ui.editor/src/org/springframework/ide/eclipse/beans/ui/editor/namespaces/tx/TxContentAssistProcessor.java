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
package org.springframework.ide.eclipse.beans.ui.editor.namespaces.tx;

import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.AbstractContentAssistProcessor;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.requestor.BeanReferenceSearchRequestor;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

@SuppressWarnings("restriction")
public class TxContentAssistProcessor extends AbstractContentAssistProcessor {

	private void addBeanReferenceProposals(ContentAssistRequest request,
			String prefix, Document document, boolean showExternal) {
		if (prefix == null) {
			prefix = "";
		}
		IFile file = BeansEditorUtils.getResource(request);
		if (document != null) {
			BeanReferenceSearchRequestor requestor = new BeanReferenceSearchRequestor(
					request);
			Map<String, Node> beanNodes = BeansEditorUtils
					.getReferenceableNodes(document);
			for (Map.Entry<String, Node> node : beanNodes.entrySet()) {
				Node beanNode = node.getValue();
				requestor.acceptSearchMatch(node.getKey(), beanNode, file,
						prefix);
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

	@Override
	protected void computeAttributeNameProposals(ContentAssistRequest request,
			String prefix, String namespace, String namespacePrefix,
			Node attributeNode) {
	}

	@Override
	protected void computeAttributeValueProposals(ContentAssistRequest request,
			IDOMNode node, String matchString, String attributeName) {
		String nodeName = node.getNodeName();
		String prefix = node.getPrefix();
		if (prefix != null) {
			nodeName = nodeName.substring(prefix.length() + 1);
		}

		if ("advice".equals(nodeName) || "annotation-driven".equals(nodeName)) {
			if ("transaction-manager".equals(attributeName)) {
				addBeanReferenceProposals(request, matchString, node
						.getOwnerDocument(), true);
			}
		}
	}

	@Override
	protected void computeTagInsertionProposals(ContentAssistRequest request,
			IDOMNode node) {
	}
}
