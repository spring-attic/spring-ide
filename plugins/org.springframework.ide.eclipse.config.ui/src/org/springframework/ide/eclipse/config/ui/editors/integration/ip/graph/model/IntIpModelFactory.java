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
package org.springframework.ide.eclipse.config.ui.editors.integration.ip.graph.model;

import java.util.List;

import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
import org.springframework.ide.eclipse.config.core.schemas.IntIpSchemaConstants;
import org.springframework.ide.eclipse.config.graph.model.Activity;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.model.AbstractIntegrationModelFactory;


/**
 * @author Leo Dos Santos
 */
@SuppressWarnings("restriction")
public class IntIpModelFactory extends AbstractIntegrationModelFactory {

	public void getChildrenFromXml(List<Activity> list, IDOMElement input, Activity parent) {
		if (input.getLocalName().equals(IntIpSchemaConstants.ELEM_UDP_INBOUND_CHANNEL_ADAPTER)) {
			UdpInboundChannelAdapterModelElement adapter = new UdpInboundChannelAdapterModelElement(input,
					parent.getDiagram());
			list.add(adapter);
		}
		else if (input.getLocalName().equals(IntIpSchemaConstants.ELEM_TCP_INBOUND_GATEWAY)) {
			TcpInboundGatewayModelElement gateway = new TcpInboundGatewayModelElement(input, parent.getDiagram());
			list.add(gateway);
		}
		else if (input.getLocalName().equals(IntIpSchemaConstants.ELEM_UDP_OUTBOUND_CHANNEL_ADAPTER)) {
			UdpOutboundChannelAdapterModelElement adapter = new UdpOutboundChannelAdapterModelElement(input,
					parent.getDiagram());
			list.add(adapter);
		}
		else if (input.getLocalName().equals(IntIpSchemaConstants.ELEM_TCP_OUTBOUND_GATEWAY)) {
			TcpOutboundGatewayModelElement gateway = new TcpOutboundGatewayModelElement(input, parent.getDiagram());
			list.add(gateway);
		}
		else if (input.getLocalName().equals(IntIpSchemaConstants.ELEM_TCP_INBOUND_CHANNEL_ADAPTER)) {
			TcpInboundChannelAdapterModelElement adapter = new TcpInboundChannelAdapterModelElement(input,
					parent.getDiagram());
			list.add(adapter);
		}
		else if (input.getLocalName().equals(IntIpSchemaConstants.ELEM_TCP_OUTBOUND_CHANNEL_ADAPTER)) {
			TcpOutboundChannelAdapterModelElement adapter = new TcpOutboundChannelAdapterModelElement(input,
					parent.getDiagram());
			list.add(adapter);
		}
	}

}
