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
import org.springframework.ide.eclipse.web.flow.core.model.IActionState;
import org.springframework.ide.eclipse.web.flow.core.model.ICloneableModelElement;
import org.springframework.ide.eclipse.web.flow.core.model.IModelElementVisitor;
import org.springframework.ide.eclipse.web.flow.core.model.IModelWriter;
import org.springframework.ide.eclipse.web.flow.core.model.IPersistableModelElement;
import org.springframework.ide.eclipse.web.flow.core.model.IProperty;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowModelElement;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowState;

public class ActionState extends AbstractTransitionableFrom implements
        IActionState, IPersistableModelElement, ICloneableModelElement {

    List actions = null;

    public ActionState(IWebFlowModelElement parent, String id, List actions) {
        super(parent, id);
        if (actions != null)
            this.actions = actions;
        else
            this.actions = new ArrayList();
        if (parent instanceof IWebFlowState) {
            ((IWebFlowState) parent).addState(this);
        }
    }

    public ActionState() {
        super(null, null);
        this.actions = new ArrayList();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ide.eclipse.web.core.model.IActionState#getActions()
     */
    public List getActions() {
        return this.actions;
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
     * @see org.springframework.ide.eclipse.web.core.model.IActionState#removeAction(org.springframework.ide.eclipse.web.core.model.IAction)
     */
    public void removeAction(IAction action) {
        this.actions.remove(action);
        super.fireStructureChange(REMOVE_CHILDREN, action);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ide.eclipse.web.flow.core.internal.model.WebFlowModelElement#getElementType()
     */
    public int getElementType() {
        return IWebFlowModelElement.ACTION_STATE;
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
     * @see org.springframework.ide.eclipse.web.flow.core.model.IWebFlowModelElement#accept(org.springframework.ide.eclipse.web.flow.core.model.IModelElementVisitor)
     */
    public void accept(IModelElementVisitor visitor) {
        if (visitor.visit(this)) {
            Iterator iter = this.getActions().iterator();
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
        Iterator iter = this.getActions().iterator();
        while (iter.hasNext()) {
            IWebFlowModelElement element = (IWebFlowModelElement) iter.next();
            if (element instanceof IPersistableModelElement) {
                ((IPersistableModelElement) element).save(writer);
            }
        }
        iter = this.getProperties().iterator();
        while (iter.hasNext()) {
            IWebFlowModelElement element = (IWebFlowModelElement) iter.next();
            if (element instanceof IPersistableModelElement) {
                ((IPersistableModelElement) element).save(writer);
            }
        }
        super.save(writer);
        writer.doEnd(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ide.eclipse.web.flow.core.model.ICloneableModelElement#cloneModelElement()
     */
    public ICloneableModelElement cloneModelElement() {
        ActionState state = new ActionState();
        state.setId(getId());
        state.setElementName(getElementName());
        for (int i = 0; i < this.getActions().size(); i++) {
            state.addAction((IAction) ((ICloneableModelElement) this
                    .getActions().get(i)).cloneModelElement());
        }
        for (int i = 0; i < this.getProperties().size(); i++) {
            Property property = (Property) this.getProperties().get(i);
            state.addProperty((IProperty) property.cloneModelElement());
        }
        return state;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ide.eclipse.web.flow.core.model.ICloneableModelElement#applyCloneValues(org.springframework.ide.eclipse.web.flow.core.model.ICloneableModelElement)
     */
    public void applyCloneValues(ICloneableModelElement element) {
        if (element instanceof IActionState) {
            ActionState action = (ActionState) element;
            setId(action.getId());
            Action[] actions = (Action[]) this.getActions().toArray(
                    new Action[this.getActions().size()]);
            for (int i = 0; i < actions.length; i++) {
                removeAction(actions[i]);
            }
            for (int i = 0; i < action.getActions().size(); i++) {
                addAction((IAction) action.getActions().get(i));
            }
            Property[] props = (Property[]) this.getProperties().toArray(
                    new Property[this.getProperties().size()]);
            for (int i = 0; i < props.length; i++) {
                removeProperty(props[i]);
            }
            for (int i = 0; i < action.getProperties().size(); i++) {
                addProperty((IProperty) action.getProperties().get(i));
            }
        }
    }
}