package org.springframework.ide.eclipse.beans.ui.editor;

import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

public interface IReferenceableElementsLocator {

	Map<String, Node> getReferenceableElements(Document documet);
	
}
