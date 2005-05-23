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
import org.springframework.ide.eclipse.web.flow.core.model.ITransition;
import org.springframework.ide.eclipse.web.flow.core.model.ITransitionableTo;

public abstract class Transition extends AbstractModelElement implements
        ITransition {

    protected ITransitionableTo toState;

    private String toStateId;
    
    public Transition() {
        super(null, null);
    }

    public Transition(ITransitionableTo to) {
        super(null, null);
        this.toState = to;
        this.toState.addInputTransition(this);
    }

    public Transition(String to) {
        super(null, null);
        this.toStateId = to;
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
     * @see org.springframework.ide.eclipse.web.flow.core.model.ITransition#getToState()
     */
    public ITransitionableTo getToState() {
        return this.toState;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ide.eclipse.web.core.model.ITransition#setToState(org.springframework.ide.eclipse.web.core.model.ITransitionableState)
     */
    public void setToState(ITransitionableTo state) {
        if (this.toState != null) {
            this.toState.removeInputTransition(this);
        }
        this.toState = state;
        super.fireStructureChange(OUTPUTS, state);
        if (this.toState != null) {
            this.toState.addInputTransition(this);
        }
    }

    public String getToStateId() {
        if (this.toState != null) {
            return this.toState.getId();
        }
        else {
            return this.toStateId;
        }
    }
}