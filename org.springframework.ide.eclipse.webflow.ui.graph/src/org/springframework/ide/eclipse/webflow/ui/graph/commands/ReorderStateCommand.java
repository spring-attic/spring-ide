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
import org.springframework.ide.eclipse.webflow.core.model.IWebflowState;

/**
 * 
 */
public class ReorderStateCommand extends Command {

    /**
     * 
     */
    private IState child;

    /**
     * 
     */
    private int oldIndex;
    
    /**
     * 
     */
    private int newIndex;

    /**
     * 
     */
    private IWebflowState parent;

    /**
     * 
     * 
     * @param child 
     * @param oldIndex 
     * @param newIndex 
     * @param parent 
     */
    public ReorderStateCommand(IState child, IWebflowState parent,
            int oldIndex, int newIndex) {
        super("Reorder state");
        this.child = child;
        this.parent = parent;
        this.oldIndex = oldIndex;
        this.newIndex = newIndex;
    }

    /* (non-Javadoc)
     * @see org.eclipse.gef.commands.Command#execute()
     */
    public void execute() {
        parent.removeState(child);
        parent.addState(child, newIndex);
    }

    /* (non-Javadoc)
     * @see org.eclipse.gef.commands.Command#undo()
     */
    public void undo() {
        parent.removeState(child);
        parent.addState(child, oldIndex);
    }

}