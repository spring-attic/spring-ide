/*******************************************************************************
 * Copyright (c) 2007, 2009 Spring IDE Developers
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.IContentAssistContext;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.IContentAssistProposalRecorder;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Utility class with helper methods to calculate bean reference proposals.
 * @author Christian Dupuis
 * @since 2.0
 */
public class BeansCompletionUtils {
	
	public static void addBeanReferenceProposals(IContentAssistContext context,
			IContentAssistProposalRecorder recorder, boolean showExternal) {
		addBeanReferenceProposals(context, recorder, showExternal, new ArrayList<String>());
	}

	public static void addBeanReferenceProposals(IContentAssistContext context,
			IContentAssistProposalRecorder recorder, boolean showExternal, List<String> requiredTypes) {
		String prefix = context.getMatchString();
		IFile file = context.getFile();
		Document document = context.getDocument();

		if (prefix == null) {
			prefix = "";
		}
		if (document != null) {
			BeanReferenceSearchRequestor requestor = new BeanReferenceSearchRequestor(recorder, requiredTypes);
			Map<String, Node> beanNodes = BeansEditorUtils.getReferenceableNodes(document, context.getFile());
			for (Map.Entry<String, Node> node : beanNodes.entrySet()) {
				Node beanNode = node.getValue();
				requestor.acceptSearchMatch(node.getKey(), beanNode, file, prefix);
			}
			if (showExternal) {
				Set<IBean> beansList = BeansEditorUtils.getBeansFromConfigSets(file);
				Iterator<IBean> iterator = beansList.iterator();
				while (iterator.hasNext()) {
					IBean bean = iterator.next();
					requestor.acceptSearchMatch(bean, file, prefix);
				}
			}
		}
	}

}
