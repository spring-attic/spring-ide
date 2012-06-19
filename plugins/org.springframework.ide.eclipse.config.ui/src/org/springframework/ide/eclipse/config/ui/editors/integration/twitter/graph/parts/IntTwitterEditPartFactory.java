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
package org.springframework.ide.eclipse.config.ui.editors.integration.twitter.graph.parts;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;
import org.springframework.ide.eclipse.config.ui.editors.integration.twitter.graph.model.DmInboundChannelAdapterModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.twitter.graph.model.DmOutboundChannelAdapterModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.twitter.graph.model.InboundChannelAdapterModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.twitter.graph.model.MentionsInboundChannelAdapterModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.twitter.graph.model.OutboundChannelAdapterModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.twitter.graph.model.SearchInboundChannelAdapterModelElement;


/**
 * @author Leo Dos Santos
 */
public class IntTwitterEditPartFactory implements EditPartFactory {

	public EditPart createEditPart(EditPart context, Object model) {
		EditPart part = null;
		if (model instanceof DmInboundChannelAdapterModelElement) {
			part = new DmInboundChannelAdapterGraphicalEditPart((DmInboundChannelAdapterModelElement) model);
		}
		else if (model instanceof DmOutboundChannelAdapterModelElement) {
			part = new DmOutboundChannelAdapterGraphicalEditPart((DmOutboundChannelAdapterModelElement) model);
		}
		else if (model instanceof InboundChannelAdapterModelElement) {
			part = new InboundChannelAdapterGraphicalEditPart((InboundChannelAdapterModelElement) model);
		}
		else if (model instanceof MentionsInboundChannelAdapterModelElement) {
			part = new MentionsInboundChannelAdapterGraphicalEditPart((MentionsInboundChannelAdapterModelElement) model);
		}
		else if (model instanceof OutboundChannelAdapterModelElement) {
			part = new OutboundChannelAdapterGraphicalEditPart((OutboundChannelAdapterModelElement) model);
		}
		else if (model instanceof SearchInboundChannelAdapterModelElement) {
			part = new SearchInboundChannelAdapterGraphicalEditPart((SearchInboundChannelAdapterModelElement) model);
		}
		return part;
	}

}
