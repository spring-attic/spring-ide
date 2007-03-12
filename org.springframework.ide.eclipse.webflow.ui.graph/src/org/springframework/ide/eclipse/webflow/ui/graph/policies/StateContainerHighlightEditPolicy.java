/*
 * Copyright 2002-2007 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
 * 
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