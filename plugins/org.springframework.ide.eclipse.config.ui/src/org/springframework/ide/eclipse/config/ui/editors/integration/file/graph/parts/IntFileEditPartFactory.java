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
package org.springframework.ide.eclipse.config.ui.editors.integration.file.graph.parts;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;
import org.springframework.ide.eclipse.config.ui.editors.integration.file.graph.model.FileToBytesTransformerModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.file.graph.model.FileToStringTransformerModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.file.graph.model.InboundChannelAdapterModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.file.graph.model.OutboundChannelAdapterModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.file.graph.model.OutboundGatewayModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.file.graph.model.TailInboundChannelAdapterModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.IntegrationImages;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.parts.BadgedIntegrationPart;

/**
 * @author Leo Dos Santos
 */
public class IntFileEditPartFactory implements EditPartFactory {

	public EditPart createEditPart(EditPart context, Object model) {
		EditPart part = null;
		if (model instanceof FileToBytesTransformerModelElement) {
			part = new BadgedIntegrationPart((FileToBytesTransformerModelElement) model, IntegrationImages.TRANSFORMER,
					IntegrationImages.BADGE_SI_FILE);
		}
		else if (model instanceof FileToStringTransformerModelElement) {
			part = new BadgedIntegrationPart((FileToStringTransformerModelElement) model,
					IntegrationImages.TRANSFORMER, IntegrationImages.BADGE_SI_FILE);
		}
		else if (model instanceof InboundChannelAdapterModelElement) {
			part = new BadgedIntegrationPart((InboundChannelAdapterModelElement) model,
					IntegrationImages.INBOUND_ADAPTER, IntegrationImages.BADGE_SI_FILE);
		}
		else if (model instanceof TailInboundChannelAdapterModelElement) {
			part = new BadgedIntegrationPart((TailInboundChannelAdapterModelElement) model,
					IntegrationImages.INBOUND_ADAPTER, IntegrationImages.BADGE_SI_FILE);
		}
		else if (model instanceof OutboundChannelAdapterModelElement) {
			part = new BadgedIntegrationPart((OutboundChannelAdapterModelElement) model,
					IntegrationImages.OUTBOUND_ADAPTER, IntegrationImages.BADGE_SI_FILE);
		}
		else if (model instanceof OutboundGatewayModelElement) {
			part = new BadgedIntegrationPart((OutboundGatewayModelElement) model, IntegrationImages.OUTBOUND_GATEWAY,
					IntegrationImages.BADGE_SI_FILE);
		}
		return part;
	}

}
