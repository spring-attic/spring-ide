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
import org.springframework.ide.eclipse.web.flow.core.model.IAttributeMapper;
import org.springframework.ide.eclipse.web.flow.core.model.ICloneableModelElement;
import org.springframework.ide.eclipse.web.flow.core.model.IPersistableModelElement;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowModelElement;

public class AttributeMapper extends WebFlowModelElement implements
        IAttributeMapper, IPersistableModelElement, ICloneableModelElement {

    private String autowire;

    private String bean;

    private String beanClass;

    private String classRef;

    private String name;

    private String method;

    public AttributeMapper(IWebFlowModelElement parent, String id) {
        super(parent, id);
    }

    public AttributeMapper() {
        super(null, null);
    }

    /**
     * @return Returns the autowire.
     */
    public String getAutowire() {
        return autowire;
    }

    /**
     * @param autowire
     *            The autowire to set.
     */
    public void setAutowire(String autowire) {
        this.autowire = autowire;
    }

    /**
     * @return Returns the bean.
     */
    public String getBean() {
        return bean;
    }

    /**
     * @param bean
     *            The bean to set.
     */
    public void setBean(String bean) {
        this.bean = bean;
    }

    /**
     * @return Returns the beanClass.
     */
    public String getBeanClass() {
        return beanClass;
    }

    /**
     * @param beanClass
     *            The beanClass to set.
     */
    public void setBeanClass(String beanClass) {
        this.beanClass = beanClass;
    }

    /**
     * @return Returns the classRef.
     */
    public String getClassRef() {
        return classRef;
    }

    /**
     * @param classRef
     *            The classRef to set.
     */
    public void setClassRef(String classRef) {
        this.classRef = classRef;
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

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ide.eclipse.web.flow.core.model.IConeableModelElement#cloneModelElement()
     */
    public ICloneableModelElement cloneModelElement() {
        AttributeMapper mapper = new AttributeMapper();
        mapper.setAutowire(getAutowire());
        mapper.setBean(getBean());
        mapper.setBeanClass(getBeanClass());
        mapper.setClassRef(getClassRef());
        mapper.setMethod(getMethod());
        mapper.setName(getName());
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
            setAutowire(mapper.getAutowire());
            setBean(mapper.getBean());
            setBeanClass(mapper.getBeanClass());
            setClassRef(mapper.getClassRef());
            setMethod(mapper.getMethod());
            setName(mapper.getName());
        }
    }
}