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
package org.springframework.ide.eclipse.config.ui.editors.integration.ip.graph.parts;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.gef.palette.CombinedTemplateCreationEntry;
import org.eclipse.gef.palette.PaletteDrawer;
import org.eclipse.gef.palette.PaletteEntry;
import org.springframework.ide.eclipse.config.core.schemas.IntIpSchemaConstants;
import org.springframework.ide.eclipse.config.graph.model.AbstractConfigGraphDiagram;
import org.springframework.ide.eclipse.config.graph.model.ModelElementCreationFactory;
import org.springframework.ide.eclipse.config.graph.parts.IPaletteFactory;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.IntegrationImages;
import org.springframework.ide.eclipse.config.ui.editors.integration.ip.graph.model.TcpInboundChannelAdapterModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.ip.graph.model.TcpInboundGatewayModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.ip.graph.model.TcpOutboundChannelAdapterModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.ip.graph.model.TcpOutboundGatewayModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.ip.graph.model.UdpInboundChannelAdapterModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.ip.graph.model.UdpOutboundChannelAdapterModelElement;


/**
 * @author Leo Dos Santos
 */
public class IntIpPaletteFactory implements IPaletteFactory {

	public PaletteDrawer createPaletteDrawer(AbstractConfigGraphDiagram diagram, String namespaceUri) {
		PaletteDrawer drawer = new PaletteDrawer("", IntegrationImages.BADGE_SI_IP); //$NON-NLS-1$
		List<PaletteEntry> entries = new ArrayList<PaletteEntry>();

		CombinedTemplateCreationEntry entry = new CombinedTemplateCreationEntry(
				IntIpSchemaConstants.ELEM_TCP_INBOUND_CHANNEL_ADAPTER,
				Messages.IntIpPaletteFactory_TCP_INBOUND_CHANNEL_ADAPTER_COMPONENT_DESCRIPTION,
				new ModelElementCreationFactory(TcpInboundChannelAdapterModelElement.class, diagram, namespaceUri),
				IntegrationImages.INBOUND_ADAPTER_SMALL, IntegrationImages.INBOUND_ADAPTER);
		entries.add(entry);

		entry = new CombinedTemplateCreationEntry(IntIpSchemaConstants.ELEM_TCP_INBOUND_GATEWAY,
				Messages.IntIpPaletteFactory_TCP_INBOUND_GATEWAY_COMPONENT_DESCRIPTION,
				new ModelElementCreationFactory(TcpInboundGatewayModelElement.class, diagram, namespaceUri),
				IntegrationImages.INBOUND_GATEWAY_SMALL, IntegrationImages.INBOUND_GATEWAY);
		entries.add(entry);

		entry = new CombinedTemplateCreationEntry(IntIpSchemaConstants.ELEM_TCP_OUTBOUND_CHANNEL_ADAPTER,
				Messages.IntIpPaletteFactory_TCP_OUTBOUND_CHANNEL_ADAPTER_COMPONENT_DESCRIPTION,
				new ModelElementCreationFactory(TcpOutboundChannelAdapterModelElement.class, diagram, namespaceUri),
				IntegrationImages.OUTBOUND_ADAPTER_SMALL, IntegrationImages.OUTBOUND_ADAPTER);
		entries.add(entry);

		entry = new CombinedTemplateCreationEntry(IntIpSchemaConstants.ELEM_TCP_OUTBOUND_GATEWAY,
				Messages.IntIpPaletteFactory_TCP_OUTBOUND_GATEWAY_COMPONENT_DESCRIPTION,
				new ModelElementCreationFactory(TcpOutboundGatewayModelElement.class, diagram, namespaceUri),
				IntegrationImages.OUTBOUND_GATEWAY_SMALL, IntegrationImages.OUTBOUND_GATEWAY);
		entries.add(entry);

		entry = new CombinedTemplateCreationEntry(IntIpSchemaConstants.ELEM_UDP_INBOUND_CHANNEL_ADAPTER,
				Messages.IntIpPaletteFactory_UDP_INBOUND_CHANNEL_ADAPTER_COMPONENT_DESCRIPTION,
				new ModelElementCreationFactory(UdpInboundChannelAdapterModelElement.class, diagram, namespaceUri),
				IntegrationImages.INBOUND_ADAPTER_SMALL, IntegrationImages.INBOUND_ADAPTER);
		entries.add(entry);

		entry = new CombinedTemplateCreationEntry(IntIpSchemaConstants.ELEM_UDP_OUTBOUND_CHANNEL_ADAPTER,
				Messages.IntIpPaletteFactory_UDP_OUTBOUND_CHANNEL_ADAPTER_COMPONENT_DESCRIPTION,
				new ModelElementCreationFactory(UdpOutboundChannelAdapterModelElement.class, diagram, namespaceUri),
				IntegrationImages.OUTBOUND_ADAPTER_SMALL, IntegrationImages.OUTBOUND_ADAPTER);
		entries.add(entry);

		drawer.addAll(entries);
		return drawer;
	}

}
