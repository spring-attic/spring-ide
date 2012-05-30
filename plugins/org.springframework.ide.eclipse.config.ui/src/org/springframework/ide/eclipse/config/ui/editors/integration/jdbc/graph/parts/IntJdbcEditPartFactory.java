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
package org.springframework.ide.eclipse.config.ui.editors.integration.jdbc.graph.parts;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;
import org.springframework.ide.eclipse.config.ui.editors.integration.jdbc.graph.model.InboundChannelAdapterModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.jdbc.graph.model.OutboundChannelAdapterModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.jdbc.graph.model.OutboundGatewayModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.jdbc.graph.model.StoredProcInboundChannelAdapterModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.jdbc.graph.model.StoredProcOutboundChannelAdapterModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.jdbc.graph.model.StoredProcOutboundGatewayModelElement;


/**
 * @author Leo Dos Santos
 */
public class IntJdbcEditPartFactory implements EditPartFactory {

	public EditPart createEditPart(EditPart context, Object model) {
		EditPart part = null;
		if (model instanceof InboundChannelAdapterModelElement) {
			part = new InboundChannelAdapterGraphicalEditPart((InboundChannelAdapterModelElement) model);
		}
		else if (model instanceof OutboundChannelAdapterModelElement) {
			part = new OutboundChannelAdapterGraphicalEditPart((OutboundChannelAdapterModelElement) model);
		}
		else if (model instanceof OutboundGatewayModelElement) {
			part = new OutboundGatewayGraphicalEditPart((OutboundGatewayModelElement) model);
		}
		else if (model instanceof StoredProcInboundChannelAdapterModelElement) {
			part = new StoredProcInboundChannelAdapterGraphicalEditPart(
					(StoredProcInboundChannelAdapterModelElement) model);
		}
		else if (model instanceof StoredProcOutboundChannelAdapterModelElement) {
			part = new StoredProcOutboundChannelAdapterGraphicalEditPart(
					(StoredProcOutboundChannelAdapterModelElement) model);
		}
		else if (model instanceof StoredProcOutboundGatewayModelElement) {
			part = new StoredProcOutboundGatewayGraphicalEditPart((StoredProcOutboundGatewayModelElement) model);
		}
		return part;
	}

}
