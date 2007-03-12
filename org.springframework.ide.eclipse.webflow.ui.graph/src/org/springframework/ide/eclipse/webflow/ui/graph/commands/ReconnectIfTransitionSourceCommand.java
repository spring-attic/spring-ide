/*
 * Copyright 2002-2007 the original author or authors.
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

package org.springframework.ide.eclipse.webflow.ui.graph.commands;

import org.eclipse.gef.commands.Command;
import org.springframework.ide.eclipse.webflow.core.model.IIf;
import org.springframework.ide.eclipse.webflow.core.model.IIfTransition;
import org.springframework.ide.eclipse.webflow.core.model.ITransition;
import org.springframework.ide.eclipse.webflow.core.model.ITransitionableTo;

/**
 * 
 */
public class ReconnectIfTransitionSourceCommand extends Command {

    /**
     * 
     */
    protected IIf oldSource;

    /**
     * 
     */
    protected IIf source;

    /**
     * 
     */
    protected ITransitionableTo target;

    /**
     * 
     */
    protected IIfTransition transition;

    /* (non-Javadoc)
     * @see org.eclipse.gef.commands.Command#canExecute()
     */
    public boolean canExecute() {
        if (transition.getToState().equals(source) || !(source instanceof IIf))
            return false;
        return true;
    }

    /* (non-Javadoc)
     * @see org.eclipse.gef.commands.Command#execute()
     */
    public void execute() {
        if (source != null) {
            if (transition.isThen()) {
                oldSource.removeThenTransition();
            }
            else {
                oldSource.removeElseTransition();
            }
            transition.setFromIf(source);
            if (transition.isThen()) {
                source.setThenTransition(transition);
            }
            else {
                source.setElseTransition(transition);
            }
        }
    }

    /**
     * 
     * 
     * @return 
     */
    public IIf getSource() {
        return source;
    }

    /**
     * 
     * 
     * @return 
     */
    public ITransitionableTo getTarget() {
        return target;
    }

    /**
     * 
     * 
     * @return 
     */
    public ITransition getTransition() {
        return transition;
    }

    /**
     * 
     * 
     * @param activity 
     */
    public void setSource(IIf activity) {
        source = activity;
    }

    /**
     * 
     * 
     * @param activity 
     */
    public void setTarget(ITransitionableTo activity) {
        target = activity;
    }

    /**
     * 
     * 
     * @param trans 
     */
    public void setTransition(IIfTransition trans) {
        transition = trans;
        target = trans.getToState();
        oldSource = trans.getFromIf();
    }

    /* (non-Javadoc)
     * @see org.eclipse.gef.commands.Command#undo()
     */
    public void undo() {
        if (source != null) {
            if (transition.isThen()) {
                source.removeThenTransition();
            }
            else {
                source.removeElseTransition();
            }
            transition.setFromIf(oldSource);
            if (transition.isThen()) {
                oldSource.setThenTransition(transition);
            }
            else {
                oldSource.setElseTransition(transition);
            }
        }
    }
}