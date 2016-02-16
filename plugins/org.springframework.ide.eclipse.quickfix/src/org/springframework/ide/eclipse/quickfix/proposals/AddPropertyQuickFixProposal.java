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
package org.springframework.ide.eclipse.quickfix.proposals;

import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.link.LinkedModeModel;
import org.eclipse.jface.text.link.LinkedModeUI;
import org.eclipse.jface.text.link.LinkedPosition;
import org.eclipse.jface.text.link.LinkedPositionGroup;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocument;
import org.eclipse.wst.xml.core.internal.document.ElementImpl;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.eclipse.wst.xml.core.internal.provisional.format.FormatProcessorXML;
import org.springframework.ide.eclipse.config.core.IConfigEditor;
import org.springframework.ide.eclipse.config.core.schemas.BeansSchemaConstants;
import org.springframework.ide.eclipse.quickfix.Activator;
import org.springsource.ide.eclipse.commons.core.StatusHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * @author Terry Denney
 */
public class AddPropertyQuickFixProposal extends BeanAttributeQuickFixProposal implements ICompletionProposal {

	private final List<String> missingProperties;

	private final IDOMNode beanNode;

	private final String label;

	public AddPropertyQuickFixProposal(int offset, int length, boolean missingEndQuote, List<String> missingProperties,
			IDOMNode beanNode, String label) {
		super(offset, length, missingEndQuote);
		this.missingProperties = missingProperties;
		this.beanNode = beanNode;
		this.label = label;
	}

	@Override
	public void applyQuickFix(IDocument document) {
		IStructuredModel model = null;
		try {
			if (document instanceof IStructuredDocument) {
				model = StructuredModelManager.getModelManager().getModelForEdit((IStructuredDocument) document);
				model.beginRecording(this);
			}

			Document ownerDocument = beanNode.getOwnerDocument();

			NodeList childNodes = beanNode.getChildNodes();
			Node lastProperty = null;
			for (int i = 0; i < childNodes.getLength(); i++) {
				Node child = childNodes.item(i);
				String nodeName = child.getNodeName();
				if (nodeName != null && nodeName.equals(BeansSchemaConstants.ELEM_PROPERTY)) {
					lastProperty = child;
				}
			}

			Node nextSibling = null;
			if (lastProperty != null) {
				nextSibling = lastProperty.getNextSibling();
			}

			FormatProcessorXML formatter = new FormatProcessorXML();
			Element[] properties = new Element[missingProperties.size()];
			for (int i = 0; i < missingProperties.size(); i++) {
				properties[i] = ownerDocument.createElement(BeansSchemaConstants.ELEM_PROPERTY);
				properties[i].setAttribute(BeansSchemaConstants.ATTR_NAME, missingProperties.get(i));
				if (nextSibling != null) {
					beanNode.insertBefore(properties[i], nextSibling);
				}
				else {
					beanNode.appendChild(properties[i]);
				}
			}

			formatter.formatNode(beanNode);

			if (model != null) {
				model.endRecording(this);
			}

			LinkedModeModel linkModel = new LinkedModeModel();
			boolean hasPositions = false;
			for (Element element : properties) {
				formatter.formatNode(element);
				if (element instanceof ElementImpl) {
					ElementImpl elementImpl = (ElementImpl) element;
					int nodeOffset = elementImpl.getStartEndOffset() - 1;

					LinkedPositionGroup group = new LinkedPositionGroup();

					try {
						group.addPosition(new LinkedPosition(document, nodeOffset, 0));
						linkModel.addGroup(group);
						hasPositions = true;
					}
					catch (BadLocationException e) {
						StatusHandler.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
								"Unable to create linked model for property quick fix"));
					}
				}
			}

			ITextViewer viewer = null;
			IEditorPart editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
			if (editor != null && editor instanceof IConfigEditor) {
				viewer = ((IConfigEditor) editor).getTextViewer();
			}
			if (hasPositions && viewer != null) {
				try {
					linkModel.forceInstall();
					LinkedModeUI ui = new LinkedModeUI(linkModel, viewer);
					ui.enter();
				}
				catch (BadLocationException e) {
					StatusHandler.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
							"Unable to create linked model for property quick fix"));
				}

			}
		}
		finally {
			if (model != null) {
				model.releaseFromEdit();
			}
		}
	}

	public String getDisplayString() {
		return label;
	}

	public Image getImage() {
		return JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_ADD);
	}

}
