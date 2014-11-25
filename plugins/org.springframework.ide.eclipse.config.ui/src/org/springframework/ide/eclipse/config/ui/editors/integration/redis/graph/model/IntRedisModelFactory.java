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
package org.springframework.ide.eclipse.config.ui.editors.integration.redis.graph.model;

import java.util.List;

import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
import org.springframework.ide.eclipse.config.core.schemas.IntRedisSchemaConstants;
import org.springframework.ide.eclipse.config.graph.model.Activity;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.model.AbstractIntegrationModelFactory;

/**
 * @author Leo Dos Santos
 */
@SuppressWarnings("restriction")
public class IntRedisModelFactory extends AbstractIntegrationModelFactory {

	public void getChildrenFromXml(List<Activity> list, IDOMElement input, Activity parent) {
		if (input.getLocalName().equals(IntRedisSchemaConstants.ELEM_INBOUND_CHANNEL_ADAPTER)) {
			InboundChannelAdapterModelElement adapter = new InboundChannelAdapterModelElement(input,
					parent.getDiagram());
			list.add(adapter);
		}
		else if (input.getLocalName().equals(IntRedisSchemaConstants.ELEM_OUTBOUND_CHANNEL_ADAPTER)) {
			OutboundChannelAdapterModelElement adapter = new OutboundChannelAdapterModelElement(input,
					parent.getDiagram());
			list.add(adapter);
		}
		else if (input.getLocalName().equals(IntRedisSchemaConstants.ELEM_OUTBOUND_GATEWAY)) {
			OutboundGatewayModelElement adapter = new OutboundGatewayModelElement(input, parent.getDiagram());
			list.add(adapter);
		}
		else if (input.getLocalName().equals(IntRedisSchemaConstants.ELEM_PUBLISH_SUBSCRIBE_CHANNEL)) {
			PublishSubscribeChannelModelElement channel = new PublishSubscribeChannelModelElement(input,
					parent.getDiagram());
			list.add(channel);
		}
		else if (input.getLocalName().equals(IntRedisSchemaConstants.ELEM_STORE_INBOUND_CHANNEL_ADAPTER)) {
			StoreInboundChannelAdapterModelElement adapter = new StoreInboundChannelAdapterModelElement(input,
					parent.getDiagram());
			list.add(adapter);
		}
		else if (input.getLocalName().equals(IntRedisSchemaConstants.ELEM_STORE_OUTBOUND_CHANNEL_ADAPTER)) {
			StoreOutboundChannelAdapterModelElement adapter = new StoreOutboundChannelAdapterModelElement(input,
					parent.getDiagram());
			list.add(adapter);
		}
		else if (input.getLocalName().equals(IntRedisSchemaConstants.ELEM_QUEUE_INBOUND_CHANNEL_ADAPTER)) {
			QueueInboundChannelAdapterModelElement adapter = new QueueInboundChannelAdapterModelElement(input,
					parent.getDiagram());
			list.add(adapter);
		}
		else if (input.getLocalName().equals(IntRedisSchemaConstants.ELEM_QUEUE_INBOUND_GATEWAY)) {
			QueueInboundGatewayModelElement adapter = new QueueInboundGatewayModelElement(input, parent.getDiagram());
			list.add(adapter);
		}
		else if (input.getLocalName().equals(IntRedisSchemaConstants.ELEM_QUEUE_OUTBOUND_CHANNEL_ADAPTER)) {
			QueueOutboundChannelAdapterModelElement adapter = new QueueOutboundChannelAdapterModelElement(input,
					parent.getDiagram());
			list.add(adapter);
		}
		else if (input.getLocalName().equals(IntRedisSchemaConstants.ELEM_QUEUE_OUTBOUND_GATEWAY)) {
			QueueOutboundGatewayModelElement adapter = new QueueOutboundGatewayModelElement(input, parent.getDiagram());
			list.add(adapter);
		}
	}

}
