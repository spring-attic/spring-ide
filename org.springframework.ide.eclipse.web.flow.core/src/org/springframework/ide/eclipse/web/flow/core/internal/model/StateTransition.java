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

import org.springframework.ide.eclipse.web.flow.core.model.IAction;
import org.springframework.ide.eclipse.web.flow.core.model.ICloneableModelElement;
import org.springframework.ide.eclipse.web.flow.core.model.IModelWriter;
import org.springframework.ide.eclipse.web.flow.core.model.IPersistableModelElement;
import org.springframework.ide.eclipse.web.flow.core.model.IStateTransition;
import org.springframework.ide.eclipse.web.flow.core.model.ITransitionableFrom;
import org.springframework.ide.eclipse.web.flow.core.model.ITransitionableTo;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowModelElement;

public class StateTransition extends Transition implements IStateTransition,
        IPersistableModelElement, ICloneableModelElement {

    private List actions;

    private ITransitionableFrom fromState;

    private String on;

    public StateTransition() {
        this.actions = new ArrayList();
    }

    public StateTransition(ITransitionableTo to, ITransitionableFrom from,
            String on) {
        super(to);
        this.on = on;
        this.actions = new ArrayList();

        this.fromState = from;
        this.fromState.addOutputTransition(this);
    }

    public StateTransition(String to, ITransitionableFrom from, String on) {
        super(to);
        this.on = on;
        this.actions = new ArrayList();

        this.fromState = from;
        this.fromState.addOutputTransition(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ide.eclipse.web.core.model.IActionState#addAction(org.springframework.ide.eclipse.web.core.model.IAction)
     */
    public void addAction(IAction action) {
        if (!this.actions.contains(action)) {
            action.setElementParent(this);
            this.actions.add(action);
            super.firePropertyChange(ADD_CHILDREN, new Integer(this.actions
                    .indexOf(action)), action);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ide.eclipse.web.flow.core.model.IActionState#addAction(org.springframework.ide.eclipse.web.flow.core.model.IAction,
     *      int)
     */
    public void addAction(IAction action, int i) {
        if (!this.actions.contains(action)) {
            this.actions.add(i, action);
            super.firePropertyChange(ADD_CHILDREN, new Integer(i), action);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ide.eclipse.web.core.model.IActionState#getActions()
     */
    public List getActions() {
        if (this.actions == null)
            this.actions = new ArrayList();
        return this.actions;
    }

    /**
     * @return Returns the on.
     */
    public String getOn() {
        return on;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ide.eclipse.web.core.model.IActionState#removeAction(org.springframework.ide.eclipse.web.core.model.IAction)
     */
    public void removeAction(IAction action) {
        this.actions.remove(action);
        super.fireStructureChange(REMOVE_CHILDREN, action);
    }

    /**
     * @param on
     *            The on to set.
     */
    public void setOn(String on) {
        String oldValue = this.on;
        this.on = on;
        super.firePropertyChange(PROPS, oldValue, on);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ide.eclipse.web.flow.core.model.IPersistable#save(org.springframework.ide.eclipse.web.flow.core.model.IModelWriter)
     */
    public void save(IModelWriter writer) {
        writer.doStart(this);
        Iterator iter = this.getActions().iterator();
        while (iter.hasNext()) {
            IWebFlowModelElement element = (IWebFlowModelElement) iter.next();
            if (element instanceof IPersistableModelElement) {
                ((IPersistableModelElement) element).save(writer);
            }
        }
        writer.doEnd(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ide.eclipse.web.flow.core.internal.model.WebFlowModelElement#getElementType()
     */
    public int getElementType() {
        return IWebFlowModelElement.STATE_TRANSITION;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ide.eclipse.web.flow.core.model.ITransition#getFromState()
     */
    public ITransitionableFrom getFromState() {
        return this.fromState;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ide.eclipse.web.flow.core.model.ITransition#setFromState(org.springframework.ide.eclipse.web.flow.core.model.ITransitionableFrom)
     */
    public void setFromState(ITransitionableFrom formState) {
        //      if (this.fromState != null) {
        //    this.fromState.removeOutputTransition(this);
        //}
        this.fromState = formState;
        //super.fireStructureChange(INPUTS, state);
        //if (this.fromState != null) {
        //    this.fromState.addOutputTransition(this);
        //}
    }

    /* (non-Javadoc)
     * @see org.springframework.ide.eclipse.web.flow.core.model.ICloneableModelElement#cloneModelElement()
     */
    public ICloneableModelElement cloneModelElement() {
        StateTransition transition = new StateTransition();
        transition.setOn(getOn());
        transition.setElementName(getElementName());
        for (int i = 0; i < this.getActions().size(); i++) {
            transition.addAction((IAction) ((ICloneableModelElement) this
                    .getActions().get(i)).cloneModelElement());
        }
        return transition;
    }

    /* (non-Javadoc)
     * @see org.springframework.ide.eclipse.web.flow.core.model.ICloneableModelElement#applyCloneValues(org.springframework.ide.eclipse.web.flow.core.model.ICloneableModelElement)
     */
    public void applyCloneValues(ICloneableModelElement element) {
        if (element instanceof StateTransition) {
            StateTransition transition = (StateTransition) element;
            setOn(transition.getOn());
            Action[] actions = (Action[]) this.getActions().toArray(
                    new Action[this.getActions().size()]);
            for (int i = 0; i < actions.length; i++) {
                removeAction(actions[i]);
            }
            for (int i = 0; i < transition.getActions().size(); i++) {
                addAction((IAction) transition.getActions().get(i));
            }
        }
    }
}