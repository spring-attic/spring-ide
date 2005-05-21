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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.AbstractConnectionAnchor;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.draw2d.graph.CompoundDirectedGraph;
import org.eclipse.draw2d.graph.Node;
import org.eclipse.draw2d.graph.Subgraph;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.NodeEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.tools.DirectEditManager;
import org.eclipse.ui.views.properties.IPropertySource;
import org.springframework.ide.eclipse.web.flow.core.model.IState;
import org.springframework.ide.eclipse.web.flow.core.model.ITransitionableFrom;
import org.springframework.ide.eclipse.web.flow.core.model.ITransitionableTo;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowModelElement;
import org.springframework.ide.eclipse.web.flow.ui.editor.WebFlowUtils;
import org.springframework.ide.eclipse.web.flow.ui.editor.policies.StateDirectEditPolicy;
import org.springframework.ide.eclipse.web.flow.ui.editor.policies.StateEditPolicy;
import org.springframework.ide.eclipse.web.flow.ui.editor.policies.StateNodeEditPolicy;

public abstract class AbstractStatePart extends AbstractGraphicalEditPart
        implements PropertyChangeListener, NodeEditPart {

    protected static class TopOrBottomAnchor extends AbstractConnectionAnchor {

        public static final int SOURCE_ANCHOR = 0;

        public static final int TARGET_ANCHOR = 1;

        private int anchor = 0;

        private int offset;

        TopOrBottomAnchor(IFigure source, int offset) {
            super(source);
            this.offset = offset;
        }

        public Point getLocation(Point reference) {
            Rectangle r = getOwner().getBounds().getCopy();
            getOwner().translateToAbsolute(r);
            int off = offset;
            if (off == -1)
                off = r.width / 2;
            if (r.contains(reference) || r.y < reference.y)
                return r.getBottom().translate(off, -1);
            else
                return r.getTop().translate(off, 0);
        }
    }

    private ConnectionAnchor connectionAnchor;

    protected DirectEditManager manager;

    public void activate() {
        super.activate();
        ((IWebFlowModelElement) getModel()).addPropertyChangeListener(this);
    }

    protected void applyGraphResults(CompoundDirectedGraph graph, Map map) {
        Node n = (Node) map.get(this);
        getFigure().setBounds(new Rectangle(n.x, n.y, n.width, n.height));

        for (int i = 0; i < getSourceConnections().size(); i++) {
            StateTransitionPart trans = (StateTransitionPart) getSourceConnections()
                    .get(i);
            trans.applyGraphResults(graph, map);
        }
    }

    public void contributeEdgesToGraph(CompoundDirectedGraph graph, Map map) {
        List outgoing = getSourceConnections();
        for (int i = 0; i < outgoing.size(); i++) {
            StateTransitionPart part = (StateTransitionPart) getSourceConnections()
                    .get(i);
            part.contributeToGraph(graph, map);
        }
        for (int i = 0; i < getChildren().size(); i++) {
            AbstractStatePart child = (AbstractStatePart) children.get(i);
            child.contributeEdgesToGraph(graph, map);
        }
    }

    public abstract void contributeNodesToGraph(CompoundDirectedGraph graph,
            Subgraph s, Map map);

    protected void createEditPolicies() {
        installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE,
                new StateNodeEditPolicy());
        installEditPolicy(EditPolicy.CONTAINER_ROLE, null);
        installEditPolicy(EditPolicy.COMPONENT_ROLE, new StateEditPolicy());
        installEditPolicy(EditPolicy.DIRECT_EDIT_ROLE,
                new StateDirectEditPolicy());
    }

    public void deactivate() {
        super.deactivate();
        ((IWebFlowModelElement) getModel()).removePropertyChangeListener(this);
    }

    public Object getAdapter(Class key) {
        if (IPropertySource.class == key) {
            return WebFlowUtils.getPropertySource(getModel());
        }
        return super.getAdapter(key);
    }

    protected List getModelSourceConnections() {
        if (getModel() instanceof ITransitionableFrom)
            return ((ITransitionableFrom) getModel()).getOutputTransitions();
        else {
            return Collections.EMPTY_LIST;
        }
    }

    protected List getModelTargetConnections() {
        if (getModel() instanceof ITransitionableTo)
            return ((ITransitionableTo) getModel()).getInputTransitions();
        else {
            return Collections.EMPTY_LIST;
        }
    }

    public ConnectionAnchor getSourceConnectionAnchor(
            ConnectionEditPart connection) {
        if (connectionAnchor == null)
            connectionAnchor = new TopOrBottomAnchor(getFigure(), 0);
        return connectionAnchor;
    }

    public ConnectionAnchor getSourceConnectionAnchor(Request request) {
        if (connectionAnchor == null)
            connectionAnchor = new TopOrBottomAnchor(getFigure(), 0);
        return connectionAnchor;
    }

    public IState getState() {
        return (IState) getModel();
    }

    public ConnectionAnchor getTargetConnectionAnchor(
            ConnectionEditPart connection) {
        if (connectionAnchor == null)
            connectionAnchor = new TopOrBottomAnchor(getFigure(), 0);
        return connectionAnchor;
    }

    public ConnectionAnchor getTargetConnectionAnchor(Request request) {
        if (connectionAnchor == null)
            connectionAnchor = new TopOrBottomAnchor(getFigure(), 0);
        return connectionAnchor;
    }

    protected void performDirectEdit() {
    }

    public void performRequest(Request request) {
        if (request.getType() == RequestConstants.REQ_DIRECT_EDIT) {
            performDirectEdit();
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        String prop = evt.getPropertyName();
        if (IWebFlowModelElement.ADD_CHILDREN.equals(prop)
                || IWebFlowModelElement.REMOVE_CHILDREN.equals(prop)) {
            refreshChildren();
        }
        else if (IWebFlowModelElement.MOVE_CHILDREN.equals(prop)) {
            refreshChildren();
        }
        else if (IWebFlowModelElement.INPUTS.equals(prop)) {
            refreshTargetConnections();
        }
        else if (IWebFlowModelElement.OUTPUTS.equals(prop)) {
            refreshSourceConnections();
        }
        else if (IWebFlowModelElement.PROPS.equals(prop)) {
            refreshVisuals();
        }
        // Causes Graph to re-layout
        ((GraphicalEditPart) (getViewer().getContents())).getFigure()
                .revalidate();
    }

    protected void setFigure(IFigure figure) {
        figure.getBounds().setSize(0, 0);
        super.setFigure(figure);
    }
}