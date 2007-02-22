/*
 * Copyright 2002-2006 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.springframework.ide.eclipse.beans.ui.editor.namespaces.util;

import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.AbstractContentAssistProcessor;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansJavaCompletionUtils;
import org.w3c.dom.Node;

/**
 * Main entry point for the Spring beans xml editor's content assist.
 * @author Christian Dupuis
 */
@SuppressWarnings("restriction")
public class UtilContentAssistProcessor extends AbstractContentAssistProcessor {

	private void addClassAttributeValueProposals(ContentAssistRequest request,
			String prefix) {
		BeansJavaCompletionUtils.addClassValueProposals(request, prefix);
	}

	private void addCollectionTypesAttributeValueProposals(
			ContentAssistRequest request, final String prefix, String typeName) {
		BeansJavaCompletionUtils.addTypeHierachyAttributeValueProposals(
				request, prefix, typeName);
	}

	@Override
	protected void computeAttributeNameProposals(ContentAssistRequest request,
			String prefix, String namespace, String namespacePrefix,
			Node attributeNode) {
	}

	protected void computeAttributeValueProposals(ContentAssistRequest request,
			IDOMNode node, String matchString, String attributeName) {

		if ("list-class".equals(attributeName)) {
			addCollectionTypesAttributeValueProposals(request, matchString,
					"java.util.List");
		}
		else if ("map-class".equals(attributeName)) {
			addCollectionTypesAttributeValueProposals(request, matchString,
					"java.util.Map");
		}
		else if ("set-class".equals(attributeName)) {
			addCollectionTypesAttributeValueProposals(request, matchString,
					"java.util.Set");
		}
		else if ("value-type".equals(attributeName)
				|| "key-type".equals(attributeName)) {
			addClassAttributeValueProposals(request, matchString);
		}
	}

	@Override
	protected void computeTagInsertionProposals(ContentAssistRequest request,
			IDOMNode node) {
	}
}
