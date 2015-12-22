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
package org.springframework.ide.eclipse.config.ui.editors.integration.kafka.graph.parts;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.gef.palette.CombinedTemplateCreationEntry;
import org.eclipse.gef.palette.PaletteDrawer;
import org.eclipse.gef.palette.PaletteEntry;
import org.springframework.ide.eclipse.config.core.schemas.IntKafkaSchemaConstants;
import org.springframework.ide.eclipse.config.graph.model.AbstractConfigGraphDiagram;
import org.springframework.ide.eclipse.config.graph.model.ModelElementCreationFactory;
import org.springframework.ide.eclipse.config.graph.parts.IPaletteFactory;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.IntegrationImages;
import org.springframework.ide.eclipse.config.ui.editors.integration.kafka.graph.model.InboundChannelAdapterModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.kafka.graph.model.MessageDrivenChannelAdapterModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.kafka.graph.model.OutboundChannelAdapterModelElement;

public class IntKafkaPaletteFactory implements IPaletteFactory {

	public PaletteDrawer createPaletteDrawer(AbstractConfigGraphDiagram diagram, String namespaceUri) {
		PaletteDrawer drawer = new PaletteDrawer("", IntegrationImages.BADGE_SI_KAFKA); //$NON-NLS-1$
		List<PaletteEntry> entries = new ArrayList<PaletteEntry>();

		CombinedTemplateCreationEntry entry = new CombinedTemplateCreationEntry(
				IntKafkaSchemaConstants.ELEM_INBOUND_CHANNEL_ADAPTER,
				Messages.IntKafkaPaletteFactory_INBOUND_CHANNEL_ADAPTER_COMPONENT_DESCRIPTION,
				new ModelElementCreationFactory(InboundChannelAdapterModelElement.class, diagram, namespaceUri),
				IntegrationImages.INBOUND_ADAPTER_SMALL, IntegrationImages.INBOUND_ADAPTER);
		entries.add(entry);

		entry = new CombinedTemplateCreationEntry(IntKafkaSchemaConstants.ELEM_OUTBOUND_CHANNEL_ADAPTER,
				Messages.IntKafkaPaletteFactory_OUTBOUND_CHANNEL_ADAPTER_COMPONENT_DESCRIPTION,
				new ModelElementCreationFactory(OutboundChannelAdapterModelElement.class, diagram, namespaceUri),
				IntegrationImages.OUTBOUND_ADAPTER_SMALL, IntegrationImages.OUTBOUND_ADAPTER);
		entries.add(entry);

		entry = new CombinedTemplateCreationEntry(IntKafkaSchemaConstants.ELEM_MESSAGE_DRIVEN_CHANNEL_ADAPTER,
				Messages.IntKafkaPaletteFactory_MESSAGE_DRIVEN_CHANNEL_ADAPTER_COMPONENT_DESCRIPTION,
				new ModelElementCreationFactory(MessageDrivenChannelAdapterModelElement.class, diagram, namespaceUri),
				IntegrationImages.INBOUND_ADAPTER_SMALL, IntegrationImages.INBOUND_ADAPTER);
		entries.add(entry);

		drawer.addAll(entries);
		return drawer;
	}
}
