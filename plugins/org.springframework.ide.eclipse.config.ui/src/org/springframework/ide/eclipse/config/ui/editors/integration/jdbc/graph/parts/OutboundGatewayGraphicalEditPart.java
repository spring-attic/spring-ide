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
package org.springframework.ide.eclipse.config.ui.editors.integration.jdbc.graph.parts;

import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.springframework.ide.eclipse.config.graph.figures.BidirectionalBorderedActivityLabel;
import org.springframework.ide.eclipse.config.graph.figures.BorderedActivityLabel;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.IntegrationImages;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.parts.BorderedIntegrationPart;
import org.springframework.ide.eclipse.config.ui.editors.integration.jdbc.graph.model.OutboundGatewayModelElement;


/**
 * @author Leo Dos Santos
 */
public class OutboundGatewayGraphicalEditPart extends BorderedIntegrationPart {

	public OutboundGatewayGraphicalEditPart(OutboundGatewayModelElement gateway) {
		super(gateway);
	}

	@Override
	protected BorderedActivityLabel createBorderedLabel(int direction, List<String> incomings, List<String> outgoings) {
		return new BidirectionalBorderedActivityLabel(direction, incomings, outgoings, true);
	}

	@Override
	protected IFigure createFigure() {
		Label l = (Label) super.createFigure();
		l.setIcon(IntegrationImages.getImageWithBadge(IntegrationImages.OUTBOUND_GATEWAY,
				IntegrationImages.BADGE_SI_JDBC));
		return l;
	}

	@Override
	public OutboundGatewayModelElement getModelElement() {
		return (OutboundGatewayModelElement) getModel();
	}

}
