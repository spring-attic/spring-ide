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
package org.springframework.ide.eclipse.config.ui.editors.integration.amqp.graph.parts;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;
import org.springframework.ide.eclipse.config.ui.editors.integration.amqp.graph.model.ChannelModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.amqp.graph.model.InboundChannelAdapterModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.amqp.graph.model.InboundGatewayModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.amqp.graph.model.OutboundChannelAdapterModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.amqp.graph.model.OutboundGatewayModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.amqp.graph.model.PublishSubscribeChannelModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.IntegrationImages;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.parts.ChannelGraphicalEditPart;

/**
 * @author Leo Dos Santos
 */
public class IntAmqpEditPartFactory implements EditPartFactory {

	public EditPart createEditPart(EditPart context, Object model) {
		EditPart part = null;
		if (model instanceof ChannelModelElement) {
			part = new ChannelGraphicalEditPart((ChannelModelElement) model, IntegrationImages.CHANNEL,
					IntegrationImages.BADGE_SI_AMQP);
		}
		else if (model instanceof InboundChannelAdapterModelElement) {
			part = new InboundChannelAdapterGraphicalEditPart((InboundChannelAdapterModelElement) model);
		}
		else if (model instanceof InboundGatewayModelElement) {
			part = new InboundGatewayGraphicalEditPart((InboundGatewayModelElement) model);
		}
		else if (model instanceof OutboundChannelAdapterModelElement) {
			part = new OutboundChannelAdapterGraphicalEditPart((OutboundChannelAdapterModelElement) model);
		}
		else if (model instanceof OutboundGatewayModelElement) {
			part = new OutboundGatewayGraphicalEditPart((OutboundGatewayModelElement) model);
		}
		else if (model instanceof PublishSubscribeChannelModelElement) {
			part = new ChannelGraphicalEditPart((PublishSubscribeChannelModelElement) model,
					IntegrationImages.PUBSUB_CHANNEL, IntegrationImages.BADGE_SI_AMQP);
		}
		return part;
	}

}
