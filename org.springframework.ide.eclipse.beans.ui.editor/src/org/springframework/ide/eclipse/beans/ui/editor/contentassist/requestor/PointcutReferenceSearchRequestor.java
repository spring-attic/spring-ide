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
package org.springframework.ide.eclipse.beans.ui.editor.contentassist.requestor;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.swt.graphics.Image;
import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
import org.springframework.ide.eclipse.beans.ui.BeansUIImages;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.BeansJavaCompletionProposal;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

@SuppressWarnings("restriction")
public class PointcutReferenceSearchRequestor {

	public static final int LOCAL_BEAN_RELEVANCE = 20;

	protected Set<String> beans;

	protected ContentAssistRequest request;

	public PointcutReferenceSearchRequestor(ContentAssistRequest request) {
		this.request = request;
		this.beans = new HashSet<String>();
	}

	public void acceptSearchMatch(Node pointcutNode, IFile file, String prefix) {
		NamedNodeMap attributes = pointcutNode.getAttributes();
		Node idAttribute = attributes.getNamedItem("id");
		if (idAttribute != null && idAttribute.getNodeValue() != null
				&& idAttribute.getNodeValue().startsWith(prefix)) {
			String pointcutName = idAttribute.getNodeValue();
			String replaceText = pointcutName;
			String fileName = file.getProjectRelativePath().toString();
			String key = pointcutName + fileName;
			if (!beans.contains(key)) {
				StringBuffer buf = new StringBuffer();
				Node parentNode = pointcutNode.getParentNode();
				buf.append(pointcutName);
				if (parentNode != null) {
					buf.append(" [");
					buf.append(parentNode.getNodeName());
					buf.append("]");
				}

				String displayText = buf.toString();
				Image image = BeansUIImages
						.getImage(BeansUIImages.IMG_OBJS_POINTCUT);

				BeansJavaCompletionProposal proposal = new BeansJavaCompletionProposal(
						replaceText, request.getReplacementBeginPosition(),
						request.getReplacementLength(), replaceText.length(),
						image, displayText, null,
						PointcutReferenceSearchRequestor.LOCAL_BEAN_RELEVANCE,
						pointcutNode);

				request.addProposal(proposal);
				beans.add(key);
			}
		}
	}
}
