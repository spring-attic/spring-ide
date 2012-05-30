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

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.NonResizableEditPolicy;
import org.eclipse.gef.editpolicies.XYLayoutEditPolicy;
import org.eclipse.gef.requests.CreateRequest;
import org.springframework.ide.eclipse.config.graph.model.Activity;
import org.springframework.ide.eclipse.config.graph.model.StructuredActivity;
import org.springframework.ide.eclipse.config.graph.model.commands.AddCommand;
import org.springframework.ide.eclipse.config.graph.model.commands.CreateCommand;
import org.springframework.ide.eclipse.config.graph.model.commands.MoveCommand;
import org.springframework.ide.eclipse.config.graph.parts.ActivityPart;


/**
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
public class GraphXYLayoutPolicy extends XYLayoutEditPolicy {

	@Override
	protected Command createAddCommand(EditPart child, Object constraint) {
		Activity activity = (Activity) child.getModel();
		EditPartViewer viewer = getHost().getViewer();
		AddCommand add = new AddCommand(activity.getDiagram().getTextEditor(), viewer);
		add.setParent((StructuredActivity) getHost().getModel());
		add.setChild(activity);
		return add;
	}

	@Override
	protected Command createChangeConstraintCommand(EditPart child, Object constraint) {
		if (!(child instanceof ActivityPart)) {
			return null;
		}
		if (!(constraint instanceof Rectangle)) {
			return null;
		}

		ActivityPart part = (ActivityPart) child;
		Activity activity = part.getModelElement();
		IFigure figure = part.getFigure();
		Rectangle oldBounds = figure.getBounds();
		Rectangle newBounds = (Rectangle) constraint;

		if (oldBounds.width != newBounds.width && newBounds.width != -1) {
			return null;
		}
		if (oldBounds.height != newBounds.height && newBounds.height != -1) {
			return null;
		}

		MoveCommand command = new MoveCommand(activity, oldBounds.getCopy(), newBounds.getCopy());
		return command;
	}

	@Override
	protected EditPolicy createChildEditPolicy(EditPart child) {
		return new NonResizableEditPolicy();
	}

	@Override
	protected Command getCreateCommand(CreateRequest request) {
		StructuredActivity parent = (StructuredActivity) getHost().getModel();
		EditPartViewer viewer = getHost().getViewer();
		CreateCommand command = new CreateCommand(parent.getDiagram().getTextEditor(), viewer);
		command.setParent(parent);
		Activity child = (Activity) request.getNewObject();
		Point location = request.getLocation();
		command.setBounds(putActivityAtLocation(child, location));
		command.setChild(child);
		return command;
	}

	@Override
	protected Rectangle getCurrentConstraintFor(GraphicalEditPart child) {
		IFigure figure = child.getFigure();
		Rectangle rectangle = (Rectangle) figure.getParent().getLayoutManager().getConstraint(figure);
		if (rectangle == null) {
			rectangle = figure.getBounds();
		}
		return rectangle;
	}

	private Rectangle putActivityAtLocation(Activity activity, Point location) {
		Rectangle bounds = activity.getBounds();
		if (bounds == null) {
			bounds = new Rectangle();
		}
		bounds.x = location.x;
		bounds.y = location.y;
		return bounds;
	}

}
