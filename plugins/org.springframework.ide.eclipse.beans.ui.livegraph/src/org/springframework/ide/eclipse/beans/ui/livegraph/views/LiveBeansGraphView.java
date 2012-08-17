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

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.zest.core.widgets.Graph;
import org.eclipse.zest.core.widgets.GraphConnection;
import org.eclipse.zest.core.widgets.GraphNode;
import org.eclipse.zest.layouts.algorithms.TreeLayoutAlgorithm;

/**
 * A simple view to host our graph mockup
 * 
 * @author Leo Dos Santos
 */
public class LiveBeansGraphView extends ViewPart {

	public static final String VIEW_ID = "org.springframework.ide.eclipse.beans.ui.livegraph.views.LiveBeansGraphView";

	@Override
	public void createPartControl(Composite parent) {
		Graph graph = new Graph(parent, SWT.NONE);
		generateModel(graph);
		graph.setLayoutAlgorithm(new TreeLayoutAlgorithm(), true);
	}

	private void generateModel(Graph graph) {
		// TODO: use the LiveBeansModel
		GraphNode topBean = new GraphNode(graph, SWT.NONE, "topBean");
		GraphNode childBean1 = new GraphNode(graph, SWT.NONE, "childBean1");
		GraphNode childBean2 = new GraphNode(graph, SWT.NONE, "childBean2");
		GraphNode grandChild1 = new GraphNode(graph, SWT.NONE, "grandChildBean");
		new GraphNode(graph, SWT.NONE, "looseBean1");
		new GraphNode(graph, SWT.NONE, "looseBean2");
		new GraphConnection(graph, SWT.NONE, topBean, childBean1);
		new GraphConnection(graph, SWT.NONE, topBean, childBean2);
		new GraphConnection(graph, SWT.NONE, childBean2, grandChild1);
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

}
