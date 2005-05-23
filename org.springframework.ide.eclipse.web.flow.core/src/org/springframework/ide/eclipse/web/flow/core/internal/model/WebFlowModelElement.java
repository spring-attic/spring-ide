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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.springframework.ide.eclipse.web.flow.core.model.IModelElementVisitor;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowModelElement;

public abstract class WebFlowModelElement implements IWebFlowModelElement {

    private IWebFlowModelElement parent;

    private String name;

    private int startLine;

    private int endLine;

    protected WebFlowModelElement(IWebFlowModelElement parent, String name) {
        this.parent = parent;
        this.name = name;
        this.startLine = -1;
        this.endLine = -1;
    }

    public abstract int getElementType();

    public final void setElementParent(IWebFlowModelElement parent) {
        this.parent = parent;
    }

    public final IWebFlowModelElement getElementParent() {
        return parent;
    }

    public final void setElementParent(WebFlowModelElement parent) {
        this.parent = parent;
    }

    public final void setElementName(String name) {
        this.name = name;
    }

    public final String getElementName() {
        return this.name;
    }

    public final void setElementStartLine(int line) {
        this.startLine = line;
    }

    public final int getElementStartLine() {
        return this.startLine;
    }

    public final void setElementEndLine(int endLine) {
        this.endLine = endLine;
    }

    public final int getElementEndLine() {
        return this.endLine;
    }

    transient protected PropertyChangeSupport listeners = new PropertyChangeSupport(
            this);

    public void addPropertyChangeListener(PropertyChangeListener l) {
        listeners.addPropertyChangeListener(l);
    }

    protected void firePropertyChange(String prop, Object old, Object newValue) {
        listeners.firePropertyChange(prop, old, newValue);
    }

    protected void fireStructureChange(String prop, Object child) {
        listeners.firePropertyChange(prop, null, child);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        listeners.removePropertyChangeListener(l);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ide.eclipse.web.flow.core.model.IWebFlowModelElement#accept(org.springframework.ide.eclipse.web.flow.core.model.IModelElementVisitor)
     */
    public void accept(IModelElementVisitor visitor) {
        visitor.visit(this);
    }
}