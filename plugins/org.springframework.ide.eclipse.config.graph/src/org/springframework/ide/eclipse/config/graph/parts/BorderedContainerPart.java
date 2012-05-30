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
import java.util.Map;

import org.eclipse.draw2d.ActionEvent;
import org.eclipse.draw2d.ActionListener;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.draw2d.graph.CompoundDirectedGraph;
import org.eclipse.draw2d.graph.Edge;
import org.eclipse.draw2d.graph.Node;
import org.eclipse.draw2d.graph.Subgraph;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.springframework.ide.eclipse.config.graph.figures.BorderedContainerFigure;
import org.springframework.ide.eclipse.config.graph.figures.SubgraphFigure;
import org.springframework.ide.eclipse.config.graph.model.Activity;
import org.springframework.ide.eclipse.config.graph.model.StructuredActivity;


/**
 * Work in progress. Not ready for client use.
 * @author Leo Dos Santos
 */
public abstract class BorderedContainerPart extends StructuredActivityPart implements ActionListener {

	public BorderedContainerPart(StructuredActivity activity) {
		super(activity);
	}

	public void actionPerformed(ActionEvent event) {
		refreshAll();
	}

	@Override
	protected void applyChildrenResults(CompoundDirectedGraph graph, Map<AbstractGraphicalEditPart, Object> map) {
		SubgraphFigure figure = (SubgraphFigure) getFigure();
		int headerY = figure.getHeader().getBounds().getBottom().y + 8;
		int y = 0;
		int x = -1;
		int heightSum = 0;
		for (int i = 0; i < getChildren().size(); i++) {
			ActivityPart part = (ActivityPart) getChildren().get(i);
			part.applyGraphResults(graph, map);
			Point p = part.getFigure().getBounds().getLocation();
			if (x == -1) {
				x = p.x;
			}
			y = headerY + heightSum;
			p.y = y;
			p.x = x;
			part.getFigure().setLocation(p);
			heightSum += part.getFigure().getBounds().height + 5;
		}
		applyMaxWidths();
	}

	protected void applyChildrenResultsToOwn(CompoundDirectedGraph graph, Map<AbstractGraphicalEditPart, Object> map) {
		Node n = (Node) map.get(this);
		int bottom = -1;
		for (int i = 0; i < getChildren().size(); i++) {
			ActivityPart part = (ActivityPart) getChildren().get(i);
			if (part.getFigure().getBounds().bottom() > bottom) {
				bottom = part.getFigure().getBounds().getBottom().y;
			}
		}
		if (bottom > -1) {
			int top = getFigure().getBounds().getTop().y;
			int height = bottom - top + 14;
			getFigure().setBounds(new Rectangle(n.x, n.y, n.width, height));
		}
	}

	@Override
	protected void applyGraphResults(CompoundDirectedGraph graph, Map<AbstractGraphicalEditPart, Object> map) {
		super.applyGraphResults(graph, map);
		applyChildrenResultsToOwn(graph, map);
	}

	protected void applyMaxWidths() {
		int maxWidth = 0;
		for (int i = 0; i < getChildren().size(); i++) {
			ActivityPart part = (ActivityPart) getChildren().get(i);
			Rectangle rect = part.getFigure().getBounds();
			if (rect.width >= maxWidth) {
				maxWidth = rect.width;
			}
		}
		for (int i = 0; i < getChildren().size(); i++) {
			ActivityPart part = (ActivityPart) getChildren().get(i);
			Rectangle rect = part.getFigure().getBounds();
			rect.width = maxWidth;
			part.getFigure().setBounds(rect);
		}
	}

	@Override
	public void contributeEdgesToGraph(CompoundDirectedGraph graph, Map<AbstractGraphicalEditPart, Object> map) {
		List outgoing = getSourceConnections();
		for (int i = 0; i < outgoing.size(); i++) {
			TransitionPart part = (TransitionPart) getSourceConnections().get(i);
			part.contributeToGraph(graph, map);
		}
		for (int i = 0; i < getChildren().size(); i++) {
			ActivityPart part = (ActivityPart) getChildren().get(i);
			if (i + 1 < getChildren().size()) {
				// insert dummy edges
				Edge e = new Edge((Node) map.get(part), (Node) map.get(getChildren().get(i + 1)));
				e.weight = 1;
				graph.edges.add(e);
				// map.put(this.toString() + i, e);
			}
		}
	}

	@Override
	public void contributeNodesToGraph(CompoundDirectedGraph graph, Subgraph s,
			Map<AbstractGraphicalEditPart, Object> map) {
		GraphAnimation.recordInitialState(getContentPane());
		Subgraph me = new Subgraph(this, s);

		me.outgoingOffset = 5;
		me.incomingOffset = 5;
		IFigure fig = getFigure();
		if (fig instanceof SubgraphFigure) {
			me.width = fig.getPreferredSize(me.width, me.height).width;
			me.height = fig.getPreferredSize().height;

			me.insets.top = ((SubgraphFigure) getFigure()).getHeader().getPreferredSize().height + 5;
			me.insets.left = 5;
			me.insets.right = 5;
			me.insets.bottom = 15;
		}
		me.innerPadding = new Insets(0, 0, 6, 4);
		map.put(this, me);
		graph.nodes.add(me);

		for (int i = 0; i < getChildren().size(); i++) {
			ActivityPart activity = (ActivityPart) getChildren().get(i);
			activity.contributeNodesToGraph(graph, me, map);
		}
	}

	@Override
	protected IFigure createFigure() {
		int direction = PositionConstants.SOUTH;
		EditPart part = getViewer().getContents();
		if (part instanceof ActivityDiagramPart) {
			ActivityDiagramPart diagramPart = (ActivityDiagramPart) part;
			direction = diagramPart.getDirection();
		}
		BorderedContainerFigure figure = new BorderedContainerFigure(createHeaderFigure(direction), direction);
		figure.addActionListener(this);
		return figure;
	}

	protected abstract Label createHeaderFigure(int direction);

	@Override
	protected List<Activity> getModelChildren() {
		BorderedContainerFigure figure = (BorderedContainerFigure) getFigure();
		if (figure.isExpanded()) {
			return super.getModelChildren();
		}
		else {
			return new ArrayList<Activity>();
		}
	}

	@Override
	protected void refreshVisuals() {
		((Label) ((SubgraphFigure) getFigure()).getHeader()).setText(getActivity().getName());
	}

}
