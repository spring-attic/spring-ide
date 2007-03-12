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
import org.springframework.ide.eclipse.webflow.core.model.IDecisionState;
import org.springframework.ide.eclipse.webflow.core.model.IIf;

/**
 * 
 */
public class DeleteIfCommand extends Command {

    /**
     * 
     */
    private IIf child;

    /**
     * 
     */
    private int index = -1;

    /**
     * 
     */
    private IDecisionState parent;

    /* (non-Javadoc)
     * @see org.eclipse.gef.commands.Command#execute()
     */
    public void execute() {
        primExecute();
    }

    /**
     * 
     */
    protected void primExecute() {
        index = parent.getIfs().indexOf(child);
        if (child.getThenTransition() != null) {
            child.getThenTransition().getToState().removeInputTransition(
                    child.getThenTransition());
        }

        if (child.getElseTransition() != null) {
            child.getElseTransition().getToState().removeInputTransition(
                    child.getElseTransition());
        }
        parent.removeIf(child);

    }

    /* (non-Javadoc)
     * @see org.eclipse.gef.commands.Command#redo()
     */
    public void redo() {
        primExecute();
    }

    /**
     * 
     * 
     * @param a 
     */
    public void setChild(IIf a) {
        child = a;
    }

    /**
     * 
     * 
     * @param sa 
     */
    public void setParent(IDecisionState sa) {
        parent = sa;
    }

    /* (non-Javadoc)
     * @see org.eclipse.gef.commands.Command#undo()
     */
    public void undo() {
        if (child.getThenTransition() != null) {
            child.getThenTransition().getToState().addInputTransition(
                    child.getThenTransition());
        }

        if (child.getElseTransition() != null) {
            child.getElseTransition().getToState().addInputTransition(
                    child.getElseTransition());
        }
        parent.addIf(child, index);

    }
}