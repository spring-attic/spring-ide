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
package org.springframework.ide.eclipse.beans.ui.livegraph.views;

import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.graph.DirectedGraph;
import org.eclipse.draw2d.graph.DirectedGraphLayout;
import org.eclipse.draw2d.graph.Edge;
import org.eclipse.draw2d.graph.Node;
import org.eclipse.swt.SWT;
import org.eclipse.zest.layouts.algorithms.DirectedGraphLayoutAlgorithm;
import org.eclipse.zest.layouts.dataStructures.InternalNode;
import org.eclipse.zest.layouts.dataStructures.InternalRelationship;

/**
 * Based on {@link DirectedGraphLayout} with modifications
 * 
 * @author Leo Dos Santos
 */
public class ExtendedDirectedGraphLayoutAlgorithm extends DirectedGraphLayoutAlgorithm {

	public ExtendedDirectedGraphLayoutAlgorithm(int styles) {
		super(styles);
	}

	@Override
	protected void applyLayoutInternal(InternalNode[] entitiesToLayout, InternalRelationship[] relationshipsToConsider,
			double boundsX, double boundsY, double boundsWidth, double boundsHeight) {
		HashMap mapping = new HashMap(entitiesToLayout.length);
		// Difference from DGLA; use the unmodified Draw2D DirectedGraph since
		// the extended one from the superclass does not handle the horizontal
		// use case properly.
		DirectedGraph graph = new DirectedGraph();
		for (InternalNode internalNode : entitiesToLayout) {
			Node node = new Node(internalNode);
			// Difference from DGLA; get the height/width from the InternalNode
			// and apply it to the Draw2D Node. Take orientation into account.
			int height = new Double(internalNode.getHeightInLayout()).intValue();
			int width = new Double(internalNode.getWidthInLayout()).intValue();
			if ((layout_styles & SWT.HORIZONTAL) == SWT.HORIZONTAL) {
				node.setSize(new Dimension(height, width));
			}
			else {
				node.setSize(new Dimension(width, height));
			}
			// End difference from DGLA
			mapping.put(internalNode, node);
			graph.nodes.add(node);
		}
		for (InternalRelationship relationship : relationshipsToConsider) {
			Node source = (Node) mapping.get(relationship.getSource());
			Node dest = (Node) mapping.get(relationship.getDestination());
			if (source != null && dest != null) {
				Edge edge = new Edge(relationship, source, dest);
				graph.edges.add(edge);
			}
		}
		DirectedGraphLayout directedGraphLayout = new DirectedGraphLayout();
		directedGraphLayout.visit(graph);

		for (Iterator iterator = graph.nodes.iterator(); iterator.hasNext();) {
			Node node = (Node) iterator.next();
			InternalNode internalNode = (InternalNode) node.data;
			// For horizontal layout transpose the x and y coordinates
			if ((layout_styles & SWT.HORIZONTAL) == SWT.HORIZONTAL) {
				internalNode.setInternalLocation(node.y, node.x);
			}
			else {
				internalNode.setInternalLocation(node.x, node.y);
			}
		}
		updateLayoutLocations(entitiesToLayout);
	}

}
