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
package org.springframework.ide.eclipse.config.ui.editors.integration.xmpp.graph.parts;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;
import org.springframework.ide.eclipse.config.ui.editors.integration.xmpp.graph.model.HeaderEnricherModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.xmpp.graph.model.InboundChannelAdapterModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.xmpp.graph.model.OutboundChannelAdapterModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.xmpp.graph.model.PresenceInboundChannelAdapterModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.xmpp.graph.model.PresenceOutboundChannelAdapterModelElement;


/**
 * @author Leo Dos Santos
 */
public class IntXmppEditPartFactory implements EditPartFactory {

	public EditPart createEditPart(EditPart context, Object model) {
		EditPart part = null;
		if (model instanceof HeaderEnricherModelElement) {
			part = new HeaderEnricherGraphicalEditPart((HeaderEnricherModelElement) model);
		}
		else if (model instanceof InboundChannelAdapterModelElement) {
			part = new InboundChannelAdapterGraphicalEditPart((InboundChannelAdapterModelElement) model);
		}
		else if (model instanceof OutboundChannelAdapterModelElement) {
			part = new OutboundChannelAdapterGraphicalEditPart((OutboundChannelAdapterModelElement) model);
		}
		else if (model instanceof PresenceInboundChannelAdapterModelElement) {
			part = new PresenceInboundChannelAdapterGraphicalEditPart(
					(PresenceInboundChannelAdapterModelElement) model);
		}
		else if (model instanceof PresenceOutboundChannelAdapterModelElement) {
			part = new PresenceOutboundChannelAdapterGraphicalEditPart(
					(PresenceOutboundChannelAdapterModelElement) model);
		}
		return part;
	}

}
