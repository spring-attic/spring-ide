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
package org.springframework.ide.eclipse.config.ui.editors.integration.smpp.graph.parts;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.IntegrationImages;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.parts.BadgedIntegrationPart;
import org.springframework.ide.eclipse.config.ui.editors.integration.smpp.graph.model.InboundChannelAdapterModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.smpp.graph.model.InboundGatewayModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.smpp.graph.model.OutboundChannelAdapterModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.smpp.graph.model.OutboundGatewayModelElement;

public class IntSmppEditPartFactory implements EditPartFactory {

	public EditPart createEditPart(EditPart context, Object model) {
		EditPart part = null;
		if (model instanceof InboundChannelAdapterModelElement) {
			part = new BadgedIntegrationPart((InboundChannelAdapterModelElement) model,
					IntegrationImages.INBOUND_ADAPTER, IntegrationImages.BADGE_SI_SMPP);
		}
		else if (model instanceof OutboundChannelAdapterModelElement) {
			part = new BadgedIntegrationPart((OutboundChannelAdapterModelElement) model,
					IntegrationImages.OUTBOUND_ADAPTER, IntegrationImages.BADGE_SI_SMPP);
		}
		else if (model instanceof InboundGatewayModelElement) {
			part = new BadgedIntegrationPart((InboundGatewayModelElement) model, IntegrationImages.INBOUND_GATEWAY,
					IntegrationImages.BADGE_SI_SMPP);
		}
		else if (model instanceof OutboundGatewayModelElement) {
			part = new BadgedIntegrationPart((OutboundGatewayModelElement) model, IntegrationImages.OUTBOUND_GATEWAY,
					IntegrationImages.BADGE_SI_SMPP);
		}
		return part;
	}
}
