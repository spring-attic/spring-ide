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
package org.springframework.ide.eclipse.config.ui.editors.integration.graph.parts;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.requests.CreateConnectionRequest;
import org.springframework.ide.eclipse.config.graph.parts.ActivityDiagramPart;
import org.springframework.ide.eclipse.config.graph.parts.BottomAnchor;
import org.springframework.ide.eclipse.config.graph.parts.RightAnchor;
import org.springframework.ide.eclipse.config.graph.policies.FixedConnectionNodeEditPolicy;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.IntegrationImages;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.RecipientListRouterNodeEditPolicy;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.model.AlternateTransition;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.model.RecipientListRouterModelElement;


/**
 * @author Leo Dos Santos
 */
public class RecipientListRouterGraphicalEditPart extends BorderedIntegrationPart {

	public RecipientListRouterGraphicalEditPart(RecipientListRouterModelElement router) {
		super(router);
	}

	@Override
	protected IFigure createFigure() {
		Label l = (Label) super.createFigure();
		l.setIcon(IntegrationImages.getImageWithBadge(IntegrationImages.RECIPIENT_LIST, IntegrationImages.BADGE_SI));
		return l;
	}

	@Override
	protected FixedConnectionNodeEditPolicy getFixedConnectionNodeEditPolicy() {
		return new RecipientListRouterNodeEditPolicy();
	}

	@Override
	public RecipientListRouterModelElement getModelElement() {
		return (RecipientListRouterModelElement) getModel();
	}

	@Override
	public ConnectionAnchor getSourceConnectionAnchor(ConnectionEditPart connection) {
		if (connection instanceof AlternateTransitionPart) {
			return getRecipientConnectionAnchor();
		}
		return super.getSourceConnectionAnchor(connection);
	}

	@Override
	public ConnectionAnchor getSourceConnectionAnchor(Request request) {
		if (request instanceof CreateConnectionRequest) {
			CreateConnectionRequest create = (CreateConnectionRequest) request;
			if (AlternateTransition.class == create.getNewObjectType()) {
				return getRecipientConnectionAnchor();
			}
		}
		return super.getSourceConnectionAnchor(request);
	}

	private ConnectionAnchor getRecipientConnectionAnchor() {
		EditPart part = getViewer().getContents();
		if (part instanceof ActivityDiagramPart) {
			ActivityDiagramPart diagram = (ActivityDiagramPart) part;
			if (diagram.getDirection() == PositionConstants.EAST) {
				return new BottomAnchor(getFigure(), getAnchorOffset());
			}
		}
		return new RightAnchor(getFigure(), getAnchorOffset());
	}

}
