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
package org.springframework.ide.eclipse.config.ui.editors.integration.smpp.graph.model;

import java.util.List;

import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
import org.springframework.ide.eclipse.config.core.schemas.IntSmppSchemaConstants;
import org.springframework.ide.eclipse.config.graph.model.Activity;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.model.AbstractIntegrationModelFactory;

@SuppressWarnings("restriction")
public class IntSmppModelFactory extends AbstractIntegrationModelFactory {

	public void getChildrenFromXml(List<Activity> list, IDOMElement input, Activity parent) {
		if (input.getLocalName().equals(IntSmppSchemaConstants.ELEM_INBOUND_CHANNEL_ADAPTER)) {
			InboundChannelAdapterModelElement adapter = new InboundChannelAdapterModelElement(input,
					parent.getDiagram());
			list.add(adapter);
		}
		else if (input.getLocalName().equals(IntSmppSchemaConstants.ELEM_OUTBOUND_CHANNEL_ADAPTER)) {
			OutboundChannelAdapterModelElement adapter = new OutboundChannelAdapterModelElement(input,
					parent.getDiagram());
			list.add(adapter);
		}
		else if (input.getLocalName().equals(IntSmppSchemaConstants.ELEM_INBOUND_GATEWAY)) {
			InboundGatewayModelElement adapter = new InboundGatewayModelElement(input, parent.getDiagram());
			list.add(adapter);
		}
		else if (input.getLocalName().equals(IntSmppSchemaConstants.ELEM_OUTBOUND_GATEWAY)) {
			OutboundGatewayModelElement adapter = new OutboundGatewayModelElement(input, parent.getDiagram());
			list.add(adapter);
		}
	}

}
