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
package org.springframework.ide.eclipse.config.ui.editors.webflow.graph.parts;

import java.util.Map;

import org.eclipse.draw2d.graph.CompoundDirectedGraph;
import org.eclipse.draw2d.graph.Edge;
import org.eclipse.draw2d.graph.Node;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.springframework.ide.eclipse.config.graph.model.LabelledTransition;
import org.springframework.ide.eclipse.config.graph.parts.ActivityPart;
import org.springframework.ide.eclipse.config.graph.parts.LabelledTransitionPart;


/**
 * @author Leo Dos Santos
 */
public class WebFlowTransitionPart extends LabelledTransitionPart {

	public WebFlowTransitionPart(LabelledTransition model) {
		super(model);
	}

	@Override
	public void contributeToGraph(CompoundDirectedGraph graph, Map<AbstractGraphicalEditPart, Object> map) {
		Node source = (Node) map.get(getSource());
		Node target = (Node) map.get(getTarget());
		int sourceIndex = ((ActivityPart) getSource()).getModelElement().getSortIndex();
		int targetIndex = ((ActivityPart) getTarget()).getModelElement().getSortIndex();
		Node n = (Node) map.get(this);
		if (targetIndex < sourceIndex) {
			graph.edges.add(new Edge(target, n));
			graph.edges.add(new Edge(n, source));
		}
		else {
			graph.edges.add(new Edge(source, n));
			graph.edges.add(new Edge(n, target));
		}
	}

}
