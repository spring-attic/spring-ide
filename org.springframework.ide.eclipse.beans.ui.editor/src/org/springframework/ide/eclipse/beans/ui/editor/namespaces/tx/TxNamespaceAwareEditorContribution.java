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
package org.springframework.ide.eclipse.beans.ui.editor.namespaces.tx;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.jface.viewers.LabelProvider;
import org.springframework.ide.eclipse.beans.ui.editor.IExtendedNamespaceAwareEditorContribution;
import org.springframework.ide.eclipse.beans.ui.editor.IReferenceableNodesLocator;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.INamespaceContentAssistProcessor;
import org.springframework.ide.eclipse.beans.ui.editor.outline.BeansContentOutlineConfiguration;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.w3c.dom.Element;

public class TxNamespaceAwareEditorContribution implements
		IExtendedNamespaceAwareEditorContribution {

	private static final String NAMESPACE_URI = "http://www.springframework.org/schema/tx";
	
	private static Map<String, String> elementToClassNameMapping;
	
	static {
		elementToClassNameMapping = new HashMap<String, String>();
		elementToClassNameMapping.put("advice", TransactionInterceptor.class.getName());
	}
	
	public String getClassNameForElement(Element elem) {
		return elementToClassNameMapping.get(elem.getLocalName());
	}

	public INamespaceContentAssistProcessor getContentAssistProcessor() {
		return null;
	}

	public IHyperlinkDetector getHyperLinkDetector() {
		return null;
	}

	public LabelProvider getLabelProvider(BeansContentOutlineConfiguration configuration) {
		return null;
	}

	public String getNamespaceUri() {
		return NAMESPACE_URI;
	}

	public IReferenceableNodesLocator getReferenceableElementsLocator() {
		return null;
	}

}
