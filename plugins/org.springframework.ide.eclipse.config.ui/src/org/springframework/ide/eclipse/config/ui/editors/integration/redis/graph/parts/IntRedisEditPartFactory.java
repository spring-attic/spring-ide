/*******************************************************************************
 *  Copyright (c) 2012, 2014 Pivotal Software Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      Pivotal Software Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.config.ui.editors.integration.redis.graph.parts;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.IntegrationImages;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.parts.BadgedIntegrationPart;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.parts.ChannelGraphicalEditPart;
import org.springframework.ide.eclipse.config.ui.editors.integration.redis.graph.model.InboundChannelAdapterModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.redis.graph.model.OutboundChannelAdapterModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.redis.graph.model.OutboundGatewayModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.redis.graph.model.PublishSubscribeChannelModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.redis.graph.model.QueueInboundChannelAdapterModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.redis.graph.model.QueueInboundGatewayModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.redis.graph.model.QueueOutboundChannelAdapterModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.redis.graph.model.QueueOutboundGatewayModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.redis.graph.model.StoreInboundChannelAdapterModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.redis.graph.model.StoreOutboundChannelAdapterModelElement;

/**
 * @author Leo Dos Santos
 */
public class IntRedisEditPartFactory implements EditPartFactory {

	public EditPart createEditPart(EditPart context, Object model) {
		EditPart part = null;
		if (model instanceof InboundChannelAdapterModelElement) {
			part = new BadgedIntegrationPart((InboundChannelAdapterModelElement) model,
					IntegrationImages.INBOUND_ADAPTER, IntegrationImages.BADGE_SI_REDIS);
		}
		else if (model instanceof OutboundChannelAdapterModelElement) {
			part = new BadgedIntegrationPart((OutboundChannelAdapterModelElement) model,
					IntegrationImages.OUTBOUND_ADAPTER, IntegrationImages.BADGE_SI_REDIS);
		}
		else if (model instanceof OutboundGatewayModelElement) {
			part = new BadgedIntegrationPart((OutboundGatewayModelElement) model, IntegrationImages.OUTBOUND_GATEWAY,
					IntegrationImages.BADGE_SI_REDIS);
		}
		else if (model instanceof PublishSubscribeChannelModelElement) {
			part = new ChannelGraphicalEditPart((PublishSubscribeChannelModelElement) model,
					IntegrationImages.PUBSUB_CHANNEL, IntegrationImages.BADGE_SI_REDIS);
		}
		else if (model instanceof StoreInboundChannelAdapterModelElement) {
			part = new BadgedIntegrationPart((StoreInboundChannelAdapterModelElement) model,
					IntegrationImages.INBOUND_ADAPTER, IntegrationImages.BADGE_SI_REDIS);
		}
		else if (model instanceof StoreOutboundChannelAdapterModelElement) {
			part = new BadgedIntegrationPart((StoreOutboundChannelAdapterModelElement) model,
					IntegrationImages.OUTBOUND_ADAPTER, IntegrationImages.BADGE_SI_REDIS);
		}
		else if (model instanceof QueueInboundChannelAdapterModelElement) {
			part = new BadgedIntegrationPart((QueueInboundChannelAdapterModelElement) model,
					IntegrationImages.INBOUND_ADAPTER, IntegrationImages.BADGE_SI_REDIS);
		}
		else if (model instanceof QueueInboundGatewayModelElement) {
			part = new BadgedIntegrationPart((QueueInboundGatewayModelElement) model,
					IntegrationImages.INBOUND_GATEWAY, IntegrationImages.BADGE_SI_REDIS);
		}
		else if (model instanceof QueueOutboundChannelAdapterModelElement) {
			part = new BadgedIntegrationPart((QueueOutboundChannelAdapterModelElement) model,
					IntegrationImages.OUTBOUND_ADAPTER, IntegrationImages.BADGE_SI_REDIS);
		}
		else if (model instanceof QueueOutboundGatewayModelElement) {
			part = new BadgedIntegrationPart((QueueOutboundGatewayModelElement) model,
					IntegrationImages.OUTBOUND_GATEWAY, IntegrationImages.BADGE_SI_REDIS);
		}
		return part;
	}

}
