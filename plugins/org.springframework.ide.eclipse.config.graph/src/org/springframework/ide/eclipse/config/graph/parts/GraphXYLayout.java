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

import org.eclipse.draw2d.FreeformLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;

/**
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
public class GraphXYLayout extends FreeformLayout {

	private final ActivityDiagramPart diagram;

	private final GraphLayoutManager autoLayout;

	GraphXYLayout(ActivityDiagramPart diagram, int direction) {
		this.diagram = diagram;
		autoLayout = new GraphLayoutManager(diagram, direction);
	}

	@Override
	public Object getConstraint(IFigure figure) {
		Object constraint = constraints.get(figure);
		if (constraint instanceof Rectangle) {
			Rectangle constraintRect = (Rectangle) constraint;
			Rectangle bounds = figure.getBounds();
			return new Rectangle(constraintRect.x, constraintRect.y, bounds.width, bounds.height);
		}
		else {
			return figure.getBounds();
		}
	}

	@Override
	public void layout(IFigure parent) {
		autoLayout.layout(parent);
		super.layout(parent);
		diagram.setBoundsOnModel();
	}
}
