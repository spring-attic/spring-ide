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

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.requests.CreateConnectionRequest;
import org.eclipse.gef.requests.ReconnectRequest;
import org.eclipse.ui.texteditor.ITextEditor;
import org.springframework.ide.eclipse.config.graph.model.Activity;
import org.springframework.ide.eclipse.config.graph.model.Transition;
import org.springframework.ide.eclipse.config.graph.model.commands.FixedConnectionCreateCommand;
import org.springframework.ide.eclipse.config.graph.policies.FixedConnectionNodeEditPolicy;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.model.AlternateTransition;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.parts.AlternateTransitionPart;


/**
 * @author Leo Dos Santos
 */
public class FixedConnectionChannelCreatePolicy extends FixedConnectionNodeEditPolicy {

	@Override
	protected Command getConnectionCreateCommand(CreateConnectionRequest request) {
		if (AlternateTransition.class == request.getNewObjectType()) {
			return null;
		}
		return super.getConnectionCreateCommand(request);
	}

	@Override
	protected FixedConnectionCreateCommand getConnectionCreateCommand(ITextEditor textEditor, int style) {
		return new FixedConnectionChannelCreateCommand(textEditor, style);
	}

	@Override
	protected Command getReconnectTargetCommand(ReconnectRequest request) {
		if (request.getConnectionEditPart() instanceof AlternateTransitionPart) {
			Activity target = getActivity();
			ReconnectTargetChannelCommand cmd = new ReconnectTargetChannelCommand(target.getDiagram().getTextEditor());
			cmd.setTransition((Transition) request.getConnectionEditPart().getModel());
			cmd.setTarget(target);
			return cmd;
		}
		return super.getReconnectTargetCommand(request);
	}

}
