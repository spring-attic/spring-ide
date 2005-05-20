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

import java.util.Map;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.graph.CompoundDirectedGraph;
import org.eclipse.draw2d.graph.Node;
import org.eclipse.draw2d.graph.Subgraph;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Color;
import org.springframework.ide.eclipse.web.flow.core.model.IAction;
import org.springframework.ide.eclipse.web.flow.core.model.IAttributeMapper;
import org.springframework.ide.eclipse.web.flow.core.model.IEndState;
import org.springframework.ide.eclipse.web.flow.ui.editor.figures.StateLabel;
import org.springframework.ide.eclipse.web.flow.ui.editor.model.WebFlowModelLabelDecorator;
import org.springframework.ide.eclipse.web.flow.ui.editor.model.WebFlowModelLabelProvider;

public class StatePart extends AbstractStatePart {

    public static final Color COLOR = new Color(null, 255, 255, 206);

    private static LabelProvider labelProvider = new DecoratingLabelProvider(
            new WebFlowModelLabelProvider(), new WebFlowModelLabelDecorator());

    public void contributeNodesToGraph(CompoundDirectedGraph graph, Subgraph s,
            Map map) {
        Node n = new Node(this, s);
        n.outgoingOffset = 9;
        n.incomingOffset = 9;
        n.width = getFigure().getPreferredSize().width + 5;
        n.height = getFigure().getPreferredSize().height;
        if (getModel() instanceof IEndState)
            n.setPadding(new Insets(0, 40, 10, 40));
        else if (getModel() instanceof IAction
                || getModel() instanceof IAttributeMapper)
            n.setPadding(new Insets(0, 5, 5, 5));
        else
            n.setPadding(new Insets(0, 50, 50, 50));
        map.put(this, n);
        graph.nodes.add(n);

    }

    protected IFigure createFigure() {
        Label l = new StateLabel();
        l.setBackgroundColor(COLOR);
        l.setLabelAlignment(PositionConstants.LEFT);
        l.setIcon(labelProvider.getImage(getModel()));
        l.setBorder(new LineBorder());
        return l;
    }

    protected void performDirectEdit() {
        /*
         * if (manager == null) { Label l = (Label) getFigure(); manager = new
         * StateDirectEditManager(this, TextCellEditor.class, new
         * StateCellEditorLocator(l), l); } manager.show();
         */
    }

    protected void refreshVisuals() {
        ((Label) getFigure()).setText(labelProvider.getText(getModel()));
        // ((Label) getFigure()).setToolTip(new Label(labelProvider.getLongText(getModel())));
        ((Label) getFigure()).setIcon(labelProvider.getImage(getModel()));
    }

}