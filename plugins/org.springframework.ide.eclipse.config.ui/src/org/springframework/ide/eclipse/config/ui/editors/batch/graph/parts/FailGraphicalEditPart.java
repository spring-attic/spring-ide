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
package org.springframework.ide.eclipse.config.ui.editors.batch.graph.parts;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.gef.EditPolicy;
import org.eclipse.mylyn.commons.ui.CommonImages;
import org.springframework.ide.eclipse.config.graph.parts.SimpleActivityPart;
import org.springframework.ide.eclipse.config.ui.editors.batch.graph.BatchImages;
import org.springframework.ide.eclipse.config.ui.editors.batch.graph.model.FailModelElement;


/**
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
public class FailGraphicalEditPart extends SimpleActivityPart {

	public FailGraphicalEditPart(FailModelElement fail) {
		super(fail);
	}

	@Override
	protected void createEditPolicies() {
		super.createEditPolicies();
		installEditPolicy(EditPolicy.DIRECT_EDIT_ROLE, null);
	}

	@Override
	protected IFigure createFigure() {
		Label l = (Label) super.createFigure();
		l.setIcon(CommonImages.getImage(BatchImages.FAIL));
		return l;
	}

	@Override
	public FailModelElement getModelElement() {
		return (FailModelElement) getModel();
	}

}
