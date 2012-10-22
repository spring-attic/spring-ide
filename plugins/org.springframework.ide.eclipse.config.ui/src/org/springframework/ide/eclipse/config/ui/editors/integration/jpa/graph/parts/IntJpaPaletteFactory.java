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
package org.springframework.ide.eclipse.config.ui.editors.integration.jpa.graph.parts;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.gef.palette.CombinedTemplateCreationEntry;
import org.eclipse.gef.palette.PaletteDrawer;
import org.eclipse.gef.palette.PaletteEntry;
import org.springframework.ide.eclipse.config.core.schemas.IntJpaSchemaConstants;
import org.springframework.ide.eclipse.config.graph.model.AbstractConfigGraphDiagram;
import org.springframework.ide.eclipse.config.graph.model.ModelElementCreationFactory;
import org.springframework.ide.eclipse.config.graph.parts.IPaletteFactory;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.IntegrationImages;
import org.springframework.ide.eclipse.config.ui.editors.integration.jpa.graph.model.InboundChannelAdapterModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.jpa.graph.model.OutboundChannelAdapterModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.jpa.graph.model.RetrievingOutboundGatewayModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.jpa.graph.model.UpdatingOutboundGatewayModelElement;

/**
 * @author Leo Dos Santos
 */
public class IntJpaPaletteFactory implements IPaletteFactory {

	public PaletteDrawer createPaletteDrawer(AbstractConfigGraphDiagram diagram, String namespaceUri) {
		PaletteDrawer drawer = new PaletteDrawer("", IntegrationImages.BADGE_SI_JPA); //$NON-NLS-1$
		List<PaletteEntry> entries = new ArrayList<PaletteEntry>();

		CombinedTemplateCreationEntry entry = new CombinedTemplateCreationEntry(
				IntJpaSchemaConstants.ELEM_INBOUND_CHANNEL_ADAPTER, Messages.IntJpaPaletteFactory_INBOUND_CHANNEL_ADAPTER_COMPONENT_DESCRIPTION,
				new ModelElementCreationFactory(InboundChannelAdapterModelElement.class, diagram, namespaceUri),
				IntegrationImages.INBOUND_ADAPTER_SMALL, IntegrationImages.INBOUND_ADAPTER);
		entries.add(entry);

		entry = new CombinedTemplateCreationEntry(IntJpaSchemaConstants.ELEM_OUTBOUND_CHANNEL_ADAPTER,
				Messages.IntJpaPaletteFactory_OUTBOUND_CHANNEL_ADAPTER_COMPONENT_DESCRIPTION, new ModelElementCreationFactory(
						OutboundChannelAdapterModelElement.class, diagram, namespaceUri),
				IntegrationImages.OUTBOUND_ADAPTER_SMALL, IntegrationImages.OUTBOUND_ADAPTER);
		entries.add(entry);

		entry = new CombinedTemplateCreationEntry(IntJpaSchemaConstants.ELEM_RETRIEVING_OUTBOUND_GATEWAY,
				Messages.IntJpaPaletteFactory_RETRIEVING_OUTBOUND_GATEWAY_COMPONENT_DESCRIPTION, new ModelElementCreationFactory(
						RetrievingOutboundGatewayModelElement.class, diagram, namespaceUri),
				IntegrationImages.OUTBOUND_GATEWAY_SMALL, IntegrationImages.OUTBOUND_GATEWAY);
		entries.add(entry);

		entry = new CombinedTemplateCreationEntry(IntJpaSchemaConstants.ELEM_UPDATING_OUTBOUND_GATEWAY,
				Messages.IntJpaPaletteFactory_UPDATING_OUTBOUND_GATEWAY_COMPONENT_DESCRIPTION, new ModelElementCreationFactory(
						UpdatingOutboundGatewayModelElement.class, diagram, namespaceUri),
				IntegrationImages.OUTBOUND_GATEWAY_SMALL, IntegrationImages.OUTBOUND_GATEWAY);
		entries.add(entry);

		drawer.addAll(entries);
		return drawer;
	}

}
