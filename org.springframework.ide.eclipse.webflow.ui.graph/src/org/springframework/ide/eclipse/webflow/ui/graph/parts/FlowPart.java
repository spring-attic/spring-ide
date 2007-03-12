/*
 * Copyright 2002-2007 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ide.eclipse.webflow.ui.graph.parts;

import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.EventObject;
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.ConnectionLayer;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.ShortestPathConnectionRouter;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.draw2d.graph.CompoundDirectedGraph;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.commands.CommandStackListener;
import org.eclipse.swt.SWT;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowState;
import org.springframework.ide.eclipse.webflow.ui.graph.policies.FlowEditPolicy;
import org.springframework.ide.eclipse.webflow.ui.graph.policies.FlowStateLayoutEditPolicy;
import org.springframework.ide.eclipse.webflow.ui.graph.policies.StateContainerEditPolicy;

/**
 * 
 * 
 * @author hudsonr Created on Jul 16, 2003
 */
public class FlowPart extends ChildrenStatePart implements
        PropertyChangeListener {

    /**
     * 
     */
    String CONNECTION_LAYER = "Connection Layer";

    /**
     * 
     */
    AnimatedFanConnectionRouter fanRouter = null;

    /**
     * 
     */
    ShortestPathConnectionRouter router = null;

    /**
     * 
     */
    CommandStackListener stackListener = new CommandStackListener() {

        public void commandStackChanged(EventObject event) {
            if (!GraphAnimation.captureLayout(getFigure()))
                return;
            while (GraphAnimation.step())
                getFigure().getUpdateManager().performUpdate();
            GraphAnimation.end();
        }
    };

    /**
     * 
     * 
     * @see org.springframework.ide.eclipse.webflow.ui.graph.parts.AbstractStatePart#activate()
     */
    public void activate() {
        super.activate();
        getViewer().getEditDomain().getCommandStack().addCommandStackListener(
                stackListener);
    }

    /* (non-Javadoc)
     * @see org.springframework.ide.eclipse.webflow.ui.graph.parts.ChildrenStatePart#applyOwnResults(org.eclipse.draw2d.graph.CompoundDirectedGraph, java.util.Map)
     */
    protected void applyOwnResults(CompoundDirectedGraph graph, Map map) {
    }

    /**
     * 
     * 
     * @see org.springframework.ide.eclipse.webflow.ui.graph.parts.AbstractStatePart#createEditPolicies()
     */
    protected void createEditPolicies() {
        installEditPolicy(EditPolicy.NODE_ROLE, null);
        installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE, null);
        installEditPolicy(EditPolicy.SELECTION_FEEDBACK_ROLE, null);
        installEditPolicy(EditPolicy.COMPONENT_ROLE,
                new FlowEditPolicy());
        installEditPolicy(EditPolicy.LAYOUT_ROLE,
                new FlowStateLayoutEditPolicy());
        installEditPolicy(EditPolicy.CONTAINER_ROLE,
                new StateContainerEditPolicy());
    }

    /* (non-Javadoc)
     * @see org.springframework.ide.eclipse.webflow.ui.graph.parts.ChildrenStatePart#createFigure()
     */
    protected IFigure createFigure() {
        Figure f = new Figure() {

            public void setBounds(Rectangle rect) {
                int x = bounds.x, y = bounds.y;

                boolean resize = (rect.width != bounds.width)
                        || (rect.height != bounds.height), translate = (rect.x != x)
                        || (rect.y != y);

                if (isVisible() && (resize || translate))
                    erase();
                if (translate) {
                    int dx = rect.x - x;
                    int dy = rect.y - y;
                    primTranslate(dx, dy);
                }
                bounds.width = rect.width;
                bounds.height = rect.height;
                if (resize || translate) {
                    fireFigureMoved();
                    repaint();
                }
            }
        };
        f.setLayoutManager(new GraphLayoutManager(this));
        return f;
    }

    /**
     * 
     * 
     * @see org.springframework.ide.eclipse.webflow.ui.graph.parts.AbstractStatePart#deactivate()
     */
    public void deactivate() {
        getViewer().getEditDomain().getCommandStack()
                .removeCommandStackListener(stackListener);
        super.deactivate();
    }

    /* (non-Javadoc)
     * @see org.springframework.ide.eclipse.webflow.ui.graph.parts.AbstractStatePart#getModelChildren()
     */
    protected List getModelChildren() {
        if (((IWebflowState) getState()).getStates() != null) {
            return ((IWebflowState) getState()).getStates();
        }
        else {
            return Collections.EMPTY_LIST;
        }
    }

    /**
     * 
     * 
     * @return 
     * 
     * @see org.eclipse.gef.editparts.AbstractEditPart#isSelectable()
     */
    public boolean isSelectable() {
        return true;
    }

    /**
     * 
     * 
     * @see org.springframework.ide.eclipse.webflow.ui.graph.parts.ChildrenStatePart#refreshVisuals()
     */
    protected void refreshVisuals() {

        ConnectionLayer cLayer = (ConnectionLayer) getLayer(CONNECTION_LAYER);
        cLayer.setAntialias(SWT.ON);
        if (fanRouter == null && router == null) {
            fanRouter = new AnimatedFanConnectionRouter();
            fanRouter.setSeparation(20);
            router = new ShortestPathConnectionRouter(getFigure());
        }
        fanRouter.setNextRouter(router);
        cLayer.setConnectionRouter(fanRouter);
    }
}