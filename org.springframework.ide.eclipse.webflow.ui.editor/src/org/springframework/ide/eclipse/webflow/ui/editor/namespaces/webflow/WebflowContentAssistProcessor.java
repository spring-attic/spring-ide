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
package org.springframework.ide.eclipse.webflow.ui.editor.namespaces.webflow;

import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.AbstractContentAssistProcessor;
import org.w3c.dom.Node;

@SuppressWarnings("restriction")
public class WebflowContentAssistProcessor extends AbstractContentAssistProcessor {

	@Override
	protected void computeAttributeValueProposals(ContentAssistRequest request, IDOMNode node, String matchString,
			String attributeName) {
	}

	@Override
	protected void computeTagInsertionProposals(ContentAssistRequest request, IDOMNode node) {
	}

	@Override
	protected void computeAttributeNameProposals(ContentAssistRequest request, String prefix, String namespace, String namespacePrefix, Node attributeNode) {
	}
}
