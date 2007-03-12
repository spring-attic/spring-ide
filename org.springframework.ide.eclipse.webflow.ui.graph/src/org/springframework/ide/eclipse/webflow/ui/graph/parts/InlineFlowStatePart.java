/*
 * Copyright 2002-2007 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ide.eclipse.webflow.ui.graph.parts;

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
import org.springframework.ide.eclipse.webflow.ui.graph.figures.CompoundStateFigure;
import org.springframework.ide.eclipse.webflow.ui.graph.figures.InlineFlowStateFigure;

/**
 * 
 */
public class InlineFlowStatePart extends ChildrenStatePart {

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.webflow.ui.graph.parts.AbstractStatePart#activate()
	 */
	public void activate() {
		super.activate();
		((IInlineFlowState) getModel()).getWebFlowState()
				.addPropertyChangeListener(this);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#createFigure()
	 */
	protected IFigure createFigure() {
		InlineFlowStateFigure figure = new InlineFlowStateFigure();
		((Label) figure.getHeader())
				.setIcon(labelProvider.getImage(getModel()));
		((Label) figure.getHeader()).setIconTextGap(5);
		((Label) figure.getHeader()).setIconAlignment(PositionConstants.TOP);
		return figure;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.webflow.ui.graph.parts.AbstractStatePart#deactivate()
	 */
	public void deactivate() {
		super.deactivate();
		((IInlineFlowState) getModel()).getWebFlowState()
				.removePropertyChangeListener(this);
	}

	/**
	 * @param connection
	 * @return
	 */
	public ConnectionAnchor getSourceConnectionAnchor(
			ConnectionEditPart connection) {
		return null;
	}

	/**
	 * @param request
	 * @return
	 */
	public ConnectionAnchor getSourceConnectionAnchor(Request request) {
		return null;
	}

	/**
	 * @param connection
	 * @return
	 */
	public ConnectionAnchor getTargetConnectionAnchor(
			ConnectionEditPart connection) {
		return null;
	}

	/**
	 * @param request
	 * @return
	 */
	public ConnectionAnchor getTargetConnectionAnchor(Request request) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.webflow.ui.graph.parts.ChildrenStatePart#applyChildrenResults(org.eclipse.draw2d.graph.CompoundDirectedGraph,
	 * java.util.Map)
	 */
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

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.webflow.ui.graph.parts.AbstractStatePart#contributeEdgesToGraph(org.eclipse.draw2d.graph.CompoundDirectedGraph,
	 * java.util.Map)
	 */
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

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.webflow.ui.graph.parts.AbstractStatePart#getModelChildren()
	 */
	protected List getModelChildren() {
		if (getModel() instanceof IInlineFlowState) {
			List child = ((IInlineFlowState) getModel()).getWebFlowState()
					.getStates();
			return child;
		}
		else {
			return Collections.EMPTY_LIST;
		}
	}
}