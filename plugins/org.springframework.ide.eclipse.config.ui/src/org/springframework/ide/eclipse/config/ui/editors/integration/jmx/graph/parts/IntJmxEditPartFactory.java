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
package org.springframework.ide.eclipse.config.ui.editors.integration.jmx.graph.parts;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.IntegrationImages;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.parts.BadgedIntegrationPart;
import org.springframework.ide.eclipse.config.ui.editors.integration.jmx.graph.model.AttributePollingChannelAdapterModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.jmx.graph.model.NotificationListeningChannelAdapterModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.jmx.graph.model.NotificationPublishingChannelAdapterModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.jmx.graph.model.OperationInvokingChannelAdapterModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.jmx.graph.model.OperationInvokingOutboundGatewayModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.jmx.graph.model.TreePollingChannelAdapterModelElement;

/**
 * @author Leo Dos Santos
 */
public class IntJmxEditPartFactory implements EditPartFactory {

	public EditPart createEditPart(EditPart context, Object model) {
		EditPart part = null;
		if (model instanceof AttributePollingChannelAdapterModelElement) {
			part = new BadgedIntegrationPart((AttributePollingChannelAdapterModelElement) model,
					IntegrationImages.INBOUND_ADAPTER, IntegrationImages.BADGE_SI_JMX);
		}
		else if (model instanceof TreePollingChannelAdapterModelElement) {
			part = new BadgedIntegrationPart((TreePollingChannelAdapterModelElement) model,
					IntegrationImages.INBOUND_ADAPTER, IntegrationImages.BADGE_SI_JMX);
		}
		else if (model instanceof NotificationListeningChannelAdapterModelElement) {
			part = new BadgedIntegrationPart((NotificationListeningChannelAdapterModelElement) model,
					IntegrationImages.INBOUND_ADAPTER, IntegrationImages.BADGE_SI_JMX);
		}
		else if (model instanceof NotificationPublishingChannelAdapterModelElement) {
			part = new BadgedIntegrationPart((NotificationPublishingChannelAdapterModelElement) model,
					IntegrationImages.OUTBOUND_ADAPTER, IntegrationImages.BADGE_SI_JMX);
		}
		else if (model instanceof OperationInvokingChannelAdapterModelElement) {
			part = new BadgedIntegrationPart((OperationInvokingChannelAdapterModelElement) model,
					IntegrationImages.OUTBOUND_ADAPTER, IntegrationImages.BADGE_SI_JMX);
		}
		else if (model instanceof OperationInvokingOutboundGatewayModelElement) {
			part = new BadgedIntegrationPart((OperationInvokingOutboundGatewayModelElement) model,
					IntegrationImages.OUTBOUND_GATEWAY, IntegrationImages.BADGE_SI_JMX);
		}
		return part;
	}

}
