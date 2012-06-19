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
package org.springframework.ide.eclipse.config.ui.editors.integration.graph;

import java.util.List;

import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMModel;
import org.springframework.ide.eclipse.config.core.ConfigCoreUtils;
import org.springframework.ide.eclipse.config.core.formatting.ShallowFormatProcessorXML;
import org.springframework.ide.eclipse.config.core.schemas.IntegrationSchemaConstants;
import org.springframework.ide.eclipse.config.graph.model.commands.FixedConnectionCreateCommand;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.model.IntegrationDiagram;
import org.w3c.dom.Node;


/**
 * @author Leo Dos Santos
 */
@SuppressWarnings("restriction")
public class FixedConnectionChannelCreateCommand extends FixedConnectionCreateCommand {

	private final ShallowFormatProcessorXML formatter;

	private IDOMElement parentElement;

	private String inputName;

	public FixedConnectionChannelCreateCommand(ITextEditor textEditor, int lineStyle) {
		super(textEditor, lineStyle);
		this.formatter = new ShallowFormatProcessorXML();
	}

	@Override
	protected void createNewElement() {
		if (parentElement != null) {
			IDOMDocument document = (IDOMDocument) parentElement.getOwnerDocument();
			IDOMElement childElement = (IDOMElement) document.createElement(inputName);
			IDOMModel model = document.getModel();
			if (model != null) {
				model.beginRecording(this);
				parentElement.appendChild(childElement);
				processor.insertDefaultAttributes(childElement);
				id = IntegrationSchemaConstants.ELEM_CHANNEL
						+ ((IntegrationDiagram) source.getDiagram()).getNewChannelId();
				childElement.setAttribute(IntegrationSchemaConstants.ATTR_ID, id);
				formatter.formatNode(childElement);
				formatter.formatNode(childElement.getParentNode());
				sourceElement.setAttribute(sourceAnchor.getConnectionLabel(), id);
				targetElement.setAttribute(targetAnchor.getConnectionLabel(), id);
				model.endRecording(this);
			}
		}
	}

	@Override
	protected boolean doesCreateNewElement() {
		Node parent = sourceElement.getParentNode();
		if (parent instanceof IDOMElement) {
			parentElement = (IDOMElement) parent;
			inputName = IntegrationSchemaConstants.ELEM_CHANNEL;
			String uri = source.getDiagram().getNamespaceUri();
			String prefix = ConfigCoreUtils.getPrefixForNamespaceUri((IDOMDocument) sourceElement.getOwnerDocument(),
					uri);
			if (prefix != null && prefix.length() > 0) {
				inputName = prefix + ":" + inputName; //$NON-NLS-1$
			}
			List<String> children = processor.getChildNames(parentElement);
			if (children.contains(inputName)) {
				return true;
			}
		}
		return false;
	}

}
