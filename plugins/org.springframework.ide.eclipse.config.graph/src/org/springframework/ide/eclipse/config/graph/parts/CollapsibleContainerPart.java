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
package org.springframework.ide.eclipse.config.graph.parts;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.ActionEvent;
import org.eclipse.draw2d.ActionListener;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.gef.EditPart;
import org.springframework.ide.eclipse.config.graph.figures.CollapsibleContainerFigure;
import org.springframework.ide.eclipse.config.graph.model.Activity;
import org.springframework.ide.eclipse.config.graph.model.ParallelActivity;


/**
 * @author Leo Dos Santos
 */
public abstract class CollapsibleContainerPart extends ParallelActivityPart implements ActionListener {

	public CollapsibleContainerPart(ParallelActivity activity) {
		super(activity);
	}

	public void actionPerformed(ActionEvent event) {
		refreshAll();
	}

	@Override
	protected IFigure createFigure() {
		int direction = PositionConstants.SOUTH;
		EditPart part = getViewer().getContents();
		if (part instanceof ActivityDiagramPart) {
			ActivityDiagramPart diagramPart = (ActivityDiagramPart) part;
			direction = diagramPart.getDirection();
		}
		CollapsibleContainerFigure figure = new CollapsibleContainerFigure(direction);
		figure.addActionListener(this);
		return figure;
	}

	@Override
	protected List<Activity> getModelChildren() {
		CollapsibleContainerFigure figure = (CollapsibleContainerFigure) getFigure();
		if (figure.isExpanded()) {
			return super.getModelChildren();
		}
		else {
			return new ArrayList<Activity>();
		}
	}

}
