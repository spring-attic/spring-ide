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
import org.springframework.ide.eclipse.web.flow.core.model.IAction;
import org.springframework.ide.eclipse.web.flow.core.model.ICloneableModelElement;
import org.springframework.ide.eclipse.web.flow.core.model.IModelElementVisitor;
import org.springframework.ide.eclipse.web.flow.core.model.IModelWriter;
import org.springframework.ide.eclipse.web.flow.core.model.IPersistableModelElement;
import org.springframework.ide.eclipse.web.flow.core.model.IProperty;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowModelElement;

public class Action extends WebFlowModelElement implements IAction,
        IPersistableModelElement, ICloneableModelElement {

    private String autowire;

    private String bean;

    private String beanClass;

    private String classRef;

    private String method;

    private String name;

    private List properties = new ArrayList();

    public Action() {
        super(null, null);
    }

    public Action(IWebFlowModelElement parent, String id) {
        super(parent, id);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ide.eclipse.web.flow.core.model.IWebFlowModelElement#accept(org.springframework.ide.eclipse.web.flow.core.model.IModelElementVisitor)
     */
    public void accept(IModelElementVisitor visitor) {
        if (visitor.visit(this)) {
            Iterator iter = this.getProperties().iterator();
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

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ide.eclipse.web.flow.core.model.IConeableModelElement#applyCloneValues(org.springframework.ide.eclipse.web.flow.core.model.IConeableModelElement)
     */
    public void applyCloneValues(ICloneableModelElement element) {
        if (element instanceof IAction) {
            Action action = (Action) element;
            setAutowire(action.getAutowire());
            setBean(action.getBean());
            setBeanClass(action.getBeanClass());
            setClassRef(action.getClassRef());
            setMethod(action.getMethod());
            setName(action.getName());
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

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ide.eclipse.web.flow.core.model.IConeableModelElement#cloneModelElement()
     */
    public ICloneableModelElement cloneModelElement() {
        Action action = new Action(); // don't set parent
        action.setAutowire(getAutowire());
        action.setBean(getBean());
        action.setBeanClass(getBeanClass());
        action.setClassRef(getClassRef());
        action.setMethod(getMethod());
        action.setName(getName());
        for (int i = 0; i < this.properties.size(); i++) {
            Property property = (Property) this.properties.get(i);
            action.addProperty((IProperty) property.cloneModelElement());
        }
        return action;
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
     * @see org.springframework.ide.eclipse.web.flow.core.internal.model.WebFlowModelElement#getElementType()
     */
    public int getElementType() {
        return IWebFlowModelElement.ACTION;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ide.eclipse.web.flow.core.model.IAction#getMethod()
     */
    public String getMethod() {
        return this.method;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ide.eclipse.web.flow.core.model.IAction#getName()
     */
    public String getName() {
        return this.name;
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

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ide.eclipse.web.flow.core.model.IAction#setMethod(java.lang.String)
     */
    public void setMethod(String method) {
        String oldValue = this.method;
        this.method = method;
        super.firePropertyChange(PROPS, oldValue, method);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ide.eclipse.web.flow.core.model.IAction#setName(java.lang.String)
     */
    public void setName(String name) {
        String oldValue = this.name;
        this.name = name;
        super.firePropertyChange(PROPS, oldValue, name);
    }
    
    public boolean hasBeanReference() {
        return (this.bean != null || this.beanClass != null
                || this.classRef != null || this.autowire != null || this.method != null);
    }
}