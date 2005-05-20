/*
 * Copyright 2002-2005 the original author or authors.
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

package org.springframework.ide.eclipse.web.flow.core.internal.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.springframework.ide.eclipse.web.flow.core.model.IAction;
import org.springframework.ide.eclipse.web.flow.core.model.IPersistableModelElement;
import org.springframework.ide.eclipse.web.flow.core.model.IProperty;
import org.springframework.ide.eclipse.web.flow.core.model.IState;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowConfig;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowModelElement;

public abstract class AbstractState extends WebFlowModelElement implements
        IState, IPersistableModelElement {

    private boolean isStartState = false;

    private String id;

    private List properties = new ArrayList();

    public AbstractState(IWebFlowModelElement parent, String id) {
        super(parent, id);
        this.id = id;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ide.eclipse.web.flow.core.model.IAction#addProperty(org.springframework.ide.eclipse.web.flow.core.model.IProperty)
     */
    public void addProperty(IProperty property) {
        if (!this.properties.contains(property)) {
            property.setElementParent(this);
            this.properties.add(property);
            super.firePropertyChange(ADD_CHILDREN, new Integer(this.properties
                    .indexOf(property)), property);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ide.eclipse.web.flow.core.model.IAction#addProperty(org.springframework.ide.eclipse.web.flow.core.model.IProperty)
     */
    public void addProperty(IProperty property, int index) {
        if (!this.properties.contains(property)) {
            property.setElementParent(this);
            this.properties.add(index, property);
            super
                    .firePropertyChange(ADD_CHILDREN, new Integer(index),
                            property);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ide.eclipse.web.flow.core.model.IAction#getProperties()
     */
    public List getProperties() {
        return this.properties;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ide.eclipse.web.flow.core.model.IAction#removeProperty(org.springframework.ide.eclipse.web.flow.core.model.IProperty)
     */
    public void removeProperty(IProperty property) {
        if (this.properties.contains(property)) {
            this.properties.remove(property);
            super.fireStructureChange(REMOVE_CHILDREN, property);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ide.eclipse.web.flow.core.model.IAction#addProperty(java.lang.String,
     *      java.lang.String)
     */
    public void addProperty(String name, String value) {
        IProperty property = new Property(this, name, value);
    }

    /**
     * @return Returns the isStartState.
     */
    public boolean isStartState() {
        return isStartState;
    }

    public void setStartState(boolean startState) {
        boolean oldValue = isStartState;
        isStartState = startState;
        super.firePropertyChange(PROPS, new Boolean(oldValue), new Boolean(
                isStartState));
    }

    /**
     * @return Returns the id.
     */
    public String getId() {
        return id;
    }

    /**
     * @param id
     *            The id to set.
     */
    public void setId(String id) {
        String oldValue = this.id;
        this.id = id;
        super.setElementName(id);
        super.firePropertyChange(PROPS, oldValue, id);
    }

    public IWebFlowConfig getConfig() {
        IWebFlowModelElement parent = getElementParent();
        if (parent instanceof IWebFlowConfig) {
            return (IWebFlowConfig) parent;
        }
        else if (parent instanceof IState) {
            return ((IState) parent).getConfig();
        }
        else if (parent instanceof IAction) {
            return ((IState) parent.getElementParent()).getConfig();
        }
        throw new IllegalStateException(
                "State can only have a parent of type "
                        + "IWebFlowConfig, IState or (in case of an inner bean) IBeanProperty");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ide.eclipse.web.flow.core.model.IWebFlowModelElement#getElementResource()
     */
    public IResource getElementResource() {
        return (super.getElementParent() != null ? super.getElementParent()
                .getElementResource() : null);
    }

    public String getParentName() {
        return (super.getElementParent() != null ? super.getElementParent()
                .getElementName() : null);
    }
}