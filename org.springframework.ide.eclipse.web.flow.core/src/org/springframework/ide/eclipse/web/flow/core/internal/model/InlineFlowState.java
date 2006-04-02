/*
 * Copyright 2004 DekaBank Deutsche Girozentrale. All rights reserved.
 */
package org.springframework.ide.eclipse.web.flow.core.internal.model;

import org.eclipse.core.resources.IResource;
import org.springframework.ide.eclipse.web.flow.core.model.IInlineFlowState;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowModelElement;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowState;
public class InlineFlowState
        extends WebFlowState implements IInlineFlowState {
    
    public InlineFlowState(IWebFlowModelElement parent, String name) {
        super(parent, name);
        this.id = name;
        if (parent instanceof IWebFlowState) {
            ((IWebFlowState) parent).addState(this);
        }
    }

    private String id;
    
    private IWebFlowState state;
    
    public int getElementType() {
        return IWebFlowModelElement.INLINE_FLOW;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        String oldValue = this.id;
        this.id = id;
        super.setElementName(id);
        super.firePropertyChange(PROPS, oldValue, id);        
    }

    public void setWebFlowState(IWebFlowState state) {
        String oldValue = this.id;
        this.state = state;
        super.firePropertyChange(PROPS, oldValue, id);
    }

    public IWebFlowState getWebFlowState() {
        return this.state;
    }

    public IResource getElementResource() {
        return getElementParent().getElementResource();
    }

}
