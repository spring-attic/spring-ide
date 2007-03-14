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
import org.springframework.ide.eclipse.webflow.core.model.IState;
import org.springframework.ide.eclipse.webflow.core.model.ITransition;
import org.springframework.ide.eclipse.webflow.core.model.ITransitionableFrom;
import org.springframework.ide.eclipse.webflow.core.model.ITransitionableTo;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowState;

/**
 * 
 */
public class AddAndAssignSourceCommand extends Command {

    /**
     * 
     */
    private IWebflowState parent;

    /**
     * 
     */
    private ITransitionableTo child;

    /**
     * 
     */
    private ITransitionableFrom source;

    /**
     * 
     */
    private ITransition transition;

    /* (non-Javadoc)
     * @see org.eclipse.gef.commands.Command#canExecute()
     */
    public boolean canExecute() {
        for (int i = 0; i < source.getOutputTransitions().size(); i++) {
            IState target = ((ITransition) source.getOutputTransitions().get(i))
                    .getToState();
            if (target.equals(child))
                return false;
        }
        return true;
    }

    /* (non-Javadoc)
     * @see org.eclipse.gef.commands.Command#execute()
     */
    public void execute() {
        parent.addState(child);
        //transition = new StateTransition(child, source, "*");
    }

    /* (non-Javadoc)
     * @see org.eclipse.gef.commands.Command#redo()
     */
    public void redo() {
        source.addOutputTransition(transition);
        child.addInputTransition(transition);
        parent.addState(child);
    }

    /**
     * 
     * 
     * @param sa 
     */
    public void setParent(IWebflowState sa) {
        parent = sa;
    }

    /**
     * 
     * 
     * @param activity 
     */
    public void setChild(ITransitionableTo activity) {
        child = activity;
    }

    /**
     * 
     * 
     * @param activity 
     */
    public void setSource(ITransitionableFrom activity) {
        source = activity;
    }

    /* (non-Javadoc)
     * @see org.eclipse.gef.commands.Command#undo()
     */
    public void undo() {
        source.removeOutputTransition(transition);
        child.removeInputTransition(transition);
        parent.removeState(child);
    }

}