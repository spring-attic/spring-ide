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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.graph.CompoundDirectedGraph;
import org.eclipse.draw2d.graph.Node;
import org.eclipse.draw2d.graph.Subgraph;
import org.eclipse.swt.graphics.Color;
import org.springframework.ide.eclipse.web.flow.core.model.IIf;
import org.springframework.ide.eclipse.web.flow.ui.editor.figures.StateLabel;
import org.springframework.ide.eclipse.web.flow.ui.editor.model.WebFlowModelLabelProvider;

public class IfPart extends AbstractStatePart {

    public static final Color COLOR = new Color(null, 255, 255, 206);

    private static WebFlowModelLabelProvider labelProvider = new WebFlowModelLabelProvider();

    public void contributeEdgesToGraph(CompoundDirectedGraph graph, Map map) {
        List outgoing = getSourceConnections();
        for (int i = 0; i < outgoing.size(); i++) {
            IfTransitionPart part = (IfTransitionPart) getSourceConnections()
                    .get(i);
            part.contributeToGraph(graph, map);
        }
    }

    public void contributeNodesToGraph(CompoundDirectedGraph graph, Subgraph s,
            Map map) {
        Node n = new Node(this, s);
        n.outgoingOffset = 9;
        n.incomingOffset = 9;
        n.width = getFigure().getPreferredSize().width + 5;
        n.height = getFigure().getPreferredSize().height;
        n.setPadding(new Insets(0, 5, 5, 5));
        map.put(this, n);
        graph.nodes.add(n);

    }

    /**
     * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#createFigure()
     */
    protected IFigure createFigure() {
        Label l = new StateLabel();
        l.setBackgroundColor(COLOR);
        l.setLabelAlignment(PositionConstants.LEFT);
        l.setIcon(labelProvider.getImage(getModel()));
        l.setBorder(new LineBorder());
        return l;
    }

    /**
     * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#getModelSourceConnections()
     */
    protected List getModelSourceConnections() {
        if (getModel() instanceof IIf) {
            List sourceConnections = new ArrayList();
            IIf theIf = (IIf) getModel();
            if (theIf.getThenTransition() != null) {
                sourceConnections.add(theIf.getThenTransition());
            }
            if (theIf.getElseTransition() != null) {
                sourceConnections.add(theIf.getElseTransition());
            }
            return sourceConnections;
        }
        else {
            return Collections.EMPTY_LIST;
        }
    }

    protected void performDirectEdit() {
        /*
         * if (manager == null) { Label l = (Label) getFigure(); manager = new
         * StateDirectEditManager(this, TextCellEditor.class, new
         * StateCellEditorLocator(l), l); } manager.show();
         */
    }

    /**
     * @see org.eclipse.gef.editparts.AbstractEditPart#refreshVisuals()
     */
    protected void refreshVisuals() {
        ((Label) getFigure()).setText(labelProvider.getText(getModel()));
        ((Label) getFigure()).setToolTip(new Label(labelProvider
                .getLongText(getModel())));
    }
}