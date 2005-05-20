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
import java.util.List;

import org.springframework.ide.eclipse.web.flow.core.model.IModelElementVisitor;
import org.springframework.ide.eclipse.web.flow.core.model.ITransition;
import org.springframework.ide.eclipse.web.flow.core.model.ITransitionableTo;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowModelElement;

public abstract class AbstractTransitionableTo extends AbstractState implements
        ITransitionableTo {

    private List inputTransitions;

    public AbstractTransitionableTo(IWebFlowModelElement parent, String id) {
        super(parent, id);
        this.inputTransitions = new ArrayList();
    }

    /**
     * @return Returns the transitions.
     */
    public List getInputTransitions() {
        return inputTransitions;
    }

    /**
     * @param transitions
     *            The transitions to set.
     */
    public void addInputTransition(ITransition transitions) {
        this.inputTransitions.add(transitions);
        super.fireStructureChange(INPUTS, transitions);
    }

    /**
     * @param transitions
     *            The transitions to set.
     */
    public void removeInputTransition(ITransition transitions) {
        this.inputTransitions.remove(transitions);
        super.fireStructureChange(INPUTS, transitions);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ide.eclipse.web.flow.core.model.IWebFlowModelElement#accept(org.springframework.ide.eclipse.web.flow.core.model.IModelElementVisitor)
     */
    public void accept(IModelElementVisitor visitor) {
        if (visitor.visit(this)) {
        }
    }
}