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
 * 
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