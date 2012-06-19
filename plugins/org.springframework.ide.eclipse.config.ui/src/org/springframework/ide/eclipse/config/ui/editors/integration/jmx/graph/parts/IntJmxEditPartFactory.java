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
package org.springframework.ide.eclipse.config.ui.editors.integration.jmx.graph.parts;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;
import org.springframework.ide.eclipse.config.ui.editors.integration.jmx.graph.model.AttributePollingChannelAdapterModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.jmx.graph.model.NotificationListeningChannelAdapterModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.jmx.graph.model.NotificationPublishingChannelAdapterModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.jmx.graph.model.OperationInvokingChannelAdapterModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.jmx.graph.model.OperationInvokingOutboundGatewayModelElement;


/**
 * @author Leo Dos Santos
 */
public class IntJmxEditPartFactory implements EditPartFactory {

	public EditPart createEditPart(EditPart context, Object model) {
		EditPart part = null;
		if (model instanceof AttributePollingChannelAdapterModelElement) {
			part = new AttributePollingChannelAdapterGraphicalEditPart(
					(AttributePollingChannelAdapterModelElement) model);
		}
		else if (model instanceof NotificationListeningChannelAdapterModelElement) {
			part = new NotificationListeningChannelAdapterGraphicalEditPart(
					(NotificationListeningChannelAdapterModelElement) model);
		}
		else if (model instanceof NotificationPublishingChannelAdapterModelElement) {
			part = new NotificationPublishingChannelAdapterGraphicalEditPart(
					(NotificationPublishingChannelAdapterModelElement) model);
		}
		else if (model instanceof OperationInvokingChannelAdapterModelElement) {
			part = new OperationInvokingChannelAdapterGraphicalEditPart(
					(OperationInvokingChannelAdapterModelElement) model);
		}
		else if (model instanceof OperationInvokingOutboundGatewayModelElement) {
			part = new OperationInvokingOutboundGatewayGraphicalEditPart(
					(OperationInvokingOutboundGatewayModelElement) model);
		}
		return part;
	}

}
