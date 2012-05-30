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

import org.eclipse.ui.texteditor.ITextEditor;
import org.springframework.ide.eclipse.config.core.schemas.IntegrationSchemaConstants;
import org.springframework.ide.eclipse.config.graph.model.ModelElementCreationFactory;
import org.springframework.ide.eclipse.config.graph.model.StructuredActivity;
import org.springframework.ide.eclipse.config.graph.model.commands.AbstractTextCommand;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.model.ChannelModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.model.ImplicitChannelModelElement;


/**
 * @author Leo Dos Santos
 */
public class CreateExplicitChannelCommand extends AbstractTextCommand {

	private StructuredActivity parent;

	private ImplicitChannelModelElement channel;

	public CreateExplicitChannelCommand(ITextEditor textEditor) {
		super(textEditor);
	}

	@Override
	public void execute() {
		ModelElementCreationFactory factory = new ModelElementCreationFactory(ChannelModelElement.class, parent
				.getDiagram());
		ChannelModelElement child = (ChannelModelElement) factory.getNewObject();
		child.getInput().setAttribute(IntegrationSchemaConstants.ATTR_ID, channel.getName());
		parent.addChild(child);
	}

	public void setChannel(ImplicitChannelModelElement channel) {
		this.channel = channel;
	}

	public void setParent(StructuredActivity parent) {
		this.parent = parent;
	}

}
