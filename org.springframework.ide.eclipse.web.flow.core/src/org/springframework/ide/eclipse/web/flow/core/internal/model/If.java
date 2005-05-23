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
import org.springframework.ide.eclipse.web.flow.core.model.IDecisionState;
import org.springframework.ide.eclipse.web.flow.core.model.IIf;
import org.springframework.ide.eclipse.web.flow.core.model.IIfTransition;
import org.springframework.ide.eclipse.web.flow.core.model.IModelWriter;
import org.springframework.ide.eclipse.web.flow.core.model.IPersistableModelElement;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowModelElement;

public class If extends WebFlowModelElement implements IIf,
        IPersistableModelElement, ICloneableModelElement {

    private String test;

    private IIfTransition elseTransition;

    private IIfTransition thenTransition;

    public If() {
        this(null, null, null);
    }

    public If(IWebFlowModelElement parent, String test, IIfTransition then) {
        this(parent, test, then, null);
    }

    public If(IWebFlowModelElement parent, String test, IIfTransition then,
            IIfTransition theElse) {
        super(parent, null);
        this.test = test;
        this.thenTransition = then;
        this.elseTransition = theElse;
        if (parent instanceof IDecisionState) {
            ((IDecisionState) parent).addIf(this);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ide.eclipse.web.flow.core.model.ICloneableModelElement#applyCloneValues(org.springframework.ide.eclipse.web.flow.core.model.ICloneableModelElement)
     */
    public void applyCloneValues(ICloneableModelElement element) {
        if (element instanceof If) {
            If cloneIf = (If) element;
            setTest(cloneIf.getTest());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ide.eclipse.web.flow.core.model.ICloneableModelElement#cloneModelElement()
     */
    public ICloneableModelElement cloneModelElement() {
        If newIf = new If();
        newIf.setElementName(this.getElementName());
        newIf.setTest(getTest());
        return newIf;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ide.eclipse.web.flow.core.model.IWebFlowModelElement#getElementResource()
     */
    public IResource getElementResource() {
        return this.getElementParent().getElementResource();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ide.eclipse.web.flow.core.internal.model.WebFlowModelElement#getElementType()
     */
    public int getElementType() {
        return IWebFlowModelElement.IF;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ide.eclipse.web.flow.core.model.IIf#getTest()
     */
    public String getTest() {
        return this.test;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ide.eclipse.web.flow.core.model.IIf#setElse(org.springframework.ide.eclipse.web.flow.core.model.ITransitionableTo)
     */
    public void setElseTransition(IIfTransition theElse) {
        IIfTransition oldValue = this.elseTransition;
        this.elseTransition = theElse;
        this.elseTransition.setThen(false);
        super.firePropertyChange(OUTPUTS, oldValue, theElse);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ide.eclipse.web.flow.core.model.IIf#setTest(java.lang.String)
     */
    public void setTest(String test) {
        String oldValue = this.test;
        this.test = test;
        super.firePropertyChange(PROPS, oldValue, test);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ide.eclipse.web.flow.core.model.IIf#setThen(org.springframework.ide.eclipse.web.flow.core.model.ITransitionableTo)
     */
    public void setThenTransition(IIfTransition then) {
        IIfTransition oldValue = this.thenTransition;
        this.thenTransition = then;
        this.thenTransition.setThen(true);
        super.firePropertyChange(OUTPUTS, oldValue, then);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ide.eclipse.web.flow.core.model.IIf#getElseTransition()
     */
    public IIfTransition getElseTransition() {
        return this.elseTransition;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ide.eclipse.web.flow.core.model.IIf#getThenTransition()
     */
    public IIfTransition getThenTransition() {
        return this.thenTransition;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ide.eclipse.web.flow.core.model.IIf#removeElseTransition()
     */
    public void removeElseTransition() {
        IIfTransition oldValue = this.elseTransition;
        this.elseTransition = null;
        super.firePropertyChange(OUTPUTS, oldValue, null);
    }

    public void removeThenTransition() {
        IIfTransition oldValue = this.thenTransition;
        this.thenTransition = null;
        super.firePropertyChange(OUTPUTS, oldValue, null);
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
}