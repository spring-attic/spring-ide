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
package org.springframework.ide.eclipse.internal.bestpractices.quickfix;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IMarkerResolution2;
import org.eclipse.wst.sse.core.internal.format.IStructuredFormatProcessor;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.sse.ui.StructuredTextEditor;
import org.eclipse.wst.xml.core.internal.cleanup.CleanupProcessorXML;
import org.eclipse.wst.xml.core.internal.document.ElementImpl;
import org.eclipse.wst.xml.core.internal.provisional.format.FormatProcessorXML;
import org.eclipse.wst.xml.ui.internal.tabletree.XMLMultiPageEditorPart;
import org.springsource.ide.eclipse.commons.core.StatusHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;


/**
 * Resolution property setters that are required due to an
 * <code>@required</code> annotation.
 * @author Wesley Coelho
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
public class CreateRequiredMethodMarkerResolution implements IMarkerResolution2 {

	private static final String MESSAGE_ATTRIBUTE_KEY = "message";

	private String property = "";

	private String bean = "";

	public CreateRequiredMethodMarkerResolution(IMarker marker) {
		String markerMessage = marker.getAttribute(MESSAGE_ATTRIBUTE_KEY, "");
		property = extractQuotedString("Property '", markerMessage);
		bean = extractQuotedString("bean '", markerMessage);
	}

	private String extractQuotedString(String startTag, String message) {
		int startPos = message.indexOf(startTag) + startTag.length();
		int endPos = message.indexOf("'", startPos);
		return message.substring(startPos, endPos);
	}

	public String getDescription() {
		return "Create property '" + property + "' in bean '" + bean + "'";
	}

	public Image getImage() {
		return null;
	}

	public String getLabel() {
		return "Create property '" + property + "' in bean '" + bean + "'";
	}

	public void run(IMarker marker) {
		IStructuredModel model = null;
		Element beanElement = null;

		try {
			model = XmlQuickFixUtil.getModel(marker);
			beanElement = XmlQuickFixUtil.getMarkerElement(model, marker);

			if (model != null) {
				if (beanElement != null) {
					Document document = beanElement.getOwnerDocument();

					Text spacerNode = document.createTextNode("\n");
					beanElement.appendChild(spacerNode);

					Element newBeanProperty = document.createElement("property");
					newBeanProperty.setAttribute("name", property);
					newBeanProperty.setAttribute("value", "");
					newBeanProperty = (Element) beanElement.appendChild(newBeanProperty);

					spacerNode = document.createTextNode("\n\n");
					beanElement.appendChild(spacerNode);

					IStructuredFormatProcessor formatProcessor = new FormatProcessorXML();
					formatProcessor.formatNode(newBeanProperty);

					IEditorPart editor = XmlQuickFixUtil.getMarkedEditor(marker);
					setCursorPositionToPropertyValue((XMLMultiPageEditorPart) editor, (ElementImpl) newBeanProperty);

					CleanupProcessorXML cleanupProcessor = new CleanupProcessorXML();
					cleanupProcessor.getCleanupPreferences().setCompressEmptyElementTags(true);
					cleanupProcessor.cleanupNode(newBeanProperty);

					XmlQuickFixUtil.saveMarkedFile(marker);
				}
			}
		}
		catch (CoreException e) {
			StatusHandler.log(e.getStatus());
		}
		finally {
			if (model != null) {
				model.releaseFromEdit();
			}
		}
	}

	/**
	 * Attempts to set the cursor position to the value attribute of the given
	 * property element
	 */
	private void setCursorPositionToPropertyValue(XMLMultiPageEditorPart xmlEditor, ElementImpl propertyElement) {
		IEditorPart[] editorParts = xmlEditor.findEditors(xmlEditor.getEditorInput());

		for (IEditorPart currEditorPart : editorParts) {
			if (currEditorPart instanceof StructuredTextEditor) {
				StructuredTextEditor structuredTextEditor = (StructuredTextEditor) currEditorPart;
				int propertyElementStartOffset = propertyElement.getStartOffset();
				String propertyElementTextContent = propertyElement.getStartStructuredDocumentRegion().getFullText();
				String valueAttributeStartString = "value=\"";
				int valueAttributeStartIndex = propertyElementTextContent.indexOf(valueAttributeStartString)
						+ valueAttributeStartString.length();
				if (valueAttributeStartIndex > 0) {
					int documentOffset = propertyElementStartOffset + valueAttributeStartIndex;
					structuredTextEditor.selectAndReveal(documentOffset, 0);
					currEditorPart.setFocus();
				}
			}
		}
	}
}
