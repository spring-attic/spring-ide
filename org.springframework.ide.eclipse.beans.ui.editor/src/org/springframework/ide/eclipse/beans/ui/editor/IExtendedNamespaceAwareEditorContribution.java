package org.springframework.ide.eclipse.beans.ui.editor;

import org.w3c.dom.Element;

public interface IExtendedNamespaceAwareEditorContribution extends
		INamespaceAwareEditorContribution {
	
	String getClassNameForElement(Element elem);
	
}
