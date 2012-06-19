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
package org.springframework.ide.eclipse.config.ui.editors.integration.jms.graph.model;

import java.util.List;

import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
import org.springframework.ide.eclipse.config.core.schemas.IntJmsSchemaConstants;
import org.springframework.ide.eclipse.config.graph.model.Activity;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.model.AbstractIntegrationModelFactory;


/**
 * @author Leo Dos Santos
 */
@SuppressWarnings("restriction")
public class IntJmsModelFactory extends AbstractIntegrationModelFactory {

	public void getChildrenFromXml(List<Activity> list, IDOMElement input, Activity parent) {
		if (input.getLocalName().equals(IntJmsSchemaConstants.ELEM_CHANNEL)) {
			ChannelModelElement channel = new ChannelModelElement(input, parent.getDiagram());
			list.add(channel);
		}
		else if (input.getLocalName().equals(IntJmsSchemaConstants.ELEM_HEADER_ENRICHER)) {
			HeaderEnricherModelElement enricher = new HeaderEnricherModelElement(input, parent.getDiagram());
			list.add(enricher);
		}
		else if (input.getLocalName().equals(IntJmsSchemaConstants.ELEM_INBOUND_CHANNEL_ADAPTER)) {
			InboundChannelAdapterModelElement adapter = new InboundChannelAdapterModelElement(input, parent
					.getDiagram());
			list.add(adapter);
		}
		else if (input.getLocalName().equals(IntJmsSchemaConstants.ELEM_INBOUND_GATEWAY)) {
			InboundGatewayModelElement gateway = new InboundGatewayModelElement(input, parent.getDiagram());
			list.add(gateway);
		}
		else if (input.getLocalName().equals(IntJmsSchemaConstants.ELEM_MESSAGE_DRIVEN_CHANNEL_ADAPTER)) {
			MessageDrivenChannelAdapterModelElement adapter = new MessageDrivenChannelAdapterModelElement(input, parent
					.getDiagram());
			list.add(adapter);
		}
		else if (input.getLocalName().equals(IntJmsSchemaConstants.ELEM_OUTBOUND_CHANNEL_ADAPTER)) {
			OutboundChannelAdapterModelElement adapter = new OutboundChannelAdapterModelElement(input, parent
					.getDiagram());
			list.add(adapter);
		}
		else if (input.getLocalName().equals(IntJmsSchemaConstants.ELEM_OUTBOUND_GATEWAY)) {
			OutboundGatewayModelElement adapter = new OutboundGatewayModelElement(input, parent.getDiagram());
			list.add(adapter);
		}
		else if (input.getLocalName().equals(IntJmsSchemaConstants.ELEM_PUBLISH_SUBSCRIBE_CHANNEL)) {
			PublishSubscribeChannelModelElement channel = new PublishSubscribeChannelModelElement(input, parent
					.getDiagram());
			list.add(channel);
		}
	}

}
