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
package org.springframework.ide.eclipse.config.ui.editors.integration.xmpp.graph.parts;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.gef.palette.CombinedTemplateCreationEntry;
import org.eclipse.gef.palette.PaletteDrawer;
import org.eclipse.gef.palette.PaletteEntry;
import org.springframework.ide.eclipse.config.core.schemas.IntXmppSchemaConstants;
import org.springframework.ide.eclipse.config.graph.model.AbstractConfigGraphDiagram;
import org.springframework.ide.eclipse.config.graph.model.ModelElementCreationFactory;
import org.springframework.ide.eclipse.config.graph.parts.IPaletteFactory;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.IntegrationImages;
import org.springframework.ide.eclipse.config.ui.editors.integration.xmpp.graph.model.HeaderEnricherModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.xmpp.graph.model.InboundChannelAdapterModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.xmpp.graph.model.OutboundChannelAdapterModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.xmpp.graph.model.PresenceInboundChannelAdapterModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.xmpp.graph.model.PresenceOutboundChannelAdapterModelElement;


/**
 * @author Leo Dos Santos
 */
public class IntXmppPaletteFactory implements IPaletteFactory {

	public PaletteDrawer createPaletteDrawer(AbstractConfigGraphDiagram diagram, String namespaceUri) {
		PaletteDrawer drawer = new PaletteDrawer("", IntegrationImages.BADGE_SI_XMPP); //$NON-NLS-1$
		List<PaletteEntry> entries = new ArrayList<PaletteEntry>();

		CombinedTemplateCreationEntry entry = new CombinedTemplateCreationEntry(
				IntXmppSchemaConstants.ELEM_HEADER_ENRICHER,
				Messages.getString("IntXmppPaletteFactory.HEADER_ENRICHER_COMPONENT_DESCRIPTION"), //$NON-NLS-1$
				new ModelElementCreationFactory(HeaderEnricherModelElement.class, diagram, namespaceUri),
				IntegrationImages.ENRICHER_SMALL, IntegrationImages.ENRICHER);
		entries.add(entry);

		entry = new CombinedTemplateCreationEntry(
				IntXmppSchemaConstants.ELEM_INBOUND_CHANNEL_ADAPTER,
				Messages.getString("IntXmppPaletteFactory.INBOUND_CHANNEL_ADAPTER_COMPONENT_DESCRIPTION"), new ModelElementCreationFactory( //$NON-NLS-1$
						InboundChannelAdapterModelElement.class, diagram, namespaceUri),
				IntegrationImages.INBOUND_ADAPTER_SMALL, IntegrationImages.INBOUND_ADAPTER);
		entries.add(entry);

		entry = new CombinedTemplateCreationEntry(
				IntXmppSchemaConstants.ELEM_OUTBOUND_CHANNEL_ADAPTER,
				Messages.getString("IntXmppPaletteFactory.OUTBOUND_CHANNEL_ADAPTER_COMPONENT_DESCRIPTION"), new ModelElementCreationFactory( //$NON-NLS-1$
						OutboundChannelAdapterModelElement.class, diagram, namespaceUri),
				IntegrationImages.OUTBOUND_ADAPTER_SMALL, IntegrationImages.OUTBOUND_ADAPTER);
		entries.add(entry);

		entry = new CombinedTemplateCreationEntry(
				IntXmppSchemaConstants.ELEM_PRESENCE_INBOUND_CHANNEL_ADAPTER,
				Messages.getString("IntXmppPaletteFactory.PRESENCE_INBOUND_CHANNEL_ADAPTER_COMPONENT_DESCRIPTION"), new ModelElementCreationFactory( //$NON-NLS-1$
						PresenceInboundChannelAdapterModelElement.class, diagram, namespaceUri),
				IntegrationImages.INBOUND_ADAPTER_SMALL, IntegrationImages.INBOUND_ADAPTER);
		entries.add(entry);

		entry = new CombinedTemplateCreationEntry(
				IntXmppSchemaConstants.ELEM_PRESENCE_OUTBOUND_CHANNEL_ADAPTER,
				Messages.getString("IntXmppPaletteFactory.PRESENCE_OUTBOUND_CHANNEL_ADAPTER_COMPONENT_DESCRIPTION"), new ModelElementCreationFactory( //$NON-NLS-1$
						PresenceOutboundChannelAdapterModelElement.class, diagram, namespaceUri),
				IntegrationImages.OUTBOUND_ADAPTER_SMALL, IntegrationImages.OUTBOUND_ADAPTER);
		entries.add(entry);

		drawer.addAll(entries);
		return drawer;
	}
}
