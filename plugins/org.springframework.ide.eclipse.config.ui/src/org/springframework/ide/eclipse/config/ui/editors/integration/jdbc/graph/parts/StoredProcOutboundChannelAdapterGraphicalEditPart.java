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

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.IntegrationImages;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.parts.BorderedIntegrationPart;
import org.springframework.ide.eclipse.config.ui.editors.integration.jdbc.graph.model.StoredProcOutboundChannelAdapterModelElement;


/**
 * @author Leo Dos Santos
 */
public class StoredProcOutboundChannelAdapterGraphicalEditPart extends BorderedIntegrationPart {

	public StoredProcOutboundChannelAdapterGraphicalEditPart(StoredProcOutboundChannelAdapterModelElement adapter) {
		super(adapter);
	}

	@Override
	protected IFigure createFigure() {
		Label l = (Label) super.createFigure();
		l.setIcon(IntegrationImages.getImageWithBadge(IntegrationImages.OUTBOUND_ADAPTER,
				IntegrationImages.BADGE_SI_JDBC));
		return l;
	}

	@Override
	public StoredProcOutboundChannelAdapterModelElement getModelElement() {
		return (StoredProcOutboundChannelAdapterModelElement) getModel();
	}

}
