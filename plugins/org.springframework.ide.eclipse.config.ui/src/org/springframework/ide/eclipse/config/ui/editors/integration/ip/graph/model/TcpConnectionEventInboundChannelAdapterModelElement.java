/*******************************************************************************
 * Copyright (c) 2014 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.config.ui.editors.integration.ip.graph.model;

import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
import org.springframework.ide.eclipse.config.core.schemas.IntIpSchemaConstants;
import org.springframework.ide.eclipse.config.graph.model.AbstractConfigGraphDiagram;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.model.AbstractInboundChannelAdapterModelElement;

@SuppressWarnings("restriction")
public class TcpConnectionEventInboundChannelAdapterModelElement extends AbstractInboundChannelAdapterModelElement {

	public TcpConnectionEventInboundChannelAdapterModelElement() {
		super();
	}

	public TcpConnectionEventInboundChannelAdapterModelElement(IDOMElement input, AbstractConfigGraphDiagram diagram) {
		super(input, diagram);
	}

	@Override
	public String getInputName() {
		return IntIpSchemaConstants.ELEM_TCP_CONNECTION_EVENT_INBOUND_CHANNEL_ADAPTER;
	}

}
