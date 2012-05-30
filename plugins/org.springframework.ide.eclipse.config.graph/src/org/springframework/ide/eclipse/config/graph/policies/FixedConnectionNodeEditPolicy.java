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
package org.springframework.ide.eclipse.config.graph.policies;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.requests.CreateConnectionRequest;
import org.eclipse.gef.requests.DropRequest;
import org.eclipse.gef.requests.ReconnectRequest;
import org.eclipse.gef.requests.SelectionRequest;
import org.eclipse.gef.requests.TargetRequest;
import org.eclipse.ui.texteditor.ITextEditor;
import org.springframework.ide.eclipse.config.graph.model.Activity;
import org.springframework.ide.eclipse.config.graph.model.Transition;
import org.springframework.ide.eclipse.config.graph.model.commands.FixedConnectionCreateCommand;
import org.springframework.ide.eclipse.config.graph.model.commands.ReconnectFixedSourceCommand;
import org.springframework.ide.eclipse.config.graph.model.commands.ReconnectFixedTargetCommand;
import org.springframework.ide.eclipse.config.graph.parts.BorderedActivityPart;
import org.springframework.ide.eclipse.config.graph.parts.FixedConnectionAnchor;


/**
 * @author Leo Dos Santos
 */
public abstract class FixedConnectionNodeEditPolicy extends ActivityNodeEditPolicy {

	private void eraseConnectionLabel() {
		if (getActivityPart() instanceof BorderedActivityPart) {
			BorderedActivityPart part = (BorderedActivityPart) getActivityPart();
			part.setBorderLabel(""); //$NON-NLS-1$
			part.getFigure().repaint();
		}
	}

	@Override
	protected void eraseTargetConnectionFeedback(DropRequest request) {
		eraseConnectionLabel();
	}

	@Override
	public void eraseTargetFeedback(Request request) {
		eraseConnectionLabel();
		super.eraseTargetFeedback(request);
	}

	@Override
	protected Command getConnectionCompleteCommand(CreateConnectionRequest request) {
		if (request.getStartCommand() instanceof FixedConnectionCreateCommand) {
			FixedConnectionCreateCommand cmd = (FixedConnectionCreateCommand) request.getStartCommand();
			ConnectionAnchor anchor = getActivityPart().getTargetConnectionAnchor(request);
			if (anchor instanceof FixedConnectionAnchor) {
				FixedConnectionAnchor fixedAnchor = (FixedConnectionAnchor) anchor;
				cmd.setTargetAnchor(fixedAnchor);
			}
			else {
				cmd.setTargetAnchor(null);
			}
			cmd.setTarget(getActivity());
			return cmd;
		}
		return super.getConnectionCompleteCommand(request);
	}

	@Override
	protected Command getConnectionCreateCommand(CreateConnectionRequest request) {
		int style = ((Integer) request.getNewObjectType()).intValue();
		Activity source = getActivity();
		FixedConnectionCreateCommand cmd = getConnectionCreateCommand(source.getDiagram().getTextEditor(), style);
		if (cmd != null) {
			ConnectionAnchor anchor = getActivityPart().getSourceConnectionAnchor(request);
			if (anchor instanceof FixedConnectionAnchor) {
				FixedConnectionAnchor fixedAnchor = (FixedConnectionAnchor) anchor;
				cmd.setSourceAnchor(fixedAnchor);
			}
			cmd.setSource(source);
			request.setStartCommand(cmd);
		}
		return cmd;
	}

	protected abstract FixedConnectionCreateCommand getConnectionCreateCommand(ITextEditor textEditor, int style);

	@Override
	protected Command getReconnectSourceCommand(ReconnectRequest request) {
		Activity source = getActivity();
		ReconnectFixedSourceCommand cmd = new ReconnectFixedSourceCommand(source.getDiagram().getTextEditor());
		ConnectionAnchor anchor = getActivityPart().getSourceConnectionAnchor(request);
		if (anchor instanceof FixedConnectionAnchor) {
			FixedConnectionAnchor fixedAnchor = (FixedConnectionAnchor) anchor;
			cmd.setSourceAnchor(fixedAnchor);
		}
		cmd.setTransition((Transition) request.getConnectionEditPart().getModel());
		cmd.setSource(source);
		return cmd;
	}

	@Override
	protected Command getReconnectTargetCommand(ReconnectRequest request) {
		Activity target = getActivity();
		ReconnectFixedTargetCommand cmd = new ReconnectFixedTargetCommand(target.getDiagram().getTextEditor());
		ConnectionAnchor anchor = getActivityPart().getTargetConnectionAnchor(request);
		if (anchor instanceof FixedConnectionAnchor) {
			FixedConnectionAnchor fixedAnchor = (FixedConnectionAnchor) anchor;
			cmd.setTargetAnchor(fixedAnchor);
		}
		cmd.setTransition((Transition) request.getConnectionEditPart().getModel());
		cmd.setTarget(target);
		return cmd;
	}

	private void showConnectionLabel(BorderedActivityPart part, Point location) {
		ConnectionAnchor anchor = part.getConnectionAnchorAt(location);
		if (anchor instanceof FixedConnectionAnchor) {
			part.setBorderLabel(((FixedConnectionAnchor) anchor).getConnectionLabel());
		}
		else {
			part.setBorderLabel(""); //$NON-NLS-1$
		}
		part.getFigure().repaint();
	}

	@Override
	protected void showTargetConnectionFeedback(DropRequest request) {
		if (getActivityPart() instanceof BorderedActivityPart) {
			BorderedActivityPart part = (BorderedActivityPart) getActivityPart();
			if (request instanceof TargetRequest) {
				Point location = request.getLocation();
				showConnectionLabel(part, location);
			}
		}
	}

	@Override
	public void showTargetFeedback(Request request) {
		if (getActivityPart() instanceof BorderedActivityPart) {
			BorderedActivityPart part = (BorderedActivityPart) getActivityPart();
			if (request instanceof SelectionRequest) {
				SelectionRequest selection = (SelectionRequest) request;
				Point location = selection.getLocation();
				showConnectionLabel(part, location);
			}
		}
		super.showTargetFeedback(request);
	}

}
