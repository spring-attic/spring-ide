/*******************************************************************************
 * Copyright (c) 2014 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.config.ui.editors.integration.graph.parts;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.requests.CreateConnectionRequest;
import org.eclipse.jface.resource.ImageDescriptor;
import org.springframework.ide.eclipse.config.graph.parts.ActivityDiagramPart;
import org.springframework.ide.eclipse.config.graph.parts.BottomAnchor;
import org.springframework.ide.eclipse.config.graph.parts.RightAnchor;
import org.springframework.ide.eclipse.config.graph.policies.FixedConnectionNodeEditPolicy;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.RouterNodeEditPolicy;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.model.AbstractRouterModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.model.AlternateTransition;

/**
 * Router edit part that has an icon image combined from main image and a little
 * aux decoration icon
 *
 * @author Alex Boyko
 *
 */
public class RouterGraphicalPart extends BadgedIntegrationPart {

	public RouterGraphicalPart(AbstractRouterModelElement activity, ImageDescriptor mainImageDescriptor,
			ImageDescriptor auxImageDescriptor) {
		super(activity, mainImageDescriptor, auxImageDescriptor);
	}

	@Override
	protected FixedConnectionNodeEditPolicy getFixedConnectionNodeEditPolicy() {
		return new RouterNodeEditPolicy();
	}

	@Override
	public ConnectionAnchor getSourceConnectionAnchor(ConnectionEditPart connection) {
		if (connection instanceof AlternateTransitionPart) {
			return getMappingConnectionAnchor();
		}
		return super.getSourceConnectionAnchor(connection);
	}

	@Override
	public ConnectionAnchor getSourceConnectionAnchor(Request request) {
		if (request instanceof CreateConnectionRequest) {
			CreateConnectionRequest create = (CreateConnectionRequest) request;
			if (AlternateTransition.class == create.getNewObjectType()) {
				return getMappingConnectionAnchor();
			}
		}
		return super.getSourceConnectionAnchor(request);
	}

	private ConnectionAnchor getMappingConnectionAnchor() {
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
