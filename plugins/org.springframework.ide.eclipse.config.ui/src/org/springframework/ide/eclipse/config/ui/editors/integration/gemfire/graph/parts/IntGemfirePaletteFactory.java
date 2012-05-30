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
package org.springframework.ide.eclipse.config.ui.editors.integration.gemfire.graph.parts;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.gef.palette.CombinedTemplateCreationEntry;
import org.eclipse.gef.palette.PaletteDrawer;
import org.eclipse.gef.palette.PaletteEntry;
import org.springframework.ide.eclipse.config.core.schemas.IntGemfireSchemaConstants;
import org.springframework.ide.eclipse.config.graph.model.AbstractConfigGraphDiagram;
import org.springframework.ide.eclipse.config.graph.model.ModelElementCreationFactory;
import org.springframework.ide.eclipse.config.graph.parts.IPaletteFactory;
import org.springframework.ide.eclipse.config.ui.editors.integration.gemfire.graph.model.CqInboundChannelAdapterModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.gemfire.graph.model.InboundChannelAdapterModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.gemfire.graph.model.OutboundChannelAdapterModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.IntegrationImages;


/**
 * @author Leo Dos Santos
 */
public class IntGemfirePaletteFactory implements IPaletteFactory {

	public PaletteDrawer createPaletteDrawer(AbstractConfigGraphDiagram diagram, String namespaceUri) {
		PaletteDrawer drawer = new PaletteDrawer("", IntegrationImages.BADGE_SI_GEMFIRE); //$NON-NLS-1$
		List<PaletteEntry> entries = new ArrayList<PaletteEntry>();

		CombinedTemplateCreationEntry entry = new CombinedTemplateCreationEntry(
				IntGemfireSchemaConstants.ELEM_CQ_INBOUND_CHANNEL_ADAPTER,
				Messages.IntGemfirePaletteFactory_CQ_INBOUND_CHANNEL_ADAPTER_COMPONENT_DESCRIPTION, new ModelElementCreationFactory(
						CqInboundChannelAdapterModelElement.class, diagram, namespaceUri),
				IntegrationImages.INBOUND_ADAPTER_SMALL, IntegrationImages.INBOUND_ADAPTER);
		entries.add(entry);

		entry = new CombinedTemplateCreationEntry(IntGemfireSchemaConstants.ELEM_INBOUND_CHANNEL_ADAPTER,
				Messages.IntGemfirePaletteFactory_INBOUND_CHANNEL_ADAPTER_COMPONENT_DESCRIPTION, new ModelElementCreationFactory(
						InboundChannelAdapterModelElement.class, diagram, namespaceUri),
				IntegrationImages.INBOUND_ADAPTER_SMALL, IntegrationImages.INBOUND_ADAPTER);
		entries.add(entry);

		entry = new CombinedTemplateCreationEntry(IntGemfireSchemaConstants.ELEM_OUTBOUND_CHANNEL_ADAPTER,
				Messages.IntGemfirePaletteFactory_OUTBOUND_CHANNEL_ADAPTER_COMPONENT_DESCRIPTION, new ModelElementCreationFactory(
						OutboundChannelAdapterModelElement.class, diagram, namespaceUri),
				IntegrationImages.OUTBOUND_ADAPTER_SMALL, IntegrationImages.OUTBOUND_ADAPTER);
		entries.add(entry);

		drawer.addAll(entries);
		return drawer;
	}

}
