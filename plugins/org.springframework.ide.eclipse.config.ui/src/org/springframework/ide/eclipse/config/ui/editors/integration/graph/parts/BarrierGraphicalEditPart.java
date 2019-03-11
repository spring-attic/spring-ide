/*******************************************************************************
 * Copyright (c) 2015 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.config.ui.editors.integration.graph.parts;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.IntegrationImages;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.model.BarrierModelElement;

/**
 * Editpart for the Thread Barrier element
 *
 * @author Alex Boyko
 *
 */
public class BarrierGraphicalEditPart extends BorderedIntegrationPart {

	public BarrierGraphicalEditPart(BarrierModelElement bridge) {
		super(bridge);
	}

	@Override
	protected IFigure createFigure() {
		Label l = (Label) super.createFigure();
		l.setIcon(IntegrationImages.getImageWithBadge(IntegrationImages.BARRIER, IntegrationImages.BADGE_SI));
		return l;
	}

	@Override
	public BarrierModelElement getModelElement() {
		return (BarrierModelElement) getModel();
	}

}
