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
package org.springframework.ide.eclipse.config.ui.editors.integration.jmx.graph.model;

import java.util.List;

import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
import org.springframework.ide.eclipse.config.core.schemas.IntJmxSchemaConstants;
import org.springframework.ide.eclipse.config.graph.model.Activity;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.model.AbstractIntegrationModelFactory;


/**
 * @author Leo Dos Santos
 */
@SuppressWarnings("restriction")
public class IntJmxModelFactory extends AbstractIntegrationModelFactory {

	public void getChildrenFromXml(List<Activity> list, IDOMElement input, Activity parent) {
		if (input.getLocalName().equals(IntJmxSchemaConstants.ELEM_ATTRIBUTE_POLLING_CHANNEL_ADAPTER)) {
			AttributePollingChannelAdapterModelElement adapter = new AttributePollingChannelAdapterModelElement(input,
					parent.getDiagram());
			list.add(adapter);
		}
		else if (input.getLocalName().equals(IntJmxSchemaConstants.ELEM_NOTIFICATION_LISTENING_CHANNEL_ADAPTER)) {
			NotificationListeningChannelAdapterModelElement adapter = new NotificationListeningChannelAdapterModelElement(
					input, parent.getDiagram());
			list.add(adapter);
		}
		else if (input.getLocalName().equals(IntJmxSchemaConstants.ELEM_NOTIFICATION_PUBLISHING_CHANNEL_ADAPTER)) {
			NotificationPublishingChannelAdapterModelElement adapter = new NotificationPublishingChannelAdapterModelElement(
					input, parent.getDiagram());
			list.add(adapter);
		}
		else if (input.getLocalName().equals(IntJmxSchemaConstants.ELEM_OPERATION_INVOKING_CHANNEL_ADAPTER)) {
			OperationInvokingChannelAdapterModelElement adapter = new OperationInvokingChannelAdapterModelElement(
					input, parent.getDiagram());
			list.add(adapter);
		}
		else if (input.getLocalName().equals(IntJmxSchemaConstants.ELEM_OPERATION_INVOKING_OUTBOUND_GATEWAY)) {
			OperationInvokingOutboundGatewayModelElement gateway = new OperationInvokingOutboundGatewayModelElement(
					input, parent.getDiagram());
			list.add(gateway);
		}
	}

}
