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
package org.springframework.ide.eclipse.config.ui.editors.webflow.graph.parts;

import org.eclipse.draw2d.ChopboxAnchor;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.Request;
import org.springframework.ide.eclipse.config.graph.parts.SimpleActivityPart;
import org.springframework.ide.eclipse.config.ui.editors.webflow.graph.StateDirectEditPolicy;
import org.springframework.ide.eclipse.config.ui.editors.webflow.graph.StateNodeEditPolicy;
import org.springframework.ide.eclipse.config.ui.editors.webflow.graph.model.AbstractStateModelElement;

/**
 * @author Leo Dos Santos
 */
public abstract class AbstractStateGraphicalEditPart extends SimpleActivityPart {

	public AbstractStateGraphicalEditPart(AbstractStateModelElement state) {
		super(state);
	}

	@Override
	protected IFigure createFigure() {
		Label l = (Label) super.createFigure();
		l.setTextPlacement(PositionConstants.SOUTH);
		return l;
	}

	@Override
	protected void createEditPolicies() {
		super.createEditPolicies();
		installEditPolicy(EditPolicy.DIRECT_EDIT_ROLE, new StateDirectEditPolicy());
		installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE, new StateNodeEditPolicy());
	}

	@Override
	protected Dimension getFigureHint() {
		return getFigure().getPreferredSize(((Label) figure).getIcon().getBounds().width,
				((Label) figure).getIcon().getBounds().height);
	}

	@Override
	public ConnectionAnchor getSourceConnectionAnchor(ConnectionEditPart connection) {
		return new ChopboxAnchor(getFigure());
	}

	@Override
	public ConnectionAnchor getSourceConnectionAnchor(Request request) {
		return new ChopboxAnchor(getFigure());
	}

	@Override
	public ConnectionAnchor getTargetConnectionAnchor(ConnectionEditPart connection) {
		return new ChopboxAnchor(getFigure());
	}

	@Override
	public ConnectionAnchor getTargetConnectionAnchor(Request request) {
		return new ChopboxAnchor(getFigure());
	}

}
