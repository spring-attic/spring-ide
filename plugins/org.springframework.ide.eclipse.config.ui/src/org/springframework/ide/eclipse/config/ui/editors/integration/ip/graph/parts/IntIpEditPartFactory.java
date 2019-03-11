/*******************************************************************************
 *  Copyright (c) 2012, 2014 Pivotal Software Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      Pivotal Software Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.config.ui.editors.integration.ip.graph.parts;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.IntegrationImages;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.parts.BadgedIntegrationPart;
import org.springframework.ide.eclipse.config.ui.editors.integration.ip.graph.model.TcpConnectionEventInboundChannelAdapterModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.ip.graph.model.TcpInboundChannelAdapterModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.ip.graph.model.TcpInboundGatewayModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.ip.graph.model.TcpOutboundChannelAdapterModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.ip.graph.model.TcpOutboundGatewayModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.ip.graph.model.UdpInboundChannelAdapterModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.ip.graph.model.UdpOutboundChannelAdapterModelElement;

/**
 * @author Leo Dos Santos
 */
public class IntIpEditPartFactory implements EditPartFactory {

	public EditPart createEditPart(EditPart context, Object model) {
		EditPart part = null;
		if (model instanceof UdpInboundChannelAdapterModelElement) {
			part = new BadgedIntegrationPart((UdpInboundChannelAdapterModelElement) model,
					IntegrationImages.INBOUND_ADAPTER, IntegrationImages.BADGE_SI_IP);
		}
		else if (model instanceof TcpInboundGatewayModelElement) {
			part = new BadgedIntegrationPart((TcpInboundGatewayModelElement) model, IntegrationImages.INBOUND_GATEWAY,
					IntegrationImages.BADGE_SI_IP);
		}
		else if (model instanceof UdpOutboundChannelAdapterModelElement) {
			part = new BadgedIntegrationPart((UdpOutboundChannelAdapterModelElement) model,
					IntegrationImages.OUTBOUND_ADAPTER, IntegrationImages.BADGE_SI_IP);
		}
		else if (model instanceof TcpOutboundGatewayModelElement) {
			part = new BadgedIntegrationPart((TcpOutboundGatewayModelElement) model,
					IntegrationImages.OUTBOUND_GATEWAY, IntegrationImages.BADGE_SI_IP);
		}
		else if (model instanceof TcpInboundChannelAdapterModelElement) {
			part = new BadgedIntegrationPart((TcpInboundChannelAdapterModelElement) model,
					IntegrationImages.INBOUND_ADAPTER, IntegrationImages.BADGE_SI_IP);
		}
		else if (model instanceof TcpOutboundChannelAdapterModelElement) {
			part = new BadgedIntegrationPart((TcpOutboundChannelAdapterModelElement) model,
					IntegrationImages.OUTBOUND_ADAPTER, IntegrationImages.BADGE_SI_IP);
		}
		else if (model instanceof TcpConnectionEventInboundChannelAdapterModelElement) {
			part = new BadgedIntegrationPart((TcpConnectionEventInboundChannelAdapterModelElement) model,
					IntegrationImages.INBOUND_ADAPTER, IntegrationImages.BADGE_SI_IP);
		}
		return part;
	}

}
