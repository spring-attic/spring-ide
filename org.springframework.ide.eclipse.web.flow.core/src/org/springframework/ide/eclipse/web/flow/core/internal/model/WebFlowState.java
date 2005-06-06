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
import java.util.Iterator;
import java.util.List;

import org.springframework.ide.eclipse.web.flow.core.model.ICloneableModelElement;
import org.springframework.ide.eclipse.web.flow.core.model.IModelElementVisitor;
import org.springframework.ide.eclipse.web.flow.core.model.IModelWriter;
import org.springframework.ide.eclipse.web.flow.core.model.IPersistableModelElement;
import org.springframework.ide.eclipse.web.flow.core.model.IProperty;
import org.springframework.ide.eclipse.web.flow.core.model.IState;
import org.springframework.ide.eclipse.web.flow.core.model.ISubFlowState;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowModelElement;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowState;

public class WebFlowState extends AbstractTransitionableFrom implements
        IWebFlowState, IPersistableModelElement, ICloneableModelElement {

    private List states;

    private IState startState;

    public WebFlowState(IWebFlowModelElement parent, String id) {
        super(parent, id);
        this.states = new ArrayList();
        if (parent instanceof ISubFlowState) {
            ((ISubFlowState) parent).addState(this);
        }
    }
    
    public WebFlowState() {
        this(null, null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ide.eclipse.web.core.model.ISubFlowState#addChildState(org.springframework.ide.eclipse.web.core.model.IState)
     */
    public void addState(IState state) {
        if (!getStates().contains(state)) {
            this.getStates().add(state);
            super.firePropertyChange(ADD_CHILDREN, new Integer(this.getStates()
                    .indexOf(state)), state);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ide.eclipse.web.core.model.ISubFlowState#removeChildState(org.springframework.ide.eclipse.web.core.model.IState)
     */
    public void removeState(IState state) {
        if (getStates().contains(state)) {
            this.getStates().remove(state);
            super.fireStructureChange(REMOVE_CHILDREN, state);
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ide.eclipse.web.core.model.ISubFlowState#addChildState(org.springframework.ide.eclipse.web.core.model.IState,
     *      int)
     */
    public void addState(IState state, int i) {
        if (!getStates().contains(state)) {
            this.getStates().add(i, state);
            super.firePropertyChange(ADD_CHILDREN, new Integer(i), state);
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ide.eclipse.web.flow.core.internal.model.WebFlowModelElement#getElementType()
     */
    public int getElementType() {
        return IWebFlowModelElement.WEBFLOW_STATE;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ide.eclipse.web.flow.core.model.ISubFlowState#getStates()
     */
    public List getStates() {
        return this.states;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ide.eclipse.web.flow.core.model.ISubFlowState#getStartState()
     */
    public IState getStartState() {
        return this.startState;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ide.eclipse.web.flow.core.model.ISubFlowState#setStartState(org.springframework.ide.eclipse.web.flow.core.model.IState)
     */
    public void setStartState(IState state) {
        IState oldValue = this.startState;
        if (hasStartState()) {
            oldValue.setStartState(false);
        }
        this.startState = state;
        this.removeState(state);
        this.addState(state, 0);
        this.startState.setStartState(true);
        //this.getStates().remove(state);
        //this.getStates().add(0, state);
        //super.firePropertyChange(MOVE_CHILDREN, oldValue, this.startState);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ide.eclipse.web.flow.core.model.ISubFlowState#hasStartState()
     */
    public boolean hasStartState() {
        return this.startState != null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ide.eclipse.web.flow.core.model.IWebFlowState#moveState(org.springframework.ide.eclipse.web.flow.core.model.IState,
     *      int)
     */
    public void moveState(IState state, int i) {
        int j = this.getStates().indexOf(state);
        if (j != -1) {
            if ((i < 0) || (i > this.getStates().size() - 1)) {
                return;
            }
            this.getStates().remove(state);
            this.getStates().add(i, state);
            super.firePropertyChange(MOVE_CHILDREN, new Integer(i), state);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ide.eclipse.web.flow.core.model.IWebFlowState#isStartState(org.springframework.ide.eclipse.web.flow.core.model.IState)
     */
    public boolean isStartState(IState state) {
        if (state == null || this.startState == null || state.getId() == null) {
            return false;
        }
        return state.getId().equals(this.startState.getId());
    }

    public void accept(IModelElementVisitor visitor) {
        if (visitor.visit(this)) {
            Iterator iter = this.getStates().iterator();
            while (iter.hasNext()) {
                IWebFlowModelElement element = (IWebFlowModelElement) iter
                        .next();
                element.accept(visitor);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ide.eclipse.web.flow.core.model.IPersistable#save(org.springframework.ide.eclipse.web.flow.core.model.IModelWriter)
     */
    public void save(IModelWriter writer) {
        writer.doStart(this);
        Iterator iter = this.getStates().iterator();
        while (iter.hasNext()) {
            IWebFlowModelElement element = (IWebFlowModelElement) iter.next();
            if (element instanceof IPersistableModelElement) {
                ((IPersistableModelElement) element).save(writer);
            }
        }
        writer.doEnd(this);
    }

    /* (non-Javadoc)
     * @see org.springframework.ide.eclipse.web.flow.core.model.ICloneableModelElement#cloneModelElement()
     */
    public ICloneableModelElement cloneModelElement() {
        WebFlowState state = new WebFlowState();
        state.setId(getId());        
        state.setAutowire(getAutowire());
        state.setBean(getBean());
        state.setBeanClass(getBeanClass());
        state.setClassRef(getClassRef());
        //state.setStartState((IState) ((ICloneableModelElement) this.startState));
        state.setElementName(getElementName());
        for (int i = 0; i < super.getProperties().size(); i++) {
            Property property = (Property) super.getProperties().get(i);
            state.addProperty((IProperty) property.cloneModelElement());
        }
        return state;
    }

    /* (non-Javadoc)
     * @see org.springframework.ide.eclipse.web.flow.core.model.ICloneableModelElement#applyCloneValues(org.springframework.ide.eclipse.web.flow.core.model.ICloneableModelElement)
     */
    public void applyCloneValues(ICloneableModelElement element) {
        if (element instanceof WebFlowState) {
            WebFlowState state = (WebFlowState) element;
            this.id = state.getId();        
            this.autowire = state.getAutowire();
            this.bean = state.getBean();
            this.beanClass = state.getBeanClass();
            this.classRef = state.getClassRef();
            Property[] props = (Property[]) this.getProperties().toArray(
                    new Property[this.getProperties().size()]);
            for (int i = 0; i < props.length; i++) {
                this.properties.remove(props[i]);
            }
            for (int i = 0; i < state.getProperties().size(); i++) {
                this.properties.add((IProperty) state.getProperties().get(i));
            }
        }
    }
}