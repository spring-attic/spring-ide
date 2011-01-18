/*******************************************************************************
 * Copyright (c) 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.webflow.ui.graph.parts;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.draw2d.graph.CompoundDirectedGraph;
import org.eclipse.draw2d.graph.Edge;
import org.eclipse.draw2d.graph.Node;
import org.springframework.ide.eclipse.webflow.core.model.IActionElement;
import org.springframework.ide.eclipse.webflow.core.model.IDecisionState;
import org.springframework.ide.eclipse.webflow.core.model.IExceptionHandler;
import org.springframework.ide.eclipse.webflow.core.model.IIf;
import org.springframework.ide.eclipse.webflow.core.model.IState;
import org.springframework.ide.eclipse.webflow.ui.graph.figures.CompoundStateFigure;

/**
 * @author Christian Dupuis
 */
public class DecisionStatePart extends ChildrenStatePart {

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.webflow.ui.graph.parts.ChildrenStatePart#applyChildrenResults(org.eclipse.draw2d.graph.CompoundDirectedGraph,
	 * java.util.Map)
	 */
	protected void applyChildrenResults(CompoundDirectedGraph graph, Map map) {
		CompoundStateFigure figure = (CompoundStateFigure) getFigure();
		int headerY = figure.getHeader().getBounds().getBottom().y + 9;
		int y = 0;
		int x = -1;
		for (int i = 0; i < getChildren().size(); i++) {
			AbstractStatePart part = (AbstractStatePart) getChildren().get(i);
			if (part.getModel() instanceof IActionElement
					|| part.getModel() instanceof IExceptionHandler) {
				part.applyGraphResults(graph, map);
				Point p = part.getFigure().getBounds().getLocation();
				if (x == -1) {
					x = p.x;
				}
				y = headerY + (i * 23);
				p.y = y;
				p.x = x;
				part.getFigure().setLocation(p);
			}
		}
		for (int i = 0; i < getChildren().size(); i++) {
			AbstractStatePart part = (AbstractStatePart) getChildren().get(i);
			if (!(part.getModel() instanceof IActionElement && !(part
					.getModel() instanceof IExceptionHandler))) {
				// part.applyGraphResults(graph, map);
				Node n = (Node) map.get(part);
				Dimension dim = part.getFigure().getPreferredSize();
				part.getFigure().setBounds(
						new Rectangle(n.x, n.y + 5, n.width, dim.height));
			}
		}

		applyMaxWidths();
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.webflow.ui.graph.parts.AbstractStatePart#contributeEdgesToGraph(org.eclipse.draw2d.graph.CompoundDirectedGraph,
	 * java.util.Map)
	 */
	@SuppressWarnings("unchecked")
	public void contributeEdgesToGraph(CompoundDirectedGraph graph, Map map) {
		List outgoing = getSourceConnections();
		for (int i = 0; i < outgoing.size(); i++) {
			StateTransitionPart part = (StateTransitionPart) getSourceConnections()
					.get(i);
			part.contributeToGraph(graph, map);
		}

		Iterator iter = ((IDecisionState) getModel()).getIfs().iterator();
		int j = 0;
		while (iter.hasNext()) {
			IIf theIf = (IIf) iter.next();
			if (theIf.getThenTransition() != null
					&& theIf.getThenTransition().getToState() != null) {
				// insert dummy edges for layouting
				Edge e1 = new Edge((Node) map.get(this), getNode(theIf
						.getThenTransition().getToState(), map));

				// e1.weight = 5;
				graph.edges.add(e1);
				map.put(this.toString() + (j++), e1);
			}
			if (theIf.getElseTransition() != null
					&& theIf.getElseTransition().getToState() != null) {
				Edge e2 = new Edge((Node) map.get(this), getNode(theIf
						.getElseTransition().getToState(), map));
				// e2.weight = 5;
				graph.edges.add(e2);
				map.put(this.toString() + (j++), e2);
			}

		}

		for (int i = 0; i < getChildren().size(); i++) {
			AbstractStatePart child = (AbstractStatePart) children.get(i);

			if (child.getModel() instanceof IActionElement
					|| child.getModel() instanceof IExceptionHandler) {
				if (i + 1 < children.size()) {
					// insert dummy edges
					Edge e = new Edge((Node) map.get(child), (Node) map
							.get(getChildren().get(i + 1)));
					e.weight = 1;
					graph.edges.add(e);
					map.put(this.toString() + i, e);
				}
			}
			else {
				child.contributeEdgesToGraph(graph, map);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.webflow.ui.graph.parts.AbstractStatePart#onGetModelChildren(java.util.List)
	 */
	@SuppressWarnings("unchecked")
	protected void onGetModelChildren(List children) {
		if (getModel() instanceof IDecisionState) {
			children.addAll(((IDecisionState) getState()).getIfs());
		}
	}

	/**
	 * @param map
	 * @param model
	 * @return
	 */
	private Node getNode(Object model, Map map) {
		Node node = null;
		Iterator iter = map.values().iterator();
		while (iter.hasNext()) {
			Object obj = (Object) iter.next();
			if (obj instanceof Node) {
				Node tempNode = (Node) obj;
				if (model
						.equals(((AbstractStatePart) tempNode.data).getModel())) {
					node = tempNode;
					break;
				}
			}
		}
		return node;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.webflow.ui.graph.parts.AbstractStatePart#getModelChildren()
	 */
	@SuppressWarnings("unchecked")
	protected List getModelChildren() {
		List children = new ArrayList();
		if (getModel() instanceof IState) {
			if (((IState) getState()).getEntryActions() != null) {
				children.addAll(((IState) getState()).getEntryActions()
						.getEntryActions());
			}
			if (((IState) getState()).getExitActions() != null) {
				children.addAll(((IState) getState()).getExitActions()
						.getExitActions());
			}
			if (((IState) getState()).getExceptionHandlers() != null) {
				children.addAll(((IState) getState()).getExceptionHandlers());
			}

			onGetModelChildren(children);
		}
		return children;
	}
}
