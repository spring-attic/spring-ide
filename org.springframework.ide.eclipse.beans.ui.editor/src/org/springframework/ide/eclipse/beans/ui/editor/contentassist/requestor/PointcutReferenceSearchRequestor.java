/*
 * Copyright 2002-2007 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ide.eclipse.beans.ui.editor.contentassist.requestor;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.swt.graphics.Image;
import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
import org.springframework.ide.eclipse.beans.ui.BeansUIImages;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.BeansJavaCompletionProposal;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
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
		if (idAttribute != null && idAttribute.getNodeValue() != null && idAttribute.getNodeValue().startsWith(prefix)) {
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
				Image image = BeansUIImages.getImage(BeansUIImages.IMG_OBJS_POINTCUT);

				BeansJavaCompletionProposal proposal = new BeansJavaCompletionProposal(replaceText, request
						.getReplacementBeginPosition(), request.getReplacementLength(), replaceText.length(), image,
						displayText, null, BeansEditorUtils.createAdditionalProposalInfo(pointcutNode, file),
						PointcutReferenceSearchRequestor.LOCAL_BEAN_RELEVANCE);

				request.addProposal(proposal);
				beans.add(key);
			}
		}
	}
}