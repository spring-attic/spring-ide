/*
 * Copyright 2004 DekaBank Deutsche Girozentrale. All rights reserved.
 */
package org.springframework.ide.eclipse.web.flow.core.internal.model;

import org.eclipse.core.resources.IResource;
import org.springframework.ide.eclipse.web.flow.core.model.ICloneableModelElement;
import org.springframework.ide.eclipse.web.flow.core.model.IModelWriter;
import org.springframework.ide.eclipse.web.flow.core.model.IPersistableModelElement;
import org.springframework.ide.eclipse.web.flow.core.model.IProperty;
import org.springframework.ide.eclipse.web.flow.core.model.ISetup;
import org.springframework.ide.eclipse.web.flow.core.model.ITransitionableTo;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowModelElement;

public class Setup extends AbstractModelElement implements ISetup, IPersistableModelElement, ICloneableModelElement {
    
    private String method; 
    
    private String onErrorId;
    
    private ITransitionableTo onError;
    
    public Setup(IWebFlowModelElement parent, String name) {
        super(parent, name);
    }
    
    public Setup() {
        super(null, null);
    }
    
    /* (non-Javadoc)
     * @see org.springframework.ide.eclipse.web.flow.core.model.IWebFlowModelElement#getElementType()
     */
    public int getElementType() {
        return IWebFlowModelElement.SETUP;
    }

    /* (non-Javadoc)
     * @see org.springframework.ide.eclipse.web.flow.core.model.ISetup#getMethod()
     */
    public String getMethod() {
        return this.method;
    }

    /* (non-Javadoc)
     * @see org.springframework.ide.eclipse.web.flow.core.model.ISetup#setMethod(java.lang.String)
     */
    public void setMethod(String method) {
        String oldValue = this.method;
        this.method = method;
        super.firePropertyChange(PROPS, oldValue, method);

    }

    /* (non-Javadoc)
     * @see org.springframework.ide.eclipse.web.flow.core.model.ISetup#getOnErrorId()
     */
    public String getOnErrorId() {
        if (this.onError != null) {
            return this.onError.getId();
        }
        else {
            return this.onErrorId;
        }
    }

    /* (non-Javadoc)
     * @see org.springframework.ide.eclipse.web.flow.core.model.ISetup#getOnError()
     */
    public ITransitionableTo getOnError() {
        return this.onError;
    }

    /* (non-Javadoc)
     * @see org.springframework.ide.eclipse.web.flow.core.model.ISetup#setOnErrorId(java.lang.String)
     */
    public void setOnErrorId(String id) {
        String oldValue = this.onErrorId;
        this.onErrorId = id;
        super.firePropertyChange(PROPS, oldValue, onErrorId);

    }

    /* (non-Javadoc)
     * @see org.springframework.ide.eclipse.web.flow.core.model.ISetup#setOnError(org.springframework.ide.eclipse.web.flow.core.model.ITransitionableTo)
     */
    public void setOnError(ITransitionableTo onerror) {
        ITransitionableTo oldValue = this.onError;
        this.onError = onerror;
        super.firePropertyChange(PROPS, oldValue, onerror);
    }


    /* (non-Javadoc)
     * @see org.springframework.ide.eclipse.web.flow.core.model.IWebFlowModelElement#getElementResource()
     */
    public IResource getElementResource() {
        return super.getElementParent().getElementResource();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ide.eclipse.web.flow.core.model.IPersistable#save(org.springframework.ide.eclipse.web.flow.core.model.IModelWriter)
     */
    public void save(IModelWriter writer) {
        writer.doStart(this);
        writer.doEnd(this);
    }

    /* (non-Javadoc)
     * @see org.springframework.ide.eclipse.web.flow.core.model.ICloneableModelElement#cloneModelElement()
     */
    public ICloneableModelElement cloneModelElement() {
        Setup setup = new Setup();
        setup.setElementName(getElementName());
        setup.setElementParent(getElementParent());
        setup.setAutowire(getAutowire());
        setup.setBean(getBean());
        setup.setBeanClass(getBeanClass());
        setup.setClassRef(getClassRef());
        setup.setMethod(getMethod());
        setup.setOnErrorId(getOnErrorId());
        for (int i = 0; i < super.getProperties().size(); i++) {
            Property property = (Property) super.getProperties().get(i);
            setup.addProperty((IProperty) property.cloneModelElement());
        }
        return setup;
    }

    /* (non-Javadoc)
     * @see org.springframework.ide.eclipse.web.flow.core.model.ICloneableModelElement#applyCloneValues(org.springframework.ide.eclipse.web.flow.core.model.ICloneableModelElement)
     */
    public void applyCloneValues(ICloneableModelElement element) {
        if (element instanceof ISetup) {
            Setup setup = (Setup) element;
            setAutowire(setup.getAutowire());
            setBean(setup.getBean());
            setBeanClass(setup.getBeanClass());
            setClassRef(setup.getClassRef());
            setMethod(setup.getMethod());
            Property[] props = (Property[]) this.getProperties().toArray(
                    new Property[this.getProperties().size()]);
            for (int i = 0; i < props.length; i++) {
                removeProperty(props[i]);
            }
            for (int i = 0; i < setup.getProperties().size(); i++) {
                addProperty((IProperty) setup.getProperties().get(i));
            }
        }
        
    }
}
