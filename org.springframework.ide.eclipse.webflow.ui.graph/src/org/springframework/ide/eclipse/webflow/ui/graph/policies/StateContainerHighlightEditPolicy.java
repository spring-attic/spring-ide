/*******************************************************************************
 * Copyright (c) 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.webflow.ui.graph.policies;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.editpolicies.GraphicalEditPolicy;
import org.eclipse.swt.graphics.Color;
import org.springframework.ide.eclipse.webflow.ui.graph.figures.CompoundStateFigure;

/**
 * @author Christian Dupuis
 */
public class StateContainerHighlightEditPolicy extends GraphicalEditPolicy {

	/**
	 * 
	 */
	private static Color highLightColor = new Color(null, 200, 200, 240);

	/**
	 * 
	 */
	private Color revertColor;

	/* (non-Javadoc)
	 * @see org.eclipse.gef.editpolicies.AbstractEditPolicy#eraseTargetFeedback(org.eclipse.gef.Request)
	 */
	public void eraseTargetFeedback(Request request) {
		if (revertColor != null) {
			setContainerBackground(revertColor);
			revertColor = null;
		}
		if (request.getType().equals(RequestConstants.REQ_CREATE)
				|| request.getType().equals(RequestConstants.REQ_ADD)
				|| request.getType().equals(RequestConstants.REQ_CLONE)
				|| request.getType().equals(RequestConstants.REQ_MOVE)
				|| request.getType().equals(
						RequestConstants.REQ_CONNECTION_START)
				|| request.getType()
						.equals(RequestConstants.REQ_CONNECTION_END))
			if (getContainerFigure() instanceof CompoundStateFigure) {
				((CompoundStateFigure) getContainerFigure()).setSelected(false);
			}
	}

	/**
	 * 
	 * 
	 * @return 
	 */
	private Color getContainerBackground() {
		return getContainerFigure().getBackgroundColor();
	}

	/**
	 * 
	 * 
	 * @return 
	 */
	private IFigure getContainerFigure() {
		return ((GraphicalEditPart) getHost()).getFigure();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gef.editpolicies.AbstractEditPolicy#getTargetEditPart(org.eclipse.gef.Request)
	 */
	public EditPart getTargetEditPart(Request request) {
		return request.getType().equals(RequestConstants.REQ_SELECTION_HOVER) ? getHost()
				: null;
	}

	/**
	 * 
	 * 
	 * @param c 
	 */
	private void setContainerBackground(Color c) {
		getContainerFigure().setBackgroundColor(c);
	}

	/**
	 * 
	 */
	protected void showHighlight() {
		if (revertColor == null) {
			revertColor = getContainerBackground();
			setContainerBackground(highLightColor);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gef.editpolicies.AbstractEditPolicy#showTargetFeedback(org.eclipse.gef.Request)
	 */
	public void showTargetFeedback(Request request) {
		if (request.getType().equals(RequestConstants.REQ_CREATE)
				|| request.getType().equals(RequestConstants.REQ_ADD)
				|| request.getType().equals(RequestConstants.REQ_CLONE)
				|| request.getType().equals(RequestConstants.REQ_MOVE))
			if (getContainerFigure() instanceof CompoundStateFigure) {
				((CompoundStateFigure) getContainerFigure()).setSelected(true);
			}
			else {
				showHighlight();
			}
		else if (request.getType()
				.equals(RequestConstants.REQ_CONNECTION_START)
				|| request.getType()
						.equals(RequestConstants.REQ_CONNECTION_END)) {
			if (getContainerFigure() instanceof CompoundStateFigure) {
				((CompoundStateFigure) getContainerFigure()).setSelected(true);
			}
		}
	}
}
