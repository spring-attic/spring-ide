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

import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.graph.CompoundDirectedGraph;
import org.eclipse.draw2d.graph.Edge;
import org.eclipse.draw2d.graph.Node;
import org.springframework.ide.eclipse.webflow.core.model.IActionElement;
import org.springframework.ide.eclipse.webflow.core.model.IActionState;
import org.springframework.ide.eclipse.webflow.core.model.IAttributeMapper;
import org.springframework.ide.eclipse.webflow.core.model.IExceptionHandler;
import org.springframework.ide.eclipse.webflow.core.model.IState;
import org.springframework.ide.eclipse.webflow.core.model.ISubflowState;
import org.springframework.ide.eclipse.webflow.core.model.IViewState;
import org.springframework.ide.eclipse.webflow.ui.graph.figures.CompoundStateFigure;

/**
 * @author Christian Dupuis
 */
public class ActionStatePart extends ChildrenStatePart {

	/* (non-Javadoc)
	 * @see org.springframework.ide.eclipse.webflow.ui.graph.parts.ChildrenStatePart#applyChildrenResults(org.eclipse.draw2d.graph.CompoundDirectedGraph, java.util.Map)
	 */
	protected void applyChildrenResults(CompoundDirectedGraph graph, Map map) {
		CompoundStateFigure figure = (CompoundStateFigure) getFigure();
		int headerY = figure.getHeader().getBounds().getBottom().y + 8;
		for (int i = 0; i < getChildren().size(); i++) {
			AbstractStatePart part = (AbstractStatePart) getChildren().get(i);
			part.applyGraphResults(graph, map);
			Point p = part.getFigure().getBounds().getLocation();
			p.y = headerY + (i * 23);
			part.getFigure().setLocation(p);
		}
		applyMaxWidths();
	}

	/* (non-Javadoc)
	 * @see org.springframework.ide.eclipse.webflow.ui.graph.parts.AbstractStatePart#contributeEdgesToGraph(org.eclipse.draw2d.graph.CompoundDirectedGraph, java.util.Map)
	 */
	@SuppressWarnings("unchecked")
	public void contributeEdgesToGraph(CompoundDirectedGraph graph, Map map) {
		List outgoing = getSourceConnections();
		for (int i = 0; i < outgoing.size(); i++) {
			StateTransitionPart part = (StateTransitionPart) getSourceConnections()
					.get(i);
			part.contributeToGraph(graph, map);
		}
		for (int i = 0; i < getChildren().size(); i++) {
			AbstractStatePart child = (AbstractStatePart) children.get(i);

			if (child.getModel() instanceof IActionElement
					|| child.getModel() instanceof IExceptionHandler
					|| child.getModel() instanceof IAttributeMapper) {
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

	/* (non-Javadoc)
	 * @see org.springframework.ide.eclipse.webflow.ui.graph.parts.AbstractStatePart#onGetModelChildren(java.util.List)
	 */
	@SuppressWarnings("unchecked")
	protected void onGetModelChildren(List children) {
		IState state = (IState) getModel();
		if (state instanceof IActionState) {
			if (((IActionState) state).getActions() != null) {
				children.addAll(((IActionState) state).getActions());
			}
		}
		else if (state instanceof IViewState) {
			if (((IViewState) state).getRenderActions() != null) {
				children.addAll(((IViewState) state).getRenderActions()
						.getRenderActions());
			}
		}
		else if (state instanceof ISubflowState) {
			if (((ISubflowState) state).getAttributeMapper() != null) {
				children.add(((ISubflowState) state).getAttributeMapper());
			}
		}
	}
}
