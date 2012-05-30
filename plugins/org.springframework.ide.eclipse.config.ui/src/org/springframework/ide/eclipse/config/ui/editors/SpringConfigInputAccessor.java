/*******************************************************************************
 *  Copyright (c) 2012 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.config.ui.editors;

import org.eclipse.wst.sse.ui.internal.StructuredTextViewer;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMText;
import org.springframework.ide.eclipse.config.core.formatting.ShallowFormatProcessorXML;
import org.w3c.dom.CharacterData;
import org.w3c.dom.NodeList;


/**
 * @author Leo Dos Santos
 * @since 2.3.4
 */
@SuppressWarnings("restriction")
public class SpringConfigInputAccessor {

	private final AbstractConfigEditor editor;

	private final IDOMElement input;

	private final ShallowFormatProcessorXML formatter;

	public SpringConfigInputAccessor(AbstractConfigEditor editor, IDOMElement input) {
		this.editor = editor;
		this.input = input;
		formatter = new ShallowFormatProcessorXML();
	}

	/**
	 * Sets the value on the given attribute. Does not modify the attribute if
	 * the existing value is the same as the new value.
	 * 
	 * @param attrName the attribute name
	 * @param newValue the new attribute value
	 */
	public void editAttribute(String attrName, String newValue) {
		if (input != null && input.getParentNode() != null) {
			StructuredTextViewer textView = editor.getTextViewer();
			input.getModel().beginRecording(textView);

			String existingValue = getAttributeValue(attrName);
			// If new value is empty...
			if (newValue == null || newValue.trim().equals("")) { //$NON-NLS-1$
				// ...and existing value is non-empty...
				if (existingValue != null && !existingValue.trim().equals("")) { //$NON-NLS-1$
					// ...remove the existing attribute.
					input.removeAttribute(attrName);
					formatter.formatNode(input);
				}
			}
			// If new value is non-empty...
			else {
				// ...and existing value is different...
				if (!newValue.equals(existingValue)) {
					// ...change the existing attribute.
					input.setAttribute(attrName, newValue);
					formatter.formatNode(input);
				}
			}
			input.getModel().endRecording(textView);
		}
	}

	/**
	 * Sets the text on the element displayed by the part. Does not modify the
	 * element if the existing text is the same as the new text.
	 * 
	 * @param elemValue the new element text
	 */
	public void editElement(String elemValue) {
		if (input != null) {
			NodeList list = input.getChildNodes();
			CharacterData textNode = null;

			for (int i = 0; i < list.getLength(); i++) {
				if (list.item(i) instanceof IDOMText) {
					IDOMText text = (IDOMText) list.item(i);
					if (!text.isElementContentWhitespace()) {
						// This is a naive assumption that the first text space
						// is the one we want to edit. What to do?
						textNode = text;
						break;
					}
				}
			}

			// If new value is empty...
			if (elemValue == null || elemValue.trim().equals("")) { //$NON-NLS-1$
				// ...and existing value is non-empty...
				if (textNode != null && !textNode.getData().trim().equals("")) { //$NON-NLS-1$
					// ...remove the existing element.
					input.removeChild(textNode);
				}
			}
			// If new value is non-empty...
			else {
				// ...ensure we have an existing element, and...
				if (textNode == null) {
					textNode = input.getOwnerDocument().createTextNode(""); //$NON-NLS-1$
					input.appendChild(textNode);
				}
				// ...if existing value is different...
				if (!elemValue.equals(textNode.getData())) {
					// ...change the existing element.
					textNode.setData(elemValue);
				}
			}
		}
	}

	/**
	 * Returns the value of the given attribute.
	 * 
	 * @param attr the attribute name
	 * @return value of the given attribute
	 */
	public String getAttributeValue(String attr) {
		if (input != null && input.getAttributeNode(attr) != null) {
			String value = input.getAttribute(attr);
			if (value != null) {
				return value;
			}
		}
		return ""; //$NON-NLS-1$
	}

	/**
	 * Returns the text of the element displayed by the part.
	 * 
	 * @return text of element displayed by the part
	 */
	public String getElementValue() {
		if (input != null) {
			NodeList list = input.getChildNodes();
			for (int i = 0; i < list.getLength(); i++) {
				if (list.item(i) instanceof IDOMText) {
					IDOMText text = (IDOMText) list.item(i);
					if (!text.isElementContentWhitespace()) {
						// We don't trim this text because it causes another
						// synchronization to the source page, and the cursor
						// will jump to the start of the text area.
						// Unfortunately formatted text in the source page will
						// look ugly in the text area.
						return text.getData();
					}
				}
			}
		}
		return ""; //$NON-NLS-1$
	}

}
