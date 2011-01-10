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
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.graph.CompoundDirectedGraph;
import org.eclipse.draw2d.graph.Edge;
import org.eclipse.draw2d.graph.Node;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.Request;
import org.springframework.ide.eclipse.webflow.core.model.IActionElement;
import org.springframework.ide.eclipse.webflow.core.model.IInlineFlowState;
import org.springframework.ide.eclipse.webflow.core.model.IState;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;
import org.springframework.ide.eclipse.webflow.ui.graph.figures.CompoundStateFigure;
import org.springframework.ide.eclipse.webflow.ui.graph.figures.InlineFlowStateFigure;

/**
 * @author Christian Dupuis
 */
public class InlineFlowStatePart extends ChildrenStatePart {

	public void activate() {
		super.activate();
		((IInlineFlowState) getModel()).getWebFlowState()
				.addPropertyChangeListener(this);
	}
	
	protected IFigure createFigure() {
		InlineFlowStateFigure figure = new InlineFlowStateFigure((IWebflowModelElement) getModel());
		((Label) figure.getHeader())
				.setIcon(labelProvider.getImage(getModel()));
		((Label) figure.getHeader()).setIconTextGap(5);
		((Label) figure.getHeader()).setIconAlignment(PositionConstants.TOP);
		return figure;
	}

	public void deactivate() {
		super.deactivate();
		((IInlineFlowState) getModel()).getWebFlowState()
				.removePropertyChangeListener(this);
	}

	public ConnectionAnchor getSourceConnectionAnchor(
			ConnectionEditPart connection) {
		return null;
	}

	public ConnectionAnchor getSourceConnectionAnchor(Request request) {
		return null;
	}

	public ConnectionAnchor getTargetConnectionAnchor(
			ConnectionEditPart connection) {
		return null;
	}

	public ConnectionAnchor getTargetConnectionAnchor(Request request) {
		return null;
	}

	protected void applyChildrenResults(CompoundDirectedGraph graph, Map map) {
		CompoundStateFigure figure = (CompoundStateFigure) getFigure();
		int headerY = figure.getHeader().getBounds().getBottom().y + 7;

		int diff = -1;
		for (int i = 0; i < getChildren().size(); i++) {
			AbstractStatePart part = (AbstractStatePart) getChildren().get(i);
			part.applyGraphResults(graph, map);

			Point p = part.getFigure().getBounds().getLocation();

			if (diff == -1) {
				diff = p.y - headerY;
			}
			p.y = p.y - diff;
			part.getFigure().setLocation(p);
		}
	}

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

			if (child.getModel() instanceof IActionElement) {
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

	protected List getModelChildren() {
		if (getModel() instanceof IInlineFlowState) {
			List<IState> states = new ArrayList<IState>();
            states.addAll(((IInlineFlowState) getModel()).getWebFlowState()
					.getStates());
            states.addAll(((IInlineFlowState) getModel()).getWebFlowState()
					.getInlineFlowStates());
			return states;
		}
		else {
			return Collections.EMPTY_LIST;
		}
	}
}
