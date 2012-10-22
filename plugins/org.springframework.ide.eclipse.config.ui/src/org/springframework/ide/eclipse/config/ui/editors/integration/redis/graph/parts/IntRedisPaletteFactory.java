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
package org.springframework.ide.eclipse.config.ui.editors.integration.redis.graph.parts;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.gef.palette.CombinedTemplateCreationEntry;
import org.eclipse.gef.palette.PaletteDrawer;
import org.eclipse.gef.palette.PaletteEntry;
import org.springframework.ide.eclipse.config.core.schemas.IntRedisSchemaConstants;
import org.springframework.ide.eclipse.config.graph.model.AbstractConfigGraphDiagram;
import org.springframework.ide.eclipse.config.graph.model.ModelElementCreationFactory;
import org.springframework.ide.eclipse.config.graph.parts.IPaletteFactory;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.IntegrationImages;
import org.springframework.ide.eclipse.config.ui.editors.integration.redis.graph.model.InboundChannelAdapterModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.redis.graph.model.OutboundChannelAdapterModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.redis.graph.model.PublishSubscribeChannelModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.redis.graph.model.StoreInboundChannelAdapterModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.redis.graph.model.StoreOutboundChannelAdapterModelElement;

/**
 * @author Leo Dos Santos
 */
public class IntRedisPaletteFactory implements IPaletteFactory {

	public PaletteDrawer createPaletteDrawer(AbstractConfigGraphDiagram diagram, String namespaceUri) {
		PaletteDrawer drawer = new PaletteDrawer("", IntegrationImages.BADGE_SI_REDIS); //$NON-NLS-1$
		List<PaletteEntry> entries = new ArrayList<PaletteEntry>();

		CombinedTemplateCreationEntry entry = new CombinedTemplateCreationEntry(
				IntRedisSchemaConstants.ELEM_INBOUND_CHANNEL_ADAPTER,
				Messages.IntRedisPaletteFactory_INBOUND_CHANNEL_ADAPTER_COMPONENT_DESCRIPTION,
				new ModelElementCreationFactory(InboundChannelAdapterModelElement.class, diagram, namespaceUri),
				IntegrationImages.INBOUND_ADAPTER_SMALL, IntegrationImages.INBOUND_ADAPTER);
		entries.add(entry);

		entry = new CombinedTemplateCreationEntry(IntRedisSchemaConstants.ELEM_OUTBOUND_CHANNEL_ADAPTER,
				Messages.IntRedisPaletteFactory_OUTBOUND_CHANNEL_ADAPTER_COMPONENT_DESCRIPTION,
				new ModelElementCreationFactory(OutboundChannelAdapterModelElement.class, diagram, namespaceUri),
				IntegrationImages.OUTBOUND_ADAPTER_SMALL, IntegrationImages.OUTBOUND_ADAPTER);
		entries.add(entry);

		entry = new CombinedTemplateCreationEntry(IntRedisSchemaConstants.ELEM_PUBLISH_SUBSCRIBE_CHANNEL,
				Messages.IntRedisPaletteFactory_PUBLISH_SUBSCRIBE_CHANNEL_COMPONENT_DESCRIPTION,
				new ModelElementCreationFactory(PublishSubscribeChannelModelElement.class, diagram, namespaceUri),
				IntegrationImages.PUBSUB_CHANNEL_SMALL, IntegrationImages.PUBSUB_CHANNEL);
		entries.add(entry);

		entry = new CombinedTemplateCreationEntry(IntRedisSchemaConstants.ELEM_STORE_INBOUND_CHANNEL_ADAPTER,
				Messages.IntRedisPaletteFactory_STORE_INBOUND_CHANNEL_ADAPTER_COMPONENT_DESCRIPTION, new ModelElementCreationFactory(
						StoreInboundChannelAdapterModelElement.class, diagram, namespaceUri),
				IntegrationImages.INBOUND_ADAPTER_SMALL, IntegrationImages.INBOUND_ADAPTER);
		entries.add(entry);

		entry = new CombinedTemplateCreationEntry(IntRedisSchemaConstants.ELEM_STORE_OUTBOUND_CHANNEL_ADAPTER,
				Messages.IntRedisPaletteFactory_STORE_OUTBOUND_CHANNEL_ADAPTER_COMPONENT_DESCRIPTION, new ModelElementCreationFactory(
						StoreOutboundChannelAdapterModelElement.class, diagram, namespaceUri),
				IntegrationImages.OUTBOUND_ADAPTER_SMALL, IntegrationImages.OUTBOUND_ADAPTER);
		entries.add(entry);

		drawer.addAll(entries);
		return drawer;
	}

}
