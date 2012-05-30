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
package org.springframework.ide.eclipse.config.ui.editors.webflow.graph;

import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMModel;
import org.springframework.ide.eclipse.config.core.ConfigCoreUtils;
import org.springframework.ide.eclipse.config.core.formatting.ShallowFormatProcessorXML;
import org.springframework.ide.eclipse.config.core.schemas.WebFlowSchemaConstants;
import org.springframework.ide.eclipse.config.graph.model.commands.AbstractConnectionCreateCommand;


/**
 * @author Leo Dos Santos
 */
@SuppressWarnings("restriction")
public class StateConnectionCreateCommand extends AbstractConnectionCreateCommand {

	private final ShallowFormatProcessorXML formatter;

	protected String oldTargetId;

	protected String targetId;

	public StateConnectionCreateCommand(ITextEditor textEditor, int lineStyle) {
		super(textEditor, lineStyle);
		this.formatter = new ShallowFormatProcessorXML();
	}

	@Override
	public boolean canExecute() {
		if (super.canExecute()) {
			oldTargetId = sourceElement.getAttribute(WebFlowSchemaConstants.ATTR_TO);
			targetId = targetElement.getAttribute(WebFlowSchemaConstants.ATTR_ID);
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
			IDOMElement transition = (IDOMElement) document.createElement(WebFlowSchemaConstants.ELEM_TRANSITION);
			transition.setPrefix(ConfigCoreUtils.getPrefixForNamespaceUri(document, WebFlowSchemaConstants.URI));
			sourceElement.appendChild(transition);
			processor.insertDefaultAttributes(transition);
			formatter.formatNode(transition);
			formatter.formatNode(sourceElement);
			transition.setAttribute(WebFlowSchemaConstants.ATTR_TO, targetId);
			model.endRecording(this);
		}
	}

}
