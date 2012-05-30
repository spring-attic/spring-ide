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

import org.eclipse.draw2d.Graphics;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMModel;
import org.springframework.ide.eclipse.config.core.ConfigCoreUtils;
import org.springframework.ide.eclipse.config.core.formatting.ShallowFormatProcessorXML;
import org.springframework.ide.eclipse.config.core.schemas.IntegrationSchemaConstants;
import org.springframework.ide.eclipse.config.graph.model.commands.AbstractConnectionCreateCommand;


/**
 * @author Leo Dos Santos
 */
@SuppressWarnings("restriction")
public class RecipientConnectionCreateCommand extends AbstractConnectionCreateCommand {

	private final ShallowFormatProcessorXML formatter;

	protected String oldTargetId;

	protected String targetId;

	public RecipientConnectionCreateCommand(ITextEditor textEditor) {
		super(textEditor, Graphics.LINE_DASH);
		this.formatter = new ShallowFormatProcessorXML();
	}

	@Override
	public boolean canExecute() {
		if (super.canExecute()) {
			oldTargetId = sourceElement.getAttribute(IntegrationSchemaConstants.ATTR_CHANNEL);
			targetId = targetElement.getAttribute(IntegrationSchemaConstants.ATTR_ID);
			if (targetId != null && targetId.trim().length() != 0) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void execute() {
		IDOMDocument document = (IDOMDocument) sourceElement.getOwnerDocument();
		IDOMModel model = document.getModel();
		if (model != null) {
			model.beginRecording(this);
			IDOMElement recipient = (IDOMElement) document.createElement(IntegrationSchemaConstants.ELEM_RECIPIENT);
			recipient.setPrefix(ConfigCoreUtils.getPrefixForNamespaceUri(document, IntegrationSchemaConstants.URI));
			sourceElement.appendChild(recipient);
			processor.insertDefaultAttributes(recipient);
			formatter.formatNode(recipient);
			formatter.formatNode(sourceElement);
			recipient.setAttribute(IntegrationSchemaConstants.ATTR_CHANNEL, targetId);
			model.endRecording(this);
		}
	}

}
