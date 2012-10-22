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
package org.springframework.ide.eclipse.config.ui.editors.integration.jpa.graph.parts;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;
import org.springframework.ide.eclipse.config.ui.editors.integration.jpa.graph.model.InboundChannelAdapterModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.jpa.graph.model.OutboundChannelAdapterModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.jpa.graph.model.RetrievingOutboundGatewayModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.jpa.graph.model.UpdatingOutboundGatewayModelElement;

/**
 * @author Leo Dos Santos
 */
public class IntJpaEditPartFactory implements EditPartFactory {

	public EditPart createEditPart(EditPart context, Object model) {
		EditPart part = null;
		if (model instanceof InboundChannelAdapterModelElement) {
			part = new InboundChannelAdapterGraphicalEditPart((InboundChannelAdapterModelElement) model);
		}
		else if (model instanceof OutboundChannelAdapterModelElement) {
			part = new OutboundChannelAdapterGraphicalEditPart((OutboundChannelAdapterModelElement) model);
		}
		else if (model instanceof RetrievingOutboundGatewayModelElement) {
			part = new RetrievingOutboundGatewayGraphicalEditPart((RetrievingOutboundGatewayModelElement) model);
		}
		else if (model instanceof UpdatingOutboundGatewayModelElement) {
			part = new UpdatingOutboundGatewayGraphicalEditPart((UpdatingOutboundGatewayModelElement) model);
		}
		return part;
	}

}
