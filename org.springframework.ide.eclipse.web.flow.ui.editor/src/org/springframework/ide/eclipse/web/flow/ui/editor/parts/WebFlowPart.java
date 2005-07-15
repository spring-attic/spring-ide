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
import org.eclipse.ui.views.properties.IPropertySource;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowState;
import org.springframework.ide.eclipse.web.flow.ui.editor.WebFlowUtils;
import org.springframework.ide.eclipse.web.flow.ui.editor.policies.StateContainerEditPolicy;
import org.springframework.ide.eclipse.web.flow.ui.editor.policies.WebFlowEditPolicy;
import org.springframework.ide.eclipse.web.flow.ui.editor.policies.WebFlowStateLayoutEditPolicy;

/**
 * @author hudsonr Created on Jul 16, 2003
 */
public class WebFlowPart extends ChildrenStatePart implements
        PropertyChangeListener {

    String CONNECTION_LAYER = "Connection Layer";

    AnimatedFanConnectionRouter fanRouter = null;

    ShortestPathConnectionRouter router = null;

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
     * @see org.springframework.ide.eclipse.web.flow.ui.editor.parts.AbstractStatePart#activate()
     */
    public void activate() {
        super.activate();
        getViewer().getEditDomain().getCommandStack().addCommandStackListener(
                stackListener);
    }

    protected void applyOwnResults(CompoundDirectedGraph graph, Map map) {
    }

    /**
     * @see org.springframework.ide.eclipse.web.flow.ui.editor.parts.AbstractStatePart#createEditPolicies()
     */
    protected void createEditPolicies() {
        installEditPolicy(EditPolicy.NODE_ROLE, null);
        installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE, null);
        installEditPolicy(EditPolicy.SELECTION_FEEDBACK_ROLE, null);
        installEditPolicy(EditPolicy.COMPONENT_ROLE,
                new WebFlowEditPolicy());
        installEditPolicy(EditPolicy.LAYOUT_ROLE,
                new WebFlowStateLayoutEditPolicy());
        installEditPolicy(EditPolicy.CONTAINER_ROLE,
                new StateContainerEditPolicy());
    }

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
     * @see org.springframework.ide.eclipse.web.flow.ui.editor.parts.AbstractStatePart#deactivate()
     */
    public void deactivate() {
        getViewer().getEditDomain().getCommandStack()
                .removeCommandStackListener(stackListener);
        super.deactivate();
    }

    public Object getAdapter(Class key) {
        if (IPropertySource.class == key) {
            return WebFlowUtils.getPropertySource(getState());
        }
        return super.getAdapter(key);
    }

    protected List getModelChildren() {
        if (((IWebFlowState) getState()).getStates() != null) {
            return ((IWebFlowState) getState()).getStates();
        }
        else {
            return Collections.EMPTY_LIST;
        }
    }

    /**
     * @see org.eclipse.gef.editparts.AbstractEditPart#isSelectable()
     */
    public boolean isSelectable() {
        return true;
    }

    /**
     * @see org.springframework.ide.eclipse.web.flow.ui.editor.parts.ChildrenStatePart#refreshVisuals()
     */
    protected void refreshVisuals() {

        ConnectionLayer cLayer = (ConnectionLayer) getLayer(CONNECTION_LAYER);

        if (fanRouter == null && router == null) {
            fanRouter = new AnimatedFanConnectionRouter();
            router = new ShortestPathConnectionRouter(getFigure());
        }
        fanRouter.setNextRouter(router);
        cLayer.setConnectionRouter(fanRouter);
    }
}