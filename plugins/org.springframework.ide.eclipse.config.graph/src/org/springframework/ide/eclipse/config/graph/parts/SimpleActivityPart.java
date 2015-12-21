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

import java.util.Map;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.graph.CompoundDirectedGraph;
import org.eclipse.draw2d.graph.Node;
import org.eclipse.draw2d.graph.Subgraph;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.mylyn.commons.ui.CommonImages;
import org.springframework.ide.eclipse.config.graph.ConfigGraphCommonImages;
import org.springframework.ide.eclipse.config.graph.figures.SimpleActivityLabel;
import org.springframework.ide.eclipse.config.graph.model.Activity;


/**
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
public abstract class SimpleActivityPart extends ActivityPart {

	public SimpleActivityPart(Activity activity) {
		super(activity);
	}

	@Override
	protected void contributeNodesToGraph(CompoundDirectedGraph graph, Subgraph s,
			Map<AbstractGraphicalEditPart, Object> map) {
		Node n = new Node(this, s);
		n.setRowConstraint(getActivity().getSortIndex());
		n.outgoingOffset = getAnchorOffset();
		n.incomingOffset = getAnchorOffset();
		Dimension hint = getFigureHint();
		n.width = hint.width;
		n.height = hint.height;
		if (graph.getDirection() == PositionConstants.EAST) {
			n.setPadding(new Insets(8, 10, 12, 10));
		}
		else {
			n.setPadding(new Insets(10, 8, 10, 12));
		}
		map.put(this, n);
		graph.nodes.add(n);

		for (int i = 0; i < getSourceConnections().size(); i++) {
			TransitionPart trans = (TransitionPart) getSourceConnections().get(i);
			Subgraph sub = (Subgraph) map.get(getViewer().getContents());
			trans.contributeNodesToGraph(graph, s, map);
		}
	}

	/**
	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#createFigure()
	 */
	@Override
	protected IFigure createFigure() {
		int direction = PositionConstants.SOUTH;
		EditPart part = getViewer().getContents();
		if (part instanceof ActivityDiagramPart) {
			ActivityDiagramPart diagramPart = (ActivityDiagramPart) part;
			direction = diagramPart.getDirection();
		}
		Label l = new SimpleActivityLabel(direction);
		l.setIcon(CommonImages.getImage(ConfigGraphCommonImages.ACTIVITY));
		return l;
	}

	@Override
	protected int getAnchorOffset() {
		EditPart part = getViewer().getContents();
		if (part instanceof ActivityDiagramPart) {
			ActivityDiagramPart diagramPart = (ActivityDiagramPart) part;
			IFigure figure = getFigure();
			int direction = diagramPart.getDirection();
			if (direction != PositionConstants.EAST && figure instanceof Label) {
				return ((Label) figure).getIconBounds().width / 2;
			}
		}
		return -1;
	}

	protected Dimension getFigureHint() {
		return getFigure().getPreferredSize();
	}

	@Override
	protected void performDirectEdit() {
		if (manager == null) {
			Label l = (Label) getFigure();
			manager = new ActivityDirectEditManager(this, TextCellEditor.class, new ActivityCellEditorLocator(l), l);
		}
		manager.show();
	}

	@Override
	protected void refreshFigureVisuals() {
		((Label) getFigure()).setText(getActivity().getName());
	}

}
