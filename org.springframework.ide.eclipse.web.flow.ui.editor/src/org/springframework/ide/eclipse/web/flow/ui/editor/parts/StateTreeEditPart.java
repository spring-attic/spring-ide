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
import java.util.ArrayList;
import java.util.List;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.views.properties.IPropertySource;
import org.springframework.ide.eclipse.web.flow.core.model.IActionState;
import org.springframework.ide.eclipse.web.flow.core.model.IAttributeMapper;
import org.springframework.ide.eclipse.web.flow.core.model.IDecisionState;
import org.springframework.ide.eclipse.web.flow.core.model.IPropertyEnabled;
import org.springframework.ide.eclipse.web.flow.core.model.IState;
import org.springframework.ide.eclipse.web.flow.core.model.ISubFlowState;
import org.springframework.ide.eclipse.web.flow.core.model.IViewState;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowModelElement;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowState;
import org.springframework.ide.eclipse.web.flow.ui.editor.WebFlowUtils;
import org.springframework.ide.eclipse.web.flow.ui.editor.model.WebFlowModelLabelDecorator;
import org.springframework.ide.eclipse.web.flow.ui.editor.model.WebFlowModelLabelProvider;
import org.springframework.ide.eclipse.web.flow.ui.editor.policies.StateEditPolicy;
import org.springframework.ide.eclipse.web.flow.ui.editor.policies.StateTreeContainerEditPolicy;
import org.springframework.ide.eclipse.web.flow.ui.editor.policies.StateTreeEditPolicy;

public class StateTreeEditPart extends
        org.eclipse.gef.editparts.AbstractTreeEditPart implements
        PropertyChangeListener {

    protected static LabelProvider labelProvider = new DecoratingLabelProvider(
            new WebFlowModelLabelProvider(), new WebFlowModelLabelDecorator());

    protected static WebFlowModelLabelProvider tLabelProvider = new WebFlowModelLabelProvider();

    public void activate() {
        super.activate();
        ((IWebFlowModelElement) getModel()).addPropertyChangeListener(this);
    }

    protected void createEditPolicies() {
        installEditPolicy(EditPolicy.COMPONENT_ROLE, new StateEditPolicy());
        installEditPolicy(EditPolicy.TREE_CONTAINER_ROLE,
                new StateTreeContainerEditPolicy());
        installEditPolicy(EditPolicy.PRIMARY_DRAG_ROLE,
                new StateTreeEditPolicy());
    }

    public void deactivate() {
        ((IWebFlowModelElement) getModel()).removePropertyChangeListener(this);
        super.deactivate();
    }

    public Object getAdapter(Class key) {
        if (IPropertySource.class == key) {
            return WebFlowUtils.getPropertySource(getModel());
        }

        return super.getAdapter(key);
    }

    protected List getModelChildren() {
        List children = new ArrayList();
        
        if (getModel() instanceof IPropertyEnabled) {
            IPropertyEnabled properties = (IPropertyEnabled) getModel();
            if (properties.getProperties() != null) {
                children.addAll(properties.getProperties());
            }
        }
        if (getModel() instanceof IActionState) {
            if (((IActionState) getState()).getActions() != null) {
                children.addAll(((IActionState) getState()).getActions());
            }
        }
        else if (getModel() instanceof ISubFlowState) {
            if (((ISubFlowState) getModel()).getAttributeMapper() != null)
                children.add(((ISubFlowState) getModel()).getAttributeMapper());
        }
        else if (getModel() instanceof IWebFlowState) {
            if (((IWebFlowState) getState()).getStates() != null)
            children.addAll(((IWebFlowState) getState()).getStates());
        }
        else if (getModel() instanceof IDecisionState) {
            if (((IDecisionState) getModel()).getIfs() != null) {
                children.addAll(((IDecisionState) getModel()).getIfs());
            }
        }
        else if (getModel() instanceof IAttributeMapper) {
            if (((IAttributeMapper) getModel()).getInputs() != null) {
                children.addAll(((IAttributeMapper) getModel()).getInputs());
            }
            if (((IAttributeMapper) getModel()).getOutputs() != null) {
                children.addAll(((IAttributeMapper) getModel()).getOutputs());
            }
        }
        else if (getModel() instanceof IViewState) {
            if (((IViewState) getModel()).getSetup() != null) {
                children.add(((IViewState) getModel()).getSetup());
            }
        }
        
        return children;
    }

    protected IState getState() {
        return (IState) getModel();
    }

    public void propertyChange(PropertyChangeEvent change) {
        if (change.getPropertyName().equals(IWebFlowModelElement.ADD_CHILDREN)) {
            addChild(createChild(change.getNewValue()), ((Integer) change
                    .getOldValue()).intValue());
        }
        else if (change.getPropertyName().equals(
                IWebFlowModelElement.REMOVE_CHILDREN)) {
            // remove child
            removeChild((EditPart) getViewer().getEditPartRegistry().get(
                    change.getNewValue()));
        }
        else if (change.getPropertyName().equals(
                IWebFlowModelElement.MOVE_CHILDREN)) {
        }
        else {
            refreshVisuals();
        }
    }

    protected void refreshVisuals() {
        Image image = labelProvider.getImage(getModel());
        String text = tLabelProvider.getText(getModel(), true, false);
        if (image != null) {
            setWidgetImage(image);
        }
        setWidgetText(text);
    }
}