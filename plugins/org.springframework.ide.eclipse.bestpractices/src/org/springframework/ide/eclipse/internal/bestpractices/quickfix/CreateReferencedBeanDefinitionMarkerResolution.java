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
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.wst.sse.core.internal.format.IStructuredFormatProcessor;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.sse.ui.StructuredTextEditor;
import org.eclipse.wst.xml.core.internal.document.ElementImpl;
import org.eclipse.wst.xml.core.internal.provisional.format.FormatProcessorXML;
import org.springsource.ide.eclipse.commons.core.StatusHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;


/**
 * Resolution for referenced bean not found markers. Creates a corresponding
 * bean definition below the bean with the marker.
 * @author Wesley Coelho
 * @author Leo Dos Santos
 * @author Christian Dupuis
 * @author Terry Denney
 */
public class CreateReferencedBeanDefinitionMarkerResolution implements IMarkerResolution2 {

	private static final String MESSAGE_ATTRIBUTE_KEY = "message";

	private String missingBeanId = "";

	public CreateReferencedBeanDefinitionMarkerResolution(IMarker marker) {
		String markerMessage = marker.getAttribute(MESSAGE_ATTRIBUTE_KEY, "");
		missingBeanId = extractBeanId(markerMessage);
	}

	private String extractBeanId(String message) {
		final String startTag = "bean '";
		int startPos = message.indexOf(startTag) + startTag.length();
		int endPos = message.indexOf("'", startPos);
		return message.substring(startPos, endPos);
	}

	public String getDescription() {
		return "Create bean definition with id " + missingBeanId;
	}

	public Image getImage() {
		return null;
	}

	public String getLabel() {
		return "Create bean definition with id " + missingBeanId;
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
					Element newBeanElement = document.createElement("bean");
					beanElement.getParentNode().insertBefore(newBeanElement, beanElement.getNextSibling());
					newBeanElement.setAttribute("id", missingBeanId);
					newBeanElement.setAttribute("class", "");

					Text spacerNode = document.createTextNode("\n\n");
					beanElement.getParentNode().insertBefore(spacerNode, newBeanElement);

					IStructuredFormatProcessor formatProcessor = new FormatProcessorXML();
					formatProcessor.formatNode(newBeanElement);
					XmlQuickFixUtil.saveMarkedFile(marker);

					IEditorPart editor = XmlQuickFixUtil.getMarkedEditor(marker);
					setCursorPositionToClassAttribute((MultiPageEditorPart) editor, (ElementImpl) newBeanElement);
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
	 * Attempts to set the cursor position to the class attribute value of the
	 * given XML Element that corresponds to a bean definition. Preconditions:
	 */
	private void setCursorPositionToClassAttribute(MultiPageEditorPart xmlEditor, ElementImpl newBeanElement) {
		IEditorPart[] editorParts = xmlEditor.findEditors(xmlEditor.getEditorInput());

		for (IEditorPart currEditorPart : editorParts) {
			if (currEditorPart instanceof StructuredTextEditor) {
				StructuredTextEditor structuredTextEditor = (StructuredTextEditor) currEditorPart;
				int beanElementStartOffset = newBeanElement.getStartOffset();
				String newBeanTextContent = newBeanElement.getFirstStructuredDocumentRegion().getFullText();
				String classAttributeStartString = "class=\"";
				int classAttributeStartIndex = newBeanTextContent.indexOf(classAttributeStartString)
						+ classAttributeStartString.length();
				if (classAttributeStartIndex > 0) {
					int documentOffset = beanElementStartOffset + classAttributeStartIndex;
					structuredTextEditor.selectAndReveal(documentOffset, 0);
					currEditorPart.setFocus();
				}
			}
		}
	}
}
