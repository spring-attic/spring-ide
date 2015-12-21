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

import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.graph.CompoundDirectedGraph;
import org.eclipse.draw2d.graph.Edge;
import org.eclipse.draw2d.graph.Node;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.springframework.ide.eclipse.config.graph.figures.SequentialActivityFigure;
import org.springframework.ide.eclipse.config.graph.model.SequentialActivity;


/**
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
public abstract class SequentialActivityPart extends StructuredActivityPart {

	public SequentialActivityPart(SequentialActivity activity) {
		super(activity);
	}

	/**
	 * @see ActivityPart#contributeEdgesToGraph(org.eclipse.draw2d.graph.CompoundDirectedGraph,
	 * java.util.Map)
	 */
	@Override
	protected void contributeEdgesToGraph(CompoundDirectedGraph graph, Map<AbstractGraphicalEditPart, Object> map) {
		super.contributeEdgesToGraph(graph, map);
		Node node, prev = null;
		EditPart a;
		List members = getChildren();
		for (int n = 0; n < members.size(); n++) {
			a = (EditPart) members.get(n);
			node = (Node) map.get(a);
			if (prev != null) {
				Edge e = new Edge(prev, node);
				graph.edges.add(e);
			}
			prev = node;
		}
	}

	/**
	 * @see org.springframework.ide.eclipse.config.graph.parts.StructuredActivityPart#createFigure()
	 */
	@Override
	protected IFigure createFigure() {
		int direction = PositionConstants.SOUTH;
		EditPart part = getViewer().getContents();
		if (part instanceof ActivityDiagramPart) {
			ActivityDiagramPart diagramPart = (ActivityDiagramPart) part;
			direction = diagramPart.getDirection();
		}
		return new SequentialActivityFigure(direction);
	}

	/**
	 * @see org.springframework.ide.eclipse.config.graph.parts.StructuredActivityPart#getAnchorOffset()
	 */
	@Override
	protected int getAnchorOffset() {
		return 15;
	}

}
