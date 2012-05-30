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
package org.springframework.ide.eclipse.config.ui.editors.integration.amqp.graph.parts;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.gef.palette.CombinedTemplateCreationEntry;
import org.eclipse.gef.palette.PaletteDrawer;
import org.eclipse.gef.palette.PaletteEntry;
import org.springframework.ide.eclipse.config.core.schemas.IntAmqpSchemaConstants;
import org.springframework.ide.eclipse.config.graph.model.AbstractConfigGraphDiagram;
import org.springframework.ide.eclipse.config.graph.model.ModelElementCreationFactory;
import org.springframework.ide.eclipse.config.graph.parts.IPaletteFactory;
import org.springframework.ide.eclipse.config.ui.editors.integration.amqp.graph.model.ChannelModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.amqp.graph.model.InboundChannelAdapterModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.amqp.graph.model.InboundGatewayModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.amqp.graph.model.OutboundChannelAdapterModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.amqp.graph.model.OutboundGatewayModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.amqp.graph.model.PublishSubscribeChannelModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.IntegrationImages;


/**
 * @author Leo Dos Santos
 */
public class IntAmqpPaletteFactory implements IPaletteFactory {

	public PaletteDrawer createPaletteDrawer(AbstractConfigGraphDiagram diagram, String namespaceUri) {
		PaletteDrawer drawer = new PaletteDrawer("", IntegrationImages.BADGE_SI_AMQP); //$NON-NLS-1$
		List<PaletteEntry> entries = new ArrayList<PaletteEntry>();

		CombinedTemplateCreationEntry entry = new CombinedTemplateCreationEntry(IntAmqpSchemaConstants.ELEM_CHANNEL,
				Messages.IntAmqpPaletteFactory_CHANNEL_COMPONENT_DESCRIPTION, new ModelElementCreationFactory(ChannelModelElement.class, diagram,
						namespaceUri), IntegrationImages.CHANNEL_SMALL, IntegrationImages.CHANNEL);
		entries.add(entry);

		entry = new CombinedTemplateCreationEntry(IntAmqpSchemaConstants.ELEM_INBOUND_CHANNEL_ADAPTER,
				Messages.IntAmqpPaletteFactory_INBOUND_CHANNEL_ADAPTER_COMPONENT_DESCRIPTION,
				new ModelElementCreationFactory(InboundChannelAdapterModelElement.class, diagram, namespaceUri),
				IntegrationImages.INBOUND_ADAPTER_SMALL, IntegrationImages.INBOUND_ADAPTER);
		entries.add(entry);

		entry = new CombinedTemplateCreationEntry(IntAmqpSchemaConstants.ELEM_INBOUND_GATEWAY,
				Messages.IntAmqpPaletteFactory_INBOUND_GATEWAY_COMPONENT_DESCRIPTION, new ModelElementCreationFactory(
						InboundGatewayModelElement.class, diagram, namespaceUri),
				IntegrationImages.INBOUND_GATEWAY_SMALL, IntegrationImages.INBOUND_GATEWAY);
		entries.add(entry);

		entry = new CombinedTemplateCreationEntry(IntAmqpSchemaConstants.ELEM_OUTBOUND_CHANNEL_ADAPTER,
				Messages.IntAmqpPaletteFactory_OUTBOUND_CHANNEL_ADAPTER_COMPONENT_DESCRIPTION,
				new ModelElementCreationFactory(OutboundChannelAdapterModelElement.class, diagram, namespaceUri),
				IntegrationImages.OUTBOUND_ADAPTER_SMALL, IntegrationImages.OUTBOUND_ADAPTER);
		entries.add(entry);

		entry = new CombinedTemplateCreationEntry(IntAmqpSchemaConstants.ELEM_OUTBOUND_GATEWAY,
				Messages.IntAmqpPaletteFactory_OUTBOUND_GATEWAY_COMPONENT_DESCRIPTION, new ModelElementCreationFactory(
						OutboundGatewayModelElement.class, diagram, namespaceUri),
				IntegrationImages.OUTBOUND_GATEWAY_SMALL, IntegrationImages.OUTBOUND_GATEWAY);
		entries.add(entry);

		entry = new CombinedTemplateCreationEntry(IntAmqpSchemaConstants.ELEM_PUBLISH_SUBSCRIBE_CHANNEL,
				Messages.IntAmqpPaletteFactory_PUBLISH_SUBSCRIBE_CHANNEL_COMPONENT_DESCRIPTION, new ModelElementCreationFactory(
						PublishSubscribeChannelModelElement.class, diagram, namespaceUri),
				IntegrationImages.PUBSUB_CHANNEL_SMALL, IntegrationImages.PUBSUB_CHANNEL);

		drawer.addAll(entries);
		return drawer;
	}

}
