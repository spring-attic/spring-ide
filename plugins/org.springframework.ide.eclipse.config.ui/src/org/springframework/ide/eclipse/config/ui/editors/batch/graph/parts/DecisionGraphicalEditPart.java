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
import org.eclipse.mylyn.commons.ui.CommonImages;
import org.springframework.ide.eclipse.config.graph.parts.SimpleActivityWithContainerPart;
import org.springframework.ide.eclipse.config.ui.editors.batch.graph.BatchImages;
import org.springframework.ide.eclipse.config.ui.editors.batch.graph.model.DecisionModelElement;


/**
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
public class DecisionGraphicalEditPart extends SimpleActivityWithContainerPart {

	public DecisionGraphicalEditPart(DecisionModelElement decision) {
		super(decision);
	}

	@Override
	protected IFigure createFigure() {
		Label l = (Label) super.createFigure();
		l.setIcon(CommonImages.getImage(BatchImages.DECISION));
		return l;
	}

	@Override
	public DecisionModelElement getModelElement() {
		return (DecisionModelElement) getModel();
	}

}
