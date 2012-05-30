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
package org.springframework.ide.eclipse.config.graph.parts;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.Request;
import org.eclipse.gef.requests.DropRequest;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.springframework.ide.eclipse.config.graph.figures.BorderedActivityLabel;
import org.springframework.ide.eclipse.config.graph.model.Activity;
import org.springframework.ide.eclipse.config.graph.model.Transition;
import org.springframework.ide.eclipse.config.graph.policies.ActivityNodeEditPolicy;
import org.springframework.ide.eclipse.config.graph.policies.FixedConnectionNodeEditPolicy;


/**
 * @author Leo Dos Santos
 */
@SuppressWarnings("restriction")
public abstract class BorderedActivityPart extends SimpleActivityPart {

	protected BorderedActivityLabel figure;

	private boolean hasAnchors = true;

	public BorderedActivityPart(Activity activity) {
		super(activity);
	}

	protected BorderedActivityLabel createBorderedLabel(int direction, List<String> incomings, List<String> outgoings) {
		return new BorderedActivityLabel(direction, incomings, outgoings);
	}

	@Override
	protected void createEditPolicies() {
		super.createEditPolicies();
		if (hasAnchors) {
			installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE, getFixedConnectionNodeEditPolicy());
		}
		else {
			installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE, getStandardConnectionNodeEditPolicy());
		}
	}

	@Override
	protected IFigure createFigure() {
		Activity activity = getModelElement();
		List<String> incomings = new ArrayList<String>();
		List<String> outgoings = new ArrayList<String>();
		if (hasAnchors) {
			incomings.addAll(activity.getPrimaryIncomingAttributes());
			incomings.addAll(activity.getSecondaryIncomingAttributes());
			outgoings.addAll(activity.getPrimaryOutgoingAttributes());
			outgoings.addAll(activity.getSecondaryOutgoingAttributes());
		}

		int direction = PositionConstants.SOUTH;
		EditPart part = getViewer().getContents();
		if (part instanceof ActivityDiagramPart) {
			ActivityDiagramPart diagramPart = (ActivityDiagramPart) part;
			direction = diagramPart.getDirection();
		}
		figure = createBorderedLabel(direction, incomings, outgoings);
		return figure;
	}

	@Override
	protected int getAnchorOffset() {
		return -1;
	}

	public ConnectionAnchor getConnectionAnchorAt(Point p) {
		return ((BorderedActivityLabel) getFigure()).getConnectionAnchorAt(p);
	}

	protected abstract FixedConnectionNodeEditPolicy getFixedConnectionNodeEditPolicy();

	protected abstract ActivityNodeEditPolicy getStandardConnectionNodeEditPolicy();

	@Override
	public ConnectionAnchor getSourceConnectionAnchor(ConnectionEditPart connection) {
		if (connection instanceof TransitionPart) {
			TransitionPart part = (TransitionPart) connection;
			Transition trans = (Transition) part.getModel();
			IDOMNode input = trans.getInput();
			if (input != null) {
				ConnectionAnchor anchor = ((BorderedActivityLabel) getFigure()).getConnectionAnchor(trans.getInput()
						.getLocalName());
				if (anchor != null) {
					return anchor;
				}
			}
		}
		return super.getSourceConnectionAnchor(connection);
	}

	@Override
	public ConnectionAnchor getSourceConnectionAnchor(Request request) {
		Point pt = new Point(((DropRequest) request).getLocation());
		ConnectionAnchor anchor = ((BorderedActivityLabel) getFigure()).getSourceConnectionAnchorAt(pt);
		if (anchor != null) {
			return anchor;
		}
		return super.getSourceConnectionAnchor(request);
	}

	@Override
	public ConnectionAnchor getTargetConnectionAnchor(ConnectionEditPart connection) {
		if (connection instanceof TransitionPart) {
			TransitionPart part = (TransitionPart) connection;
			Transition trans = (Transition) part.getModel();
			IDOMNode input = trans.getInput();
			if (input != null) {
				ConnectionAnchor anchor = ((BorderedActivityLabel) getFigure()).getConnectionAnchor(trans.getInput()
						.getLocalName());
				if (anchor != null) {
					return anchor;
				}
			}
		}
		return super.getTargetConnectionAnchor(connection);
	}

	@Override
	public ConnectionAnchor getTargetConnectionAnchor(Request request) {
		Point pt = new Point(((DropRequest) request).getLocation());
		ConnectionAnchor anchor = ((BorderedActivityLabel) getFigure()).getTargetConnectionAnchorAt(pt);
		if (anchor != null) {
			return anchor;
		}
		return super.getTargetConnectionAnchor(request);
	}

	public void setBorderLabel(String label) {
		if (figure != null) {
			figure.setBorderLabel(label);
		}
	}

	public void setHasAnchors(boolean hasAnchors) {
		this.hasAnchors = hasAnchors;
	}

}
