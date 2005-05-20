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

import org.springframework.ide.eclipse.web.flow.core.model.IModelElementVisitor;
import org.springframework.ide.eclipse.web.flow.core.model.IModelWriter;
import org.springframework.ide.eclipse.web.flow.core.model.IPersistableModelElement;
import org.springframework.ide.eclipse.web.flow.core.model.ITransition;
import org.springframework.ide.eclipse.web.flow.core.model.ITransitionableFrom;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowModelElement;

public abstract class AbstractTransitionableFrom extends
        AbstractTransitionableTo implements ITransitionableFrom {

    private List outputTransitions;

    public AbstractTransitionableFrom(IWebFlowModelElement parent, String id) {
        super(parent, id);
        this.outputTransitions = new ArrayList();
    }

    /**
     * @return Returns the transitions.
     */
    public List getOutputTransitions() {
        return outputTransitions;
    }

    /**
     * @param transitions
     *            The transitions to set.
     */
    public void addOutputTransition(ITransition transitions) {
        this.outputTransitions.add(transitions);
        super.fireStructureChange(OUTPUTS, transitions);
    }

    /**
     * @param transitions
     *            The transitions to set.
     */
    public void removeOutputTransition(ITransition transitions) {
        this.outputTransitions.remove(transitions);
        super.fireStructureChange(OUTPUTS, transitions);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ide.eclipse.web.flow.core.model.IWebFlowModelElement#accept(org.springframework.ide.eclipse.web.flow.core.model.IModelElementVisitor)
     */
    public void accept(IModelElementVisitor visitor) {
        if (visitor.visit(this)) {
            Iterator iter = this.getOutputTransitions().iterator();
            while (iter.hasNext()) {
                IWebFlowModelElement element = (IWebFlowModelElement) iter
                        .next();
                element.accept(visitor);
            }
        }
    }

    public void save(IModelWriter writer) {
        Iterator iter = this.outputTransitions.iterator();
        while (iter.hasNext()) {
            IPersistableModelElement element = (IPersistableModelElement) iter
                    .next();
            element.save(writer);
        }
    }
}