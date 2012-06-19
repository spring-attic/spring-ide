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

import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.gef.EditPolicy;
import org.springframework.ide.eclipse.config.graph.model.Activity;
import org.springframework.ide.eclipse.config.graph.parts.BorderedActivityPart;
import org.springframework.ide.eclipse.config.graph.policies.ActivityNodeEditPolicy;
import org.springframework.ide.eclipse.config.graph.policies.FixedConnectionNodeEditPolicy;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.ChannelDirectEditPolicy;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.FixedConnectionChannelCreatePolicy;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.LabelHighlightEditPolicy;


/**
 * @author Leo Dos Santos
 */
public abstract class BorderedIntegrationPart extends BorderedActivityPart {

	public BorderedIntegrationPart(Activity activity) {
		super(activity);
	}

	@Override
	protected void createEditPolicies() {
		super.createEditPolicies();
		installEditPolicy(EditPolicy.SELECTION_FEEDBACK_ROLE, new LabelHighlightEditPolicy());
		installEditPolicy(EditPolicy.DIRECT_EDIT_ROLE, new ChannelDirectEditPolicy());
	}

	@Override
	protected Dimension getFigureHint() {
		return getFigure().getPreferredSize(figure.getIcon().getBounds().width, figure.getIcon().getBounds().height);
	}

	@Override
	protected FixedConnectionNodeEditPolicy getFixedConnectionNodeEditPolicy() {
		return new FixedConnectionChannelCreatePolicy();
	}

	@Override
	protected ActivityNodeEditPolicy getStandardConnectionNodeEditPolicy() {
		return null;
	}

	@Override
	protected void performDirectEdit() {
		super.performDirectEdit();
		manager.getCellEditor().setValue(getModelElement().getName());
	}

	@Override
	protected void refreshFigureVisuals() {
		((Label) getFigure()).setText(getModelElement().getDisplayLabel());
	}

}
