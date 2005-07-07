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

import org.springframework.ide.eclipse.web.flow.core.model.ICloneableModelElement;
import org.springframework.ide.eclipse.web.flow.core.model.IDecisionState;
import org.springframework.ide.eclipse.web.flow.core.model.IIf;
import org.springframework.ide.eclipse.web.flow.core.model.IModelWriter;
import org.springframework.ide.eclipse.web.flow.core.model.IPersistableModelElement;
import org.springframework.ide.eclipse.web.flow.core.model.IProperty;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowModelElement;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowState;

public class DecisionState extends AbstractTransitionableFrom implements
        IDecisionState, IPersistableModelElement, ICloneableModelElement {

    private List ifs = new ArrayList();

    public DecisionState(IWebFlowModelElement parent, String id, List ifs) {
        super(parent, id);
        if (ifs != null)
            this.ifs = ifs;
        if (parent instanceof IWebFlowState) {
            ((IWebFlowState) parent).addState(this);
        }
    }

    public DecisionState() {
        super(null, null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ide.eclipse.web.flow.core.model.IWebFlowModelElement#getElementType()
     */
    public int getElementType() {
        return IWebFlowModelElement.DECISION_STATE;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ide.eclipse.web.flow.core.model.IDecisionState#getIfs()
     */
    public List getIfs() {
        return this.ifs;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ide.eclipse.web.flow.core.model.IDecisionState#addIf(org.springframework.ide.eclipse.web.flow.core.model.IIf)
     */
    public void addIf(IIf theIf) {
        if (!this.ifs.contains(theIf)) {
            theIf.setElementParent(this);
            this.ifs.add(theIf);
            super.firePropertyChange(ADD_CHILDREN, new Integer(this.ifs
                    .indexOf(theIf)), theIf);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ide.eclipse.web.flow.core.model.IDecisionState#addIf(org.springframework.ide.eclipse.web.flow.core.model.IIf,
     *      int)
     */
    public void addIf(IIf theIf, int i) {
        if (!this.ifs.contains(theIf)) {
            theIf.setElementParent(this);
            this.ifs.add(i, theIf);
            super.firePropertyChange(ADD_CHILDREN, new Integer(i), theIf);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ide.eclipse.web.flow.core.model.IDecisionState#removeIf(org.springframework.ide.eclipse.web.flow.core.model.IIf)
     */
    public void removeIf(IIf theIf) {
        this.ifs.remove(theIf);
        super.fireStructureChange(REMOVE_CHILDREN, theIf);
    }

    public void save(IModelWriter writer) {
        writer.doStart(this);
        Iterator iter = this.ifs.iterator();
        while (iter.hasNext()) {
            IPersistableModelElement element = (IPersistableModelElement) iter
                    .next();
            element.save(writer);
        }
        //super.save(writer);
        iter = this.getProperties().iterator();
        while (iter.hasNext()) {
            IWebFlowModelElement element = (IWebFlowModelElement) iter.next();
            if (element instanceof IPersistableModelElement) {
                ((IPersistableModelElement) element).save(writer);
            }
        }
        writer.doEnd(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ide.eclipse.web.flow.core.model.ICloneableModelElement#cloneModelElement()
     */
    public ICloneableModelElement cloneModelElement() {
        DecisionState state = new DecisionState();
        state.setId(getId());
        state.setElementName(getElementName());
        state.setElementParent(getElementParent());
        state.setAutowire(getAutowire());
        state.setBean(getBean());
        state.setBeanClass(getBeanClass());
        state.setClassRef(getClassRef());
        state.setDescription(getDescription());
        for (int i = 0; i < this.getIfs().size(); i++) {
            state.addIf((IIf) ((ICloneableModelElement) this.getIfs().get(i))
                    .cloneModelElement());
        }
        for (int i = 0; i < this.getProperties().size(); i++) {
            Property property = (Property) this.getProperties().get(i);
            state.addProperty((IProperty) property.cloneModelElement());
        }
        return state;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ide.eclipse.web.flow.core.model.ICloneableModelElement#applyCloneValues(org.springframework.ide.eclipse.web.flow.core.model.ICloneableModelElement)
     */
    public void applyCloneValues(ICloneableModelElement element) {
        if (element instanceof IDecisionState) {
            DecisionState state = (DecisionState) element;
            setId(state.getId());
            setAutowire(state.getAutowire());
            setBean(state.getBean());
            setBeanClass(state.getBeanClass());
            setClassRef(state.getClassRef());
            setDescription(state.getDescription());
            for (int i = 0; i < state.getIfs().size(); i++) {
                ((ICloneableModelElement) this.getIfs().get(i))
                        .applyCloneValues((ICloneableModelElement) state
                                .getIfs().get(i));
            }
            Property[] props = (Property[]) this.getProperties().toArray(
                    new Property[this.getProperties().size()]);
            for (int i = 0; i < props.length; i++) {
                removeProperty(props[i]);
            }
            for (int i = 0; i < state.getProperties().size(); i++) {
                addProperty((IProperty) state.getProperties().get(i));
            }
        }
    }

}