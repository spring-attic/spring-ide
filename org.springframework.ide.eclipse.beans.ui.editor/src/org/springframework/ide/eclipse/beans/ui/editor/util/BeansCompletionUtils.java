/*******************************************************************************
 * Copyright (c) 2005, 2008 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.editor.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Utility class with helper methods to calculate bean reference proposals.
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public class BeansCompletionUtils {
	
	public static void addBeanReferenceProposals(ContentAssistRequest request,
			String prefix, Document document, boolean showExternal) {
		addBeanReferenceProposals(request, prefix, document, showExternal, new ArrayList<String>());
	}
	
	public static void addBeanReferenceProposals(ContentAssistRequest request,
			String prefix, Document document, boolean showExternal, List<String> requiredTypes) {
		if (prefix == null) {
			prefix = "";
		}
		IFile file = BeansEditorUtils.getFile(request);
		if (document != null) {
			BeanReferenceSearchRequestor requestor = new BeanReferenceSearchRequestor(
					request, requiredTypes);
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

}
