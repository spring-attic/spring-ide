/*******************************************************************************
 * Copyright (c) 2014 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.config.ui.editors.integration.xquery.graph.parts;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.gef.palette.CombinedTemplateCreationEntry;
import org.eclipse.gef.palette.PaletteDrawer;
import org.eclipse.gef.palette.PaletteEntry;
import org.springframework.ide.eclipse.config.core.schemas.IntXquerySchemaConstants;
import org.springframework.ide.eclipse.config.graph.model.AbstractConfigGraphDiagram;
import org.springframework.ide.eclipse.config.graph.model.ModelElementCreationFactory;
import org.springframework.ide.eclipse.config.graph.parts.IPaletteFactory;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.IntegrationImages;
import org.springframework.ide.eclipse.config.ui.editors.integration.xquery.graph.model.XqueryRouterModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.xquery.graph.model.XqueryTransformerModelElement;

public class IntXqueryPaletteFactory implements IPaletteFactory {

	public PaletteDrawer createPaletteDrawer(AbstractConfigGraphDiagram diagram, String namespaceUri) {
		PaletteDrawer drawer = new PaletteDrawer("", IntegrationImages.BADGE_SI_XQUERY); //$NON-NLS-1$
		List<PaletteEntry> entries = new ArrayList<PaletteEntry>();

		CombinedTemplateCreationEntry entry = new CombinedTemplateCreationEntry(
				IntXquerySchemaConstants.ELEM_XQUERY_ROUTER,
				Messages.IntXqueryPaletteFactory_INBOUND_CHANNEL_ADAPTER_COMPONENT_DESCRIPTION,
				new ModelElementCreationFactory(XqueryRouterModelElement.class, diagram, namespaceUri),
				IntegrationImages.ROUTER_SMALL, IntegrationImages.ROUTER);
		entries.add(entry);

		entry = new CombinedTemplateCreationEntry(IntXquerySchemaConstants.ELEM_XQUERY_TRANSFORMER,
				Messages.IntXqueryPaletteFactory_OUTBOUND_CHANNEL_ADAPTER_COMPONENT_DESCRIPTION,
				new ModelElementCreationFactory(XqueryTransformerModelElement.class, diagram, namespaceUri),
				IntegrationImages.TRANSFORMER_SMALL, IntegrationImages.TRANSFORMER);
		entries.add(entry);

		drawer.addAll(entries);
		return drawer;
	}
}
