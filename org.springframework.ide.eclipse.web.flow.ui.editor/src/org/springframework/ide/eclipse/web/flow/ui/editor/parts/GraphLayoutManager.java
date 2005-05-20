/*
 * Copyright 2002-2005 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.springframework.ide.eclipse.web.flow.ui.editor.parts;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.AbstractLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.draw2d.graph.CompoundDirectedGraph;
import org.eclipse.draw2d.graph.CompoundDirectedGraphLayout;

class GraphLayoutManager extends AbstractLayout {

    private WebFlowPart diagram;

    GraphLayoutManager(WebFlowPart diagram) {
        this.diagram = diagram;
    }

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

    public void layout(IFigure container) {
        GraphAnimation.recordInitialState(container);
        if (GraphAnimation.playbackState(container))
            return;

        CompoundDirectedGraph graph = new CompoundDirectedGraph();
        Map partsToNodes = new HashMap();
        diagram.contributeNodesToGraph(graph, null, partsToNodes);
        diagram.contributeEdgesToGraph(graph, partsToNodes);
        new CompoundDirectedGraphLayout().visit(graph);
        diagram.applyGraphResults(graph, partsToNodes);
    }

}