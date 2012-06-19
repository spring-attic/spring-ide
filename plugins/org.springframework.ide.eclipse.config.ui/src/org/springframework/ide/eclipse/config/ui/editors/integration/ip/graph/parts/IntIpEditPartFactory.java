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
package org.springframework.ide.eclipse.config.ui.editors.integration.ip.graph.parts;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;
import org.springframework.ide.eclipse.config.ui.editors.integration.ip.graph.model.TcpInboundChannelAdapterModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.ip.graph.model.TcpInboundGatewayModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.ip.graph.model.TcpOutboundChannelAdapterModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.ip.graph.model.TcpOutboundGatewayModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.ip.graph.model.UdpInboundChannelAdapterModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.ip.graph.model.UdpOutboundChannelAdapterModelElement;


/**
 * @author Leo Dos Santos
 */
public class IntIpEditPartFactory implements EditPartFactory {

	public EditPart createEditPart(EditPart context, Object model) {
		EditPart part = null;
		if (model instanceof UdpInboundChannelAdapterModelElement) {
			part = new UdpInboundChannelAdapterGraphicalEditPart((UdpInboundChannelAdapterModelElement) model);
		}
		else if (model instanceof TcpInboundGatewayModelElement) {
			part = new TcpInboundGatewayGraphicalEditPart((TcpInboundGatewayModelElement) model);
		}
		else if (model instanceof UdpOutboundChannelAdapterModelElement) {
			part = new UdpOutboundChannelAdapterGraphicalEditPart((UdpOutboundChannelAdapterModelElement) model);
		}
		else if (model instanceof TcpOutboundGatewayModelElement) {
			part = new TcpOutboundGatewayGraphicalEditPart((TcpOutboundGatewayModelElement) model);
		}
		else if (model instanceof TcpInboundChannelAdapterModelElement) {
			part = new TcpInboundChannelAdapterGraphicalEditPart((TcpInboundChannelAdapterModelElement) model);
		}
		else if (model instanceof TcpOutboundChannelAdapterModelElement) {
			part = new TcpOutboundChannelAdapterGraphicalEditPart((TcpOutboundChannelAdapterModelElement) model);
		}
		return part;
	}

}
