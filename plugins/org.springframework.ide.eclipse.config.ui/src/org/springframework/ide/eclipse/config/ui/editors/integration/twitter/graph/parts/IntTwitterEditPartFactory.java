/*******************************************************************************
 *  Copyright (c) 2012, 2014 Pivotal Software Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      Pivotal Software Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.config.ui.editors.integration.twitter.graph.parts;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.IntegrationImages;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.parts.BadgedIntegrationPart;
import org.springframework.ide.eclipse.config.ui.editors.integration.twitter.graph.model.DmInboundChannelAdapterModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.twitter.graph.model.DmOutboundChannelAdapterModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.twitter.graph.model.InboundChannelAdapterModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.twitter.graph.model.MentionsInboundChannelAdapterModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.twitter.graph.model.OutboundChannelAdapterModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.twitter.graph.model.SearchInboundChannelAdapterModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.twitter.graph.model.SearchOutboundGatewayModelElement;

/**
 * @author Leo Dos Santos
 */
public class IntTwitterEditPartFactory implements EditPartFactory {

	public EditPart createEditPart(EditPart context, Object model) {
		EditPart part = null;
		if (model instanceof DmInboundChannelAdapterModelElement) {
			part = new BadgedIntegrationPart((DmInboundChannelAdapterModelElement) model,
					IntegrationImages.INBOUND_ADAPTER, IntegrationImages.BADGE_SI_TWITTER);
		}
		else if (model instanceof DmOutboundChannelAdapterModelElement) {
			part = new BadgedIntegrationPart((DmOutboundChannelAdapterModelElement) model,
					IntegrationImages.OUTBOUND_ADAPTER, IntegrationImages.BADGE_SI_TWITTER);
		}
		else if (model instanceof InboundChannelAdapterModelElement) {
			part = new BadgedIntegrationPart((InboundChannelAdapterModelElement) model,
					IntegrationImages.INBOUND_ADAPTER, IntegrationImages.BADGE_SI_TWITTER);
		}
		else if (model instanceof MentionsInboundChannelAdapterModelElement) {
			part = new BadgedIntegrationPart((MentionsInboundChannelAdapterModelElement) model,
					IntegrationImages.INBOUND_ADAPTER, IntegrationImages.BADGE_SI_TWITTER);
		}
		else if (model instanceof OutboundChannelAdapterModelElement) {
			part = new BadgedIntegrationPart((OutboundChannelAdapterModelElement) model,
					IntegrationImages.OUTBOUND_ADAPTER, IntegrationImages.BADGE_SI_TWITTER);
		}
		else if (model instanceof SearchInboundChannelAdapterModelElement) {
			part = new BadgedIntegrationPart((SearchInboundChannelAdapterModelElement) model,
					IntegrationImages.INBOUND_ADAPTER, IntegrationImages.BADGE_SI_TWITTER);
		}
		else if (model instanceof SearchOutboundGatewayModelElement) {
			part = new BadgedIntegrationPart((SearchOutboundGatewayModelElement) model,
					IntegrationImages.INBOUND_GATEWAY, IntegrationImages.BADGE_SI_TWITTER);
		}
		return part;
	}

}
