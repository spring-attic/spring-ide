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

import org.eclipse.core.resources.IResource;
import org.springframework.ide.eclipse.web.flow.core.model.ICloneableModelElement;
import org.springframework.ide.eclipse.web.flow.core.model.IPersistableModelElement;
import org.springframework.ide.eclipse.web.flow.core.model.IProperty;
import org.springframework.ide.eclipse.web.flow.core.model.IState;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowModelElement;

public class Property extends WebFlowModelElement implements IProperty,
        IPersistableModelElement, ICloneableModelElement {

    private String name;

    private String value;

    public Property(IWebFlowModelElement parent, String name, String value) {
        super(parent, name);
        this.value = value;
        this.name = name;
        if (parent instanceof Action) {
            ((Action) parent).addProperty(this);
        }
        else if (parent instanceof IState) {
            ((IState) parent).addProperty(this);
        }
    }

    public Property() {
        super(null, null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ide.eclipse.web.flow.core.model.IWebFlowModelElement#getElementType()
     */
    public int getElementType() {
        return IWebFlowModelElement.PROPERTY;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ide.eclipse.web.flow.core.model.IProperty#setName()
     */
    public void setName(String name) {
        String oldValue = this.name;
        this.name = name;
        super.firePropertyChange(PROPS, oldValue, name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ide.eclipse.web.flow.core.model.IProperty#setValue()
     */
    public void setValue(String value) {
        String oldValue = this.name;
        this.value = value;
        super.firePropertyChange(PROPS, oldValue, value);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ide.eclipse.web.flow.core.model.IProperty#getName()
     */
    public String getName() {
        return this.name;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ide.eclipse.web.flow.core.model.IProperty#getValue()
     */
    public String getValue() {
        return this.value;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ide.eclipse.web.flow.core.model.IWebFlowModelElement#getElementResource()
     */
    public IResource getElementResource() {
        return super.getElementParent().getElementResource();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ide.eclipse.web.flow.core.model.IConeableModelElement#cloneModelElement()
     */
    public ICloneableModelElement cloneModelElement() {
        Property property = new Property();
        property.setName(getName());
        property.setValue(getValue());
        return property;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ide.eclipse.web.flow.core.model.IConeableModelElement#applyCloneValues(org.springframework.ide.eclipse.web.flow.core.model.IConeableModelElement)
     */
    public void applyCloneValues(ICloneableModelElement element) {
        if (element instanceof Property) {
            Property property = (Property) element;
            setName(property.getName());
            setValue(property.getValue());
        }
    }
}