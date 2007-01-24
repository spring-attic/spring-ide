package org.springframework.ide.eclipse.beans.ui.editor.namespaces;

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
