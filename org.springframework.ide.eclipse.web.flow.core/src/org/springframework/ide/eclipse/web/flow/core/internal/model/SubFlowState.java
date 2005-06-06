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

import org.springframework.ide.eclipse.web.flow.core.model.IAttributeMapper;
import org.springframework.ide.eclipse.web.flow.core.model.ICloneableModelElement;
import org.springframework.ide.eclipse.web.flow.core.model.IModelWriter;
import org.springframework.ide.eclipse.web.flow.core.model.IPersistableModelElement;
import org.springframework.ide.eclipse.web.flow.core.model.IProperty;
import org.springframework.ide.eclipse.web.flow.core.model.ISubFlowState;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowModelElement;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowState;

public class SubFlowState extends WebFlowState implements ISubFlowState,
        IPersistableModelElement, ICloneableModelElement {

    private String flow;

    private IAttributeMapper attributeMapper;

    public SubFlowState(IWebFlowModelElement parent, String id, String subFlow,
            IAttributeMapper mapper) {
        super(parent, id);
        this.flow = subFlow;
        this.attributeMapper = mapper;
        if (parent instanceof IWebFlowState) {
            ((IWebFlowState) parent).addState(this);
        }
    }

    public SubFlowState(IWebFlowModelElement parent, String id, String subFlow) {
        super(parent, id);
        this.flow = subFlow;
        if (parent instanceof IWebFlowState) {
            ((IWebFlowState) parent).addState(this);
        }
    }

    public SubFlowState() {
        super(null, null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ide.eclipse.web.core.model.ISubFlowState#getAttributeMapper()
     */
    public IAttributeMapper getAttributeMapper() {
        return this.attributeMapper;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ide.eclipse.web.core.model.ISubFlowState#setAttributeMapper(java.lang.String)
     */
    public void setAttributeMapper(IAttributeMapper attributeMapper) {
        IAttributeMapper oldValue = this.attributeMapper;
        this.attributeMapper = attributeMapper;
        super.firePropertyChange(ADD_CHILDREN, new Integer(0), attributeMapper);
    }

    public void removeAttributeMapper() {
        IAttributeMapper oldValue = this.attributeMapper;
        this.attributeMapper = null;
        super.firePropertyChange(REMOVE_CHILDREN, attributeMapper, oldValue);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ide.eclipse.web.flow.core.internal.model.WebFlowModelElement#getElementType()
     */
    public int getElementType() {
        return IWebFlowModelElement.SUBFLOW_STATE;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ide.eclipse.web.flow.core.model.IPersistable#save(org.springframework.ide.eclipse.web.flow.core.model.IModelWriter)
     */
    public void save(IModelWriter writer) {
        writer.doStart(this);
        if (this.attributeMapper != null
                && this.attributeMapper instanceof IPersistableModelElement) {
            ((IPersistableModelElement) this.attributeMapper).save(writer);
        }
        Iterator iter = super.getOutputTransitions().iterator();
        while (iter.hasNext()) {
            IPersistableModelElement element = (IPersistableModelElement) iter
                    .next();
            element.save(writer);
        }

        iter = this.getProperties().iterator();
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
     * @see org.springframework.ide.eclipse.web.flow.core.model.ISubFlowState#setFlow(java.lang.String)
     */
    public void setFlow(String flow) {
        String oldValue = this.flow;
        this.flow = flow;
        super.firePropertyChange(PROPS, flow, oldValue);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ide.eclipse.web.flow.core.model.ISubFlowState#getFlow()
     */
    public String getFlow() {
        return this.flow;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ide.eclipse.web.flow.core.model.IConeableModelElement#cloneModelElement()
     */
    public ICloneableModelElement cloneModelElement() {
        SubFlowState state = new SubFlowState();
        state.setId(getId());
        state.setFlow(getFlow());
        state.setAutowire(getAutowire());
        state.setBean(getBean());
        state.setBeanClass(getBeanClass());
        state.setClassRef(getClassRef());
        state.setElementName(getElementName());
        if (this.attributeMapper != null) {
            state
                    .setAttributeMapper((IAttributeMapper) ((ICloneableModelElement) this.attributeMapper)
                            .cloneModelElement());
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
     * @see org.springframework.ide.eclipse.web.flow.core.model.IConeableModelElement#applyCloneValues(org.springframework.ide.eclipse.web.flow.core.model.IConeableModelElement)
     */
    public void applyCloneValues(ICloneableModelElement element) {
        if (element instanceof ISubFlowState) {
            ISubFlowState state = (ISubFlowState) element;
            setId(state.getId());
            setFlow(state.getFlow());
            setAutowire(state.getAutowire());
            setBean(state.getBean());
            setBeanClass(state.getBeanClass());
            setClassRef(state.getClassRef());
            if (state.getAttributeMapper() != null) {
                if (this.attributeMapper != null) {
                    ((ICloneableModelElement) this.attributeMapper)
                            .applyCloneValues((ICloneableModelElement) state
                                    .getAttributeMapper());
                }
                else {
                    setAttributeMapper(state.getAttributeMapper());
                    getAttributeMapper().setElementParent(this);
                }
            }
            else {
                if (this.attributeMapper != null) {
                    removeAttributeMapper();
                }
            }
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
}