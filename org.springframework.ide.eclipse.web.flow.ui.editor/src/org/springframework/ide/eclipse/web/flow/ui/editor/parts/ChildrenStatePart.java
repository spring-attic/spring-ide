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
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.graph.CompoundDirectedGraph;
import org.eclipse.draw2d.graph.Subgraph;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.NodeEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.requests.DirectEditRequest;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TextCellEditor;
import org.springframework.ide.eclipse.web.flow.ui.editor.figures.CompoundStateFigure;
import org.springframework.ide.eclipse.web.flow.ui.editor.figures.SubgraphFigure;
import org.springframework.ide.eclipse.web.flow.ui.editor.model.WebFlowModelLabelDecorator;
import org.springframework.ide.eclipse.web.flow.ui.editor.model.WebFlowModelLabelProvider;
import org.springframework.ide.eclipse.web.flow.ui.editor.policies.StateContainerEditPolicy;
import org.springframework.ide.eclipse.web.flow.ui.editor.policies.StateContainerHighlightEditPolicy;
import org.springframework.ide.eclipse.web.flow.ui.editor.policies.StateEditPolicy;
import org.springframework.ide.eclipse.web.flow.ui.editor.policies.StateNodeEditPolicy;
import org.springframework.ide.eclipse.web.flow.ui.editor.policies.SubFlowStateDirectEditPolicy;
import org.springframework.ide.eclipse.web.flow.ui.editor.policies.WebFlowStateLayoutEditPolicy;

public abstract class ChildrenStatePart extends AbstractStatePart implements
        NodeEditPart {

    private static final Insets INNER_PADDING = new Insets(5, 0, 0, 0);

    protected static LabelProvider labelProvider = new DecoratingLabelProvider(
            new WebFlowModelLabelProvider(), new WebFlowModelLabelDecorator());

    private static final Insets PADDING = new Insets(0, 50, 50, 0);

    protected void applyChildrenResults(CompoundDirectedGraph graph, Map map) {
        for (int i = 0; i < getChildren().size(); i++) {
            AbstractStatePart part = (AbstractStatePart) getChildren().get(i);
            part.applyGraphResults(graph, map);
        }
    }

    protected void applyGraphResults(CompoundDirectedGraph graph, Map map) {
        applyOwnResults(graph, map);
        applyChildrenResults(graph, map);
    }

    protected void applyOwnResults(CompoundDirectedGraph graph, Map map) {
        super.applyGraphResults(graph, map);
    }

    public void contributeNodesToGraph(CompoundDirectedGraph graph, Subgraph s,
            Map map) {
        GraphAnimation.recordInitialState(getContentPane());
        Subgraph me = new Subgraph(this, s);
        me.outgoingOffset = 5;
        me.incomingOffset = 5;
        IFigure fig = getFigure();
        if (fig instanceof SubgraphFigure) {
            me.width = fig.getPreferredSize(me.width, me.height).width;
            me.height = fig.getPreferredSize().height;

            int tagHeight = ((SubgraphFigure) fig).getHeader()
                    .getPreferredSize().height;
            me.insets.top = 20; //tagHeight;
            me.insets.left = 5;
            me.insets.right = 5;
            me.insets.bottom = 5;
        }
        me.innerPadding = INNER_PADDING;
        me.setPadding(PADDING);
        map.put(this, me);
        graph.nodes.add(me);
        for (int i = 0; i < getChildren().size(); i++) {
            AbstractStatePart activity = (AbstractStatePart) getChildren().get(
                    i);
            activity.contributeNodesToGraph(graph, me, map);
        }
    }

    protected void createEditPolicies() {
        installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE,
                new StateNodeEditPolicy());
        installEditPolicy(EditPolicy.COMPONENT_ROLE, new StateEditPolicy());
        installEditPolicy(EditPolicy.SELECTION_FEEDBACK_ROLE,
                new StateContainerHighlightEditPolicy());
        installEditPolicy(EditPolicy.CONTAINER_ROLE,
                new StateContainerEditPolicy());
        installEditPolicy(EditPolicy.LAYOUT_ROLE,
                new WebFlowStateLayoutEditPolicy());
        installEditPolicy(EditPolicy.DIRECT_EDIT_ROLE,
                new SubFlowStateDirectEditPolicy());
    }

    protected IFigure createFigure() {
        CompoundStateFigure figure = new CompoundStateFigure();
        ((Label) figure.getHeader())
                .setIcon(labelProvider.getImage(getModel()));
        return figure;
    }

    private boolean directEditHitTest(Point requestLoc) {
        IFigure header = ((SubgraphFigure) getFigure()).getHeader();
        header.translateToRelative(requestLoc);
        if (header.containsPoint(requestLoc))
            return true;
        return false;
    }

    public IFigure getContentPane() {
        if (getFigure() instanceof SubgraphFigure)
            return ((SubgraphFigure) getFigure()).getContents();
        return getFigure();
    }

    protected void performDirectEdit() {
        if (manager == null) {
            Label l = ((Label) ((SubgraphFigure) getFigure()).getHeader());
            manager = new StateDirectEditManager(this, TextCellEditor.class,
                    new StateCellEditorLocator(l), l);
        }
        manager.show();
    }

    public void performRequest(Request request) {
        if (request.getType() == RequestConstants.REQ_DIRECT_EDIT) {
            if (request instanceof DirectEditRequest
                    && !directEditHitTest(((DirectEditRequest) request)
                            .getLocation().getCopy()))
                return;
            performDirectEdit();
        }
    }

    protected void refreshVisuals() {
        ((Label) ((SubgraphFigure) getFigure()).getHeader()).setText(getState()
                .getId());
        ((Label) ((SubgraphFigure) getFigure()).getHeader())
                .setIcon(labelProvider.getImage(getModel()));
    }

    public void setSelected(int value) {
        super.setSelected(value);
        SubgraphFigure sf = (SubgraphFigure) getFigure();
        sf.setSelected(value != SELECTED_NONE);
    }
}