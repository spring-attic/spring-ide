/*
 * Copyright 2004 DekaBank Deutsche Girozentrale. All rights reserved.
 */
package org.springframework.ide.eclipse.web.flow.core.internal.model;

import java.util.ArrayList;
import java.util.List;

import org.springframework.ide.eclipse.web.flow.core.model.IAttribute;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowModelElement;

public abstract class AbstractModelElement extends WebFlowModelElement {

    protected List properties = new ArrayList();

    protected AbstractModelElement(IWebFlowModelElement parent, String name) {
        super(parent, name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ide.eclipse.web.flow.core.model.IAction#addProperty(org.springframework.ide.eclipse.web.flow.core.model.IProperty)
     */
    public void addProperty(IAttribute property) {
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
    public void addProperty(IAttribute property, int index) {
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
     * @see org.springframework.ide.eclipse.web.flow.core.model.IAction#addProperty(java.lang.String,
     *      java.lang.String)
     */
    public void addProperty(String name, String value) {
        IAttribute property = new Property(this, name, value);
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
    public void removeProperty(IAttribute property) {
        if (this.properties.contains(property)) {
            this.properties.remove(property);
            super.fireStructureChange(REMOVE_CHILDREN, property);
        }
    }
}
