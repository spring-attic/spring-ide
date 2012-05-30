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
package org.springframework.ide.eclipse.config.ui.editors.integration.mail.graph.parts;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.gef.palette.CombinedTemplateCreationEntry;
import org.eclipse.gef.palette.PaletteDrawer;
import org.eclipse.gef.palette.PaletteEntry;
import org.springframework.ide.eclipse.config.core.schemas.IntMailSchemaConstants;
import org.springframework.ide.eclipse.config.graph.model.AbstractConfigGraphDiagram;
import org.springframework.ide.eclipse.config.graph.model.ModelElementCreationFactory;
import org.springframework.ide.eclipse.config.graph.parts.IPaletteFactory;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.IntegrationImages;
import org.springframework.ide.eclipse.config.ui.editors.integration.mail.graph.model.HeaderEnricherModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.mail.graph.model.ImapIdleChannelAdapterModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.mail.graph.model.InboundChannelAdapterModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.mail.graph.model.MailToStringTransformerModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.mail.graph.model.OutboundChannelAdapterModelElement;


/**
 * @author Leo Dos Santos
 */
public class IntMailPaletteFactory implements IPaletteFactory {

	public PaletteDrawer createPaletteDrawer(AbstractConfigGraphDiagram diagram, String namespaceUri) {
		PaletteDrawer drawer = new PaletteDrawer("", IntegrationImages.BADGE_SI_MAIL); //$NON-NLS-1$
		List<PaletteEntry> entries = new ArrayList<PaletteEntry>();

		CombinedTemplateCreationEntry entry = new CombinedTemplateCreationEntry(
				IntMailSchemaConstants.ELEM_HEADER_ENRICHER, Messages.IntMailPaletteFactory_HEADER_ENRICHER_COMPONENT_DESCRIPTION,
				new ModelElementCreationFactory(HeaderEnricherModelElement.class, diagram, namespaceUri),
				IntegrationImages.ENRICHER_SMALL, IntegrationImages.ENRICHER);
		entries.add(entry);

		entry = new CombinedTemplateCreationEntry(IntMailSchemaConstants.ELEM_IMAP_IDLE_CHANNEL_ADAPTER,
				Messages.IntMailPaletteFactory_IMAP_IDLE_CHANNEL_ADAPTER_COMPONENT_DESCRIPTION, new ModelElementCreationFactory(
						ImapIdleChannelAdapterModelElement.class, diagram, namespaceUri),
				IntegrationImages.INBOUND_ADAPTER_SMALL, IntegrationImages.INBOUND_ADAPTER);
		entries.add(entry);

		entry = new CombinedTemplateCreationEntry(IntMailSchemaConstants.ELEM_INBOUND_CHANNEL_ADAPTER,
				Messages.IntMailPaletteFactory_INBOUND_CHANNEL_ADAPTER_COMPONENT_DESCRIPTION, new ModelElementCreationFactory(
						InboundChannelAdapterModelElement.class, diagram, namespaceUri),
				IntegrationImages.INBOUND_ADAPTER_SMALL, IntegrationImages.INBOUND_ADAPTER);
		entries.add(entry);

		entry = new CombinedTemplateCreationEntry(IntMailSchemaConstants.ELEM_MAIL_TO_STRING_TRANSFORMER,
				Messages.IntMailPaletteFactory_MAIL_TO_STRING_TRANSFORMER_COMPONENT_DESCRIPTION, new ModelElementCreationFactory(
						MailToStringTransformerModelElement.class, diagram, namespaceUri),
				IntegrationImages.TRANSFORMER_SMALL, IntegrationImages.TRANSFORMER);
		entries.add(entry);

		entry = new CombinedTemplateCreationEntry(IntMailSchemaConstants.ELEM_OUTBOUND_CHANNEL_ADAPTER,
				Messages.IntMailPaletteFactory_OUTBOUND_CHANNEL_ADAPTER_COMPONENT_DESCRIPTION, new ModelElementCreationFactory(
						OutboundChannelAdapterModelElement.class, diagram, namespaceUri),
				IntegrationImages.OUTBOUND_ADAPTER_SMALL, IntegrationImages.OUTBOUND_ADAPTER);
		entries.add(entry);

		drawer.addAll(entries);
		return drawer;
	}

}
