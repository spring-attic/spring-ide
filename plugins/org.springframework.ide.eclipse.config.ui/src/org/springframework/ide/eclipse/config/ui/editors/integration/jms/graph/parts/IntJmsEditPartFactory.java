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
package org.springframework.ide.eclipse.config.ui.editors.integration.jms.graph.parts;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;
import org.springframework.ide.eclipse.config.ui.editors.integration.jms.graph.model.ChannelModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.jms.graph.model.HeaderEnricherModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.jms.graph.model.InboundChannelAdapterModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.jms.graph.model.InboundGatewayModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.jms.graph.model.MessageDrivenChannelAdapterModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.jms.graph.model.OutboundChannelAdapterModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.jms.graph.model.OutboundGatewayModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.jms.graph.model.PublishSubscribeChannelModelElement;


/**
 * @author Leo Dos Santos
 */
public class IntJmsEditPartFactory implements EditPartFactory {

	public EditPart createEditPart(EditPart context, Object model) {
		EditPart part = null;
		if (model instanceof ChannelModelElement) {
			part = new ChannelGraphicalEditPart((ChannelModelElement) model);
		}
		else if (model instanceof HeaderEnricherModelElement) {
			part = new HeaderEnricherGraphicalEditPart((HeaderEnricherModelElement) model);
		}
		else if (model instanceof InboundChannelAdapterModelElement) {
			part = new InboundChannelAdapterGraphicalEditPart((InboundChannelAdapterModelElement) model);
		}
		else if (model instanceof InboundGatewayModelElement) {
			part = new InboundGatewayGraphicalEditPart((InboundGatewayModelElement) model);
		}
		else if (model instanceof MessageDrivenChannelAdapterModelElement) {
			part = new MessageDrivenChannelAdapterGraphicalEditPart((MessageDrivenChannelAdapterModelElement) model);
		}
		else if (model instanceof OutboundChannelAdapterModelElement) {
			part = new OutboundChannelAdapterGraphicalEditPart((OutboundChannelAdapterModelElement) model);
		}
		else if (model instanceof OutboundGatewayModelElement) {
			part = new OutboundGatewayGraphicalEditPart((OutboundGatewayModelElement) model);
		}
		else if (model instanceof PublishSubscribeChannelModelElement) {
			part = new PublishSubscribeChannelGraphicalEditPart((PublishSubscribeChannelModelElement) model);
		}
		return part;
	}

}
