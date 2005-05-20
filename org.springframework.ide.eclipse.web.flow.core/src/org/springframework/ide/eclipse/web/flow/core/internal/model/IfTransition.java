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

import org.springframework.ide.eclipse.web.flow.core.model.IIf;
import org.springframework.ide.eclipse.web.flow.core.model.IIfTransition;
import org.springframework.ide.eclipse.web.flow.core.model.ITransitionableTo;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowModelElement;

public class IfTransition extends Transition implements IIfTransition {

    private boolean isThen;

    private IIf fromIf;

    public IfTransition() {
    }

    public IfTransition(ITransitionableTo to, IIf from, boolean isThen) {
        super(to);
        this.isThen = isThen;
        this.fromIf = from;
        if (isThen) {
            this.fromIf.setThenTransition(this);
        }
        else {
            this.fromIf.setElseTransition(this);
        }
    }

    public IfTransition(String to, IIf from, boolean isThen) {
        super(to);
        this.isThen = isThen;
        this.fromIf = from;
        if (isThen) {
            this.fromIf.setThenTransition(this);
        }
        else {
            this.fromIf.setElseTransition(this);
        }
    }

    /**
     * @return Returns the isThen.
     */
    public boolean isThen() {
        return isThen;
    }

    /**
     * @param isThen
     *            The isThen to set.
     */
    public void setThen(boolean isThen) {
        boolean oldValue = this.isThen;
        this.isThen = isThen;
        super.firePropertyChange(PROPS, new Boolean(oldValue), new Boolean(
                isThen));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ide.eclipse.web.flow.core.internal.model.WebFlowModelElement#getElementType()
     */
    public int getElementType() {
        return IWebFlowModelElement.IF_TRANSITION;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ide.eclipse.web.flow.core.model.IIfTransition#getFromState()
     */
    public IIf getFromIf() {
        return this.fromIf;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ide.eclipse.web.flow.core.model.IIfTransition#setFromIf(org.springframework.ide.eclipse.web.flow.core.model.IIf)
     */
    public void setFromIf(IIf fromIf) {
        this.fromIf = fromIf;
        if (isThen) {
            this.fromIf.setThenTransition(this);
        }
        else {
            this.fromIf.setElseTransition(this);
        }
    }
}