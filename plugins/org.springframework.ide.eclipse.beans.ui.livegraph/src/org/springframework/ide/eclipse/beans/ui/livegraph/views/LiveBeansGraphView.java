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
import org.eclipse.zest.core.viewers.GraphViewer;
import org.eclipse.zest.layouts.LayoutStyles;
import org.eclipse.zest.layouts.algorithms.TreeLayoutAlgorithm;
import org.springframework.ide.eclipse.beans.ui.livegraph.model.LiveBeansModel;

/**
 * A simple view to host our graph mockup
 * 
 * @author Leo Dos Santos
 */
public class LiveBeansGraphView extends ViewPart {

	public static final String VIEW_ID = "org.springframework.ide.eclipse.beans.ui.livegraph.views.LiveBeansGraphView";

	@Override
	public void createPartControl(Composite parent) {
		GraphViewer graph = new GraphViewer(parent, SWT.NONE);
		graph.setContentProvider(new LiveBeansGraphContentProvider());
		graph.setLabelProvider(new LiveBeansGraphLabelProvider());
		graph.setInput(new LiveBeansModel());
		graph.setLayoutAlgorithm(new TreeLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING), true);
		graph.applyLayout();
		getSite().setSelectionProvider(graph);
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

}
