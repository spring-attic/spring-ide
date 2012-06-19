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
package org.springframework.ide.eclipse.config.ui.editors.integration.graph.parts;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.IntegrationImages;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.model.ControlBusModelElement;


/**
 * @author Leo Dos Santos
 */
public class ControlBusGraphicalEditPart extends BorderedIntegrationPart {

	public ControlBusGraphicalEditPart(ControlBusModelElement bus) {
		super(bus);
	}

	@Override
	protected IFigure createFigure() {
		Label l = (Label) super.createFigure();
		l.setIcon(IntegrationImages.getImageWithBadge(IntegrationImages.CONTROL_BUS, IntegrationImages.BADGE_SI));
		return l;
	}

	@Override
	public ControlBusModelElement getModelElement() {
		return (ControlBusModelElement) getModel();
	}

}
