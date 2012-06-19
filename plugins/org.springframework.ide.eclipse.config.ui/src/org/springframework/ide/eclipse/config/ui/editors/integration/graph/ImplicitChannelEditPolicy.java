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

import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.ComponentEditPolicy;
import org.springframework.ide.eclipse.config.graph.model.StructuredActivity;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.model.ImplicitChannelModelElement;


/**
 * @author Leo Dos Santos
 */
public class ImplicitChannelEditPolicy extends ComponentEditPolicy {

	@Override
	public Command getCommand(Request request) {
		if (CreateExplicitChannelAction.EXPLICIT_CHANNEL_REQ.equals(request.getType())) {
			return getExplicitChannelCommand();
		}
		return super.getCommand(request);
	}

	protected Command getExplicitChannelCommand() {
		StructuredActivity parent = (StructuredActivity) getHost().getParent().getModel();
		ImplicitChannelModelElement channel = (ImplicitChannelModelElement) getHost().getModel();
		CreateExplicitChannelCommand command = new CreateExplicitChannelCommand(parent.getDiagram().getTextEditor());
		command.setParent(parent);
		command.setChannel(channel);
		return command;
	}

}
