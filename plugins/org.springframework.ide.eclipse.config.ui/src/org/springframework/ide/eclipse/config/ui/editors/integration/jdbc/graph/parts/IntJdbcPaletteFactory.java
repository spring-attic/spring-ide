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
package org.springframework.ide.eclipse.config.ui.editors.integration.jdbc.graph.parts;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.gef.palette.CombinedTemplateCreationEntry;
import org.eclipse.gef.palette.PaletteDrawer;
import org.eclipse.gef.palette.PaletteEntry;
import org.springframework.ide.eclipse.config.core.schemas.IntJdbcSchemaConstants;
import org.springframework.ide.eclipse.config.graph.model.AbstractConfigGraphDiagram;
import org.springframework.ide.eclipse.config.graph.model.ModelElementCreationFactory;
import org.springframework.ide.eclipse.config.graph.parts.IPaletteFactory;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.IntegrationImages;
import org.springframework.ide.eclipse.config.ui.editors.integration.jdbc.graph.model.InboundChannelAdapterModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.jdbc.graph.model.OutboundChannelAdapterModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.jdbc.graph.model.OutboundGatewayModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.jdbc.graph.model.StoredProcInboundChannelAdapterModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.jdbc.graph.model.StoredProcOutboundChannelAdapterModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.jdbc.graph.model.StoredProcOutboundGatewayModelElement;


/**
 * @author Leo Dos Santos
 */
public class IntJdbcPaletteFactory implements IPaletteFactory {

	public PaletteDrawer createPaletteDrawer(AbstractConfigGraphDiagram diagram, String namespaceUri) {
		PaletteDrawer drawer = new PaletteDrawer("", IntegrationImages.BADGE_SI_JDBC); //$NON-NLS-1$
		List<PaletteEntry> entries = new ArrayList<PaletteEntry>();

		CombinedTemplateCreationEntry entry = new CombinedTemplateCreationEntry(
				IntJdbcSchemaConstants.ELEM_INBOUND_CHANNEL_ADAPTER,
				Messages.IntJdbcPaletteFactory_INBOUND_CHANNEL_ADAPTER_COMPONENT_DESCRIPTION,
				new ModelElementCreationFactory(InboundChannelAdapterModelElement.class, diagram, namespaceUri),
				IntegrationImages.INBOUND_ADAPTER_SMALL, IntegrationImages.INBOUND_ADAPTER);
		entries.add(entry);

		entry = new CombinedTemplateCreationEntry(IntJdbcSchemaConstants.ELEM_OUTBOUND_CHANNEL_ADAPTER,
				Messages.IntJdbcPaletteFactory_OUTBOUND_CHANNEL_ADAPTER_COMPONENT_DESCRIPTION,
				new ModelElementCreationFactory(OutboundChannelAdapterModelElement.class, diagram, namespaceUri),
				IntegrationImages.OUTBOUND_ADAPTER_SMALL, IntegrationImages.OUTBOUND_ADAPTER);
		entries.add(entry);

		entry = new CombinedTemplateCreationEntry(IntJdbcSchemaConstants.ELEM_OUTBOUND_GATEWAY,
				Messages.IntJdbcPaletteFactory_OUTBOUND_GATEWAY_COMPONENT_DESCRIPTION, new ModelElementCreationFactory(
						OutboundGatewayModelElement.class, diagram, namespaceUri),
				IntegrationImages.OUTBOUND_GATEWAY_SMALL, IntegrationImages.OUTBOUND_GATEWAY);
		entries.add(entry);

		entry = new CombinedTemplateCreationEntry(IntJdbcSchemaConstants.ELEM_STORED_PROC_INBOUND_CHANNEL_ADAPTER,
				Messages.IntJdbcPaletteFactory_STORED_PROC_INBOUND_CHANNEL_ADAPTER_COMPONENT_DESCRIPTION, new ModelElementCreationFactory(
						StoredProcInboundChannelAdapterModelElement.class, diagram, namespaceUri),
				IntegrationImages.INBOUND_ADAPTER_SMALL, IntegrationImages.INBOUND_ADAPTER);
		entries.add(entry);

		entry = new CombinedTemplateCreationEntry(IntJdbcSchemaConstants.ELEM_STORED_PROC_OUTBOUND_CHANNEL_ADAPTER,
				Messages.IntJdbcPaletteFactory_STORED_PROC_OUTBOUND_CHANNEL_ADAPTER_COMPONENT_DESCRIPTION, new ModelElementCreationFactory(
						StoredProcOutboundChannelAdapterModelElement.class, diagram, namespaceUri),
				IntegrationImages.OUTBOUND_ADAPTER_SMALL, IntegrationImages.OUTBOUND_ADAPTER);
		entries.add(entry);

		entry = new CombinedTemplateCreationEntry(IntJdbcSchemaConstants.ELEM_STORED_PROC_OUTBOUND_GATEWAY,
				Messages.IntJdbcPaletteFactory_STORED_PROC_OUTBOUND_GATEWAY_COMPONENT_DESCRIPTION, new ModelElementCreationFactory(
						StoredProcOutboundGatewayModelElement.class, diagram, namespaceUri),
				IntegrationImages.OUTBOUND_GATEWAY_SMALL, IntegrationImages.OUTBOUND_GATEWAY);
		entries.add(entry);

		drawer.addAll(entries);
		return drawer;
	}

}
