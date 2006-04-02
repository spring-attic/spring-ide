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

import org.eclipse.core.resources.IResource;
import org.springframework.ide.eclipse.web.flow.core.model.IAttributeMapper;
import org.springframework.ide.eclipse.web.flow.core.model.ICloneableModelElement;
import org.springframework.ide.eclipse.web.flow.core.model.IInputMapping;
import org.springframework.ide.eclipse.web.flow.core.model.IModelWriter;
import org.springframework.ide.eclipse.web.flow.core.model.IOutputMapping;
import org.springframework.ide.eclipse.web.flow.core.model.IPersistableModelElement;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowModelElement;

public class AttributeMapper extends AbstractModelElement implements
        IAttributeMapper, IPersistableModelElement, ICloneableModelElement {
    
    private List inputs;
    
    private List outputs;
    
    private String bean;
    
    public AttributeMapper(IWebFlowModelElement parent, String id) {
        super(parent, id);
        this.inputs = new ArrayList();
        this.outputs = new ArrayList();
    }

    public AttributeMapper() {
        this(null, null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ide.eclipse.web.flow.core.internal.model.WebFlowModelElement#getElementType()
     */
    public int getElementType() {
        return IWebFlowModelElement.ATTRIBUTEMAPPER;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ide.eclipse.web.flow.core.model.IWebFlowModelElement#getElementResource()
     */
    public IResource getElementResource() {
        return super.getElementParent().getElementResource();
    }

    /* (non-Javadoc)
     * 
     * @see org.springframework.ide.eclipse.web.flow.core.model.IConeableModelElement#cloneModelElement()
     */
    public ICloneableModelElement cloneModelElement() {
        AttributeMapper mapper = new AttributeMapper();
        mapper.setBean(getBean());
        for (int i = 0; i < this.getInputs().size(); i++) {
            Input input = (Input) this.getInputs().get(i);
            mapper.addInput((Input) input.cloneModelElement());
        }
        for (int i = 0; i < this.getOutputs().size(); i++) {
            Output input = (Output) this.getOutputs().get(i);
            mapper.addOutput((Output) input.cloneModelElement());
        }
        return mapper;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ide.eclipse.web.flow.core.model.IConeableModelElement#applyCloneValues(org.springframework.ide.eclipse.web.flow.core.model.IConeableModelElement)
     */
    public void applyCloneValues(ICloneableModelElement element) {
        if (element instanceof IAttributeMapper) {
            IAttributeMapper mapper = (IAttributeMapper) element;
            setBean(mapper.getBean());
            Input[] inputs = (Input[]) this.getInputs().toArray(
                    new Input[this.getInputs().size()]);
            for (int i = 0; i < inputs.length; i++) {
                removeInput(inputs[i]);
            }
            for (int i = 0; i < mapper.getInputs().size(); i++) {
                addInput((Input) mapper.getInputs().get(i));
            }
            Output[] props = (Output[]) this.getOutputs().toArray(
                    new Output[this.getOutputs().size()]);
            for (int i = 0; i < props.length; i++) {
                removeOutput(props[i]);
            }
            for (int i = 0; i < mapper.getOutputs().size(); i++) {
                addOutput((Output) mapper.getOutputs().get(i));
            }
        }
    }

    /* (non-Javadoc)
     * @see org.springframework.ide.eclipse.web.flow.core.model.IAttributeMapper#getInputs()
     */
    public List getInputs() {
        return this.inputs;
    }

    /* (non-Javadoc)
     * @see org.springframework.ide.eclipse.web.flow.core.model.IAttributeMapper#getOutputs()
     */
    public List getOutputs() {
        return this.outputs;
    }

    /* (non-Javadoc)
     * @see org.springframework.ide.eclipse.web.flow.core.model.IAttributeMapper#addInput(org.springframework.ide.eclipse.web.flow.core.model.IInput)
     */
    public void addInput(IInputMapping input) {
        if (!this.inputs.contains(input)) {
            input.setElementParent(this);
            this.inputs.add(input);
            super.firePropertyChange(ADD_CHILDREN, new Integer(this.inputs
                    .indexOf(input)), input);
        }
    }

    /* (non-Javadoc)
     * @see org.springframework.ide.eclipse.web.flow.core.model.IAttributeMapper#addOutput(org.springframework.ide.eclipse.web.flow.core.model.IOutput)
     */
    public void addOutput(IOutputMapping output) {
        if (!this.outputs.contains(output)) {
            output.setElementParent(this);
            this.outputs.add(output);
            super.firePropertyChange(ADD_CHILDREN, new Integer(this.outputs
                    .indexOf(output)), output);
        }
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ide.eclipse.web.flow.core.model.IPersistable#save(org.springframework.ide.eclipse.web.flow.core.model.IModelWriter)
     */
    public void save(IModelWriter writer) {
        writer.doStart(this);
        Iterator iter = this.inputs.iterator();
        while (iter.hasNext()) {
            ((IPersistableModelElement) iter.next()).save(writer);
        }
        iter = this.outputs.iterator();
        while (iter.hasNext()) {
            ((IPersistableModelElement) iter.next()).save(writer);
        }
        writer.doEnd(this);
    }

    /* (non-Javadoc)
     * @see org.springframework.ide.eclipse.web.flow.core.model.IAttributeMapper#removeInput(org.springframework.ide.eclipse.web.flow.core.model.IInput)
     */
    public void removeInput(IInputMapping input) {
        if (this.inputs.contains(input)) {
            this.inputs.remove(input);
            super.fireStructureChange(REMOVE_CHILDREN, input);
        }
    }

    /* (non-Javadoc)
     * @see org.springframework.ide.eclipse.web.flow.core.model.IAttributeMapper#removeOutput(org.springframework.ide.eclipse.web.flow.core.model.IOutput)
     */
    public void removeOutput(IOutputMapping output) {
        if (this.outputs.contains(output)) {
            this.outputs.remove(output);
            super.fireStructureChange(REMOVE_CHILDREN, output);
        }
        
    }

    /* (non-Javadoc)
     * @see org.springframework.ide.eclipse.web.flow.core.model.IAttributeMapper#addInput(org.springframework.ide.eclipse.web.flow.core.model.IInput, int)
     */
    public void addInput(IInputMapping input, int index) {
        if (!this.inputs.contains(input)) {
            input.setElementParent(this);
            this.inputs.add(index, input);
            super.firePropertyChange(ADD_CHILDREN, new Integer(this.inputs
                    .indexOf(input)), input);
        }
    }

    /* (non-Javadoc)
     * @see org.springframework.ide.eclipse.web.flow.core.model.IAttributeMapper#addOutput(org.springframework.ide.eclipse.web.flow.core.model.IOutput, int)
     */
    public void addOutput(IOutputMapping output, int index) {
        if (!this.outputs.contains(output)) {
            output.setElementParent(this);
            this.outputs.add(index, output);
            super.firePropertyChange(ADD_CHILDREN, new Integer(this.outputs
                    .indexOf(output)), output);
        }        
    }
    
    /**
     * @param bean
     *            The bean to set.
     */
    public void setBean(String bean) {
        String oldValue = this.bean;
        this.bean = bean;
        super.firePropertyChange(PROPS, oldValue, bean);
    }
    
    public String getBean() {
        return this.bean;
    }

    public boolean hasBeanReference() {
        // TODO Auto-generated method stub
        return (this.bean != null);
    }

    public void setMethod(String method) {
    }

    public String getMethod() {
        return null;
    }
}