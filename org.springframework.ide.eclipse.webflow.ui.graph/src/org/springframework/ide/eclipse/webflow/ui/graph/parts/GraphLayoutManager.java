/*******************************************************************************
 * Copyright (c) 2005, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.webflow.ui.graph.parts;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.AbstractLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.draw2d.graph.CompoundDirectedGraph;
import org.eclipse.draw2d.graph.CompoundDirectedGraphLayout;

/**
 * 
 */
class GraphLayoutManager extends AbstractLayout {

    /**
     * 
     */
    private FlowPart diagram;

    /**
     * 
     * 
     * @param diagram 
     */
    GraphLayoutManager(FlowPart diagram) {
        this.diagram = diagram;
    }

    /* (non-Javadoc)
     * @see org.eclipse.draw2d.AbstractLayout#calculatePreferredSize(org.eclipse.draw2d.IFigure, int, int)
     */
    protected Dimension calculatePreferredSize(IFigure container, int wHint,
            int hHint) {
        container.validate();
        List children = container.getChildren();
        Rectangle result = new Rectangle().setLocation(container
                .getClientArea().getLocation());
        for (int i = 0; i < children.size(); i++)
            result.union(((IFigure) children.get(i)).getBounds());
        result.resize(container.getInsets().getWidth(), container.getInsets()
                .getHeight());
        return result.getSize();
    }

    /* (non-Javadoc)
     * @see org.eclipse.draw2d.LayoutManager#layout(org.eclipse.draw2d.IFigure)
     */
    public void layout(IFigure container) {
        GraphAnimation.recordInitialState(container);
        if (GraphAnimation.playbackState(container))
            return;

        CompoundDirectedGraph graph = new CompoundDirectedGraph();
        graph.setDirection(PositionConstants.SOUTH);
        Map partsToNodes = new HashMap();
        diagram.contributeNodesToGraph(graph, null, partsToNodes);
        diagram.contributeEdgesToGraph(graph, partsToNodes);
        new CompoundDirectedGraphLayout().visit(graph);
        diagram.applyGraphResults(graph, partsToNodes);
    }

}
