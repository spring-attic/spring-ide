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

import java.util.Iterator;

import org.springframework.ide.eclipse.web.flow.core.model.ICloneableModelElement;
import org.springframework.ide.eclipse.web.flow.core.model.IEndState;
import org.springframework.ide.eclipse.web.flow.core.model.IModelWriter;
import org.springframework.ide.eclipse.web.flow.core.model.IPersistableModelElement;
import org.springframework.ide.eclipse.web.flow.core.model.IProperty;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowModelElement;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowState;

public class EndState extends AbstractTransitionableTo implements IEndState,
        IPersistableModelElement, ICloneableModelElement {

    String view;

    public EndState(IWebFlowModelElement parent, String id, String viewName) {
        super(parent, id);
        this.view = viewName;
        if (parent instanceof IWebFlowState) {
            ((IWebFlowState) parent).addState(this);
        }
    }

    public EndState(IWebFlowModelElement parent, String id) {
        this(parent, id, null);
    }

    public EndState() {
        this(null, null);
    }

    /**
     * @return Returns the view.
     */
    public String getView() {
        return view;
    }

    /**
     * @param view
     *            The view to set.
     */
    public void setView(String view) {
        String oldValue = this.view;
        this.view = view;
        super.firePropertyChange(PROPS, oldValue, this.view);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ide.eclipse.web.flow.core.internal.model.WebFlowModelElement#getElementType()
     */
    public int getElementType() {
        return IWebFlowModelElement.END_STATE;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ide.eclipse.web.flow.core.model.IConeableModelElement#cloneModelElement()
     */
    public ICloneableModelElement cloneModelElement() {
        EndState state = new EndState();
        state.setId(getId());
        state.setView(getView());
        state.setAutowire(getAutowire());
        state.setBean(getBean());
        state.setBeanClass(getBeanClass());
        state.setClassRef(getClassRef());
        state.setElementName(getElementName());
        state.setDescription(getDescription());
        for (int i = 0; i < this.getProperties().size(); i++) {
            Property property = (Property) this.getProperties().get(i);
            state.addProperty((IProperty) property.cloneModelElement());
        }
        return state;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ide.eclipse.web.flow.core.model.IConeableModelElement#applyCloneValues(org.springframework.ide.eclipse.web.flow.core.model.IConeableModelElement)
     */
    public void applyCloneValues(ICloneableModelElement element) {
        if (element instanceof EndState) {
            EndState state = (EndState) element;
            setView(state.getView());
            setId(state.getId());
            setAutowire(state.getAutowire());
            setBean(state.getBean());
            setBeanClass(state.getBeanClass());
            setClassRef(state.getClassRef());
            setDescription(state.getDescription());
            Property[] props = (Property[]) this.getProperties().toArray(
                    new Property[this.getProperties().size()]);
            for (int i = 0; i < props.length; i++) {
                removeProperty(props[i]);
            }
            for (int i = 0; i < state.getProperties().size(); i++) {
                addProperty((IProperty) state.getProperties().get(i));
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
        Iterator iter = this.getProperties().iterator();
        while (iter.hasNext()) {
            IWebFlowModelElement element = (IWebFlowModelElement) iter.next();
            if (element instanceof IPersistableModelElement) {
                ((IPersistableModelElement) element).save(writer);
            }
        }
        writer.doEnd(this);
    }
}