/*
 * Copyright 2004 DekaBank Deutsche Girozentrale. All rights reserved.
 */
package org.springframework.ide.eclipse.web.flow.core.internal.model;

import java.util.ArrayList;
import java.util.List;

import org.springframework.ide.eclipse.web.flow.core.model.IProperty;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowModelElement;

public abstract class AbstractModelElement extends WebFlowModelElement {

    protected String autowire;

    protected String bean;

    protected String beanClass;

    protected String classRef;

    protected String method;

    protected List properties = new ArrayList();

    protected AbstractModelElement(IWebFlowModelElement parent, String name) {
        super(parent, name);
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
     * @see org.springframework.ide.eclipse.web.flow.core.model.IAction#addProperty(java.lang.String,
     *      java.lang.String)
     */
    public void addProperty(String name, String value) {
        IProperty property = new Property(this, name, value);
    }

    /**
     * @return Returns the autowire.
     */
    public String getAutowire() {
        return autowire;
    }

    /**
     * @return Returns the bean.
     */
    public String getBean() {
        return bean;
    }

    /**
     * @return Returns the beanClass.
     */
    public String getBeanClass() {
        return beanClass;
    }

    /**
     * @return Returns the classRef.
     */
    public String getClassRef() {
        return classRef;
    }

    /**
     * @return Returns the method.
     */
    public String getMethod() {
        return method;
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

    /**
     * @param autowire
     *            The autowire to set.
     */
    public void setAutowire(String autowire) {
        String oldValue = this.autowire;
        this.autowire = autowire;
        super.firePropertyChange(PROPS, oldValue, autowire);
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

    /**
     * @param beanClass
     *            The beanClass to set.
     */
    public void setBeanClass(String beanClass) {
        String oldValue = this.beanClass;
        this.beanClass = beanClass;
        super.firePropertyChange(PROPS, oldValue, beanClass);
    }

    /**
     * @param classRef
     *            The classRef to set.
     */
    public void setClassRef(String classRef) {
        String oldValue = this.classRef;
        this.classRef = classRef;
        super.firePropertyChange(PROPS, oldValue, classRef);
    }

    /**
     * @param method
     *            The method to set.
     */
    public void setMethod(String method) {
        String oldValue = this.method;
        this.method = method;
        super.firePropertyChange(PROPS, oldValue, method);
    }

    public boolean hasBeanReference() {
        return (this.bean != null || this.beanClass != null
                || this.classRef != null || this.autowire != null || this.method != null);
    }
}
