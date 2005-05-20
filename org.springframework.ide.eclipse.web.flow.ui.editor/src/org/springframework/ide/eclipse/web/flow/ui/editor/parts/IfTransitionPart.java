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
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.PolygonDecoration;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.graph.CompoundDirectedGraph;
import org.eclipse.draw2d.graph.Edge;
import org.eclipse.draw2d.graph.Node;
import org.eclipse.draw2d.graph.NodeList;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.editparts.AbstractConnectionEditPart;
import org.eclipse.gef.editpolicies.ConnectionEndpointEditPolicy;
import org.eclipse.ui.views.properties.IPropertySource;
import org.springframework.ide.eclipse.web.flow.core.model.IIf;
import org.springframework.ide.eclipse.web.flow.core.model.IIfTransition;
import org.springframework.ide.eclipse.web.flow.core.model.IState;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowModelElement;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowState;
import org.springframework.ide.eclipse.web.flow.ui.editor.WebFlowUIUtils;
import org.springframework.ide.eclipse.web.flow.ui.editor.policies.TransitionEditPolicy;

public class IfTransitionPart extends AbstractConnectionEditPart implements
        PropertyChangeListener {

    private PolylineConnection conn;

    private Label label;

    private boolean swapped = false;

    /**
     * @see org.eclipse.gef.EditPart#activate()
     */
    public void activate() {
        super.activate();
        ((IWebFlowModelElement) getModel()).addPropertyChangeListener(this);
    }

    protected void applyGraphResults(CompoundDirectedGraph graph, Map map) {
        Edge e = (Edge) map.get(this);
        NodeList nodes = e.vNodes;
        conn = (PolylineConnection) getConnectionFigure();
        conn.setTargetDecoration(new PolygonDecoration());

    }

    public void contributeToGraph(CompoundDirectedGraph graph, Map map) {
        GraphAnimation.recordInitialState(getConnectionFigure());
        Node source = (Node) map.get(getSource());
        Node target = (Node) map.get(getTarget());
        Edge e = null;
        if (target.data != null && target.data instanceof AbstractStatePart) {
            IState startState = ((IWebFlowState) ((AbstractStatePart) target.data)
                    .getState().getElementParent()).getStartState();
            IIf sourceState = (IIf) ((AbstractStatePart) source.data)
                    .getModel();
            IState targetState = ((AbstractStatePart) target.data).getState();
            if (startState.getId().equals(targetState.getId())) {
                e = new Edge(this, target, source);
                this.swapped = true;
            }
            else {
                List children = ((IWebFlowState) ((AbstractStatePart) target.data)
                        .getState().getElementParent()).getStates();
                int sourceIndex = children.indexOf(sourceState);
                int targetIndex = children.indexOf(targetState);
                if (targetIndex < sourceIndex) {
                    e = new Edge(this, target, source);
                    this.swapped = true;
                }
                else {
                    e = new Edge(this, source, target);
                    this.swapped = false;
                }
            }
        }
        else {
            List children = ((IWebFlowState) ((AbstractStatePart) target.data)
                    .getState().getElementParent()).getStates();
            int sourceIndex = children.indexOf(source);
            int targetIndex = children.indexOf(target);
            if (targetIndex < sourceIndex) {
                e = new Edge(this, target, source);
                this.swapped = true;
            }
            else {
                e = new Edge(this, source, target);
                this.swapped = false;
            }
        }

        graph.edges.add(e);
        map.put(this, e);
    }

    /**
     * @see org.eclipse.gef.editparts.AbstractEditPart#createEditPolicies()
     */
    protected void createEditPolicies() {
        installEditPolicy(EditPolicy.CONNECTION_ENDPOINTS_ROLE,
                new ConnectionEndpointEditPolicy());
        installEditPolicy(EditPolicy.CONNECTION_ROLE,
                new TransitionEditPolicy());
    }

    /**
     * @see org.eclipse.gef.editparts.AbstractConnectionEditPart#createFigure()
     */
    protected IFigure createFigure() {
        PolylineConnection conn = (PolylineConnection) super.createFigure();
        conn.setTargetDecoration(new PolygonDecoration());
        if (!getTransitionModel().isThen()) {
            conn.setLineStyle(Graphics.LINE_DOT);
        }
        return conn;
    }

    /**
     * @see org.eclipse.gef.EditPart#deactivate()
     */
    public void deactivate() {
        super.deactivate();
        ((IWebFlowModelElement) getModel()).removePropertyChangeListener(this);
    }

    public Object getAdapter(Class key) {
        if (IPropertySource.class == key) {
            return WebFlowUIUtils.getPropertySource(getTransitionModel());
        }
        return super.getAdapter(key);
    }

    public IIfTransition getTransitionModel() {
        return (IIfTransition) getModel();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt) {
        String prop = evt.getPropertyName();
        //if (IWebFlowModelElement.PROPS.equals(prop))

    }

    public void refreshVisuals() {
        if (!getTransitionModel().isThen() && conn != null) {
            conn.setLineStyle(Graphics.LINE_DOT);
        }
    }

    /**
     * @see org.eclipse.gef.EditPart#setSelected(int)
     */
    public void setSelected(int value) {
        super.setSelected(value);
        if (value != EditPart.SELECTED_NONE) {
            ((PolylineConnection) getFigure()).setLineWidth(2);
            if (label != null) {
                ((LineBorder) label.getBorder()).setWidth(2);
            }
        }
        else {
            ((PolylineConnection) getFigure()).setLineWidth(1);
            if (label != null) {
                ((LineBorder) label.getBorder()).setWidth(1);
            }
        }
    }
}