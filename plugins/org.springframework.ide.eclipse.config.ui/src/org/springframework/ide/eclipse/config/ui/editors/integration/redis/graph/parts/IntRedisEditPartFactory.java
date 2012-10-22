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
package org.springframework.ide.eclipse.config.ui.editors.integration.redis.graph.parts;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;
import org.springframework.ide.eclipse.config.ui.editors.integration.redis.graph.model.InboundChannelAdapterModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.redis.graph.model.OutboundChannelAdapterModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.redis.graph.model.PublishSubscribeChannelModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.redis.graph.model.StoreInboundChannelAdapterModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.redis.graph.model.StoreOutboundChannelAdapterModelElement;

/**
 * @author Leo Dos Santos
 */
public class IntRedisEditPartFactory implements EditPartFactory {

	public EditPart createEditPart(EditPart context, Object model) {
		EditPart part = null;
		if (model instanceof InboundChannelAdapterModelElement) {
			part = new InboundChannelAdapterGraphicalEditPart((InboundChannelAdapterModelElement) model);
		}
		else if (model instanceof OutboundChannelAdapterModelElement) {
			part = new OutboundChannelAdapterGraphicalEditPart((OutboundChannelAdapterModelElement) model);
		}
		else if (model instanceof PublishSubscribeChannelModelElement) {
			part = new PublishSubscribeChannelGraphicalEditPart((PublishSubscribeChannelModelElement) model);
		}
		else if (model instanceof StoreInboundChannelAdapterModelElement) {
			part = new StoreInboundChannelAdapterGraphicalEditPart((StoreInboundChannelAdapterModelElement) model);
		}
		else if (model instanceof StoreOutboundChannelAdapterModelElement) {
			part = new StoreOutboundChannelAdapterGraphicalEditPart((StoreOutboundChannelAdapterModelElement) model);
		}
		return part;
	}

}
