/*******************************************************************************
 * Copyright (c) 2005, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
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
        this.oldIndex = oldIndex - 1;
        this.newIndex = newIndex - 1;
    }

    /* (non-Javadoc)
     * @see org.eclipse.gef.commands.Command#execute()
     */
    public void execute() {
        parent.removeState(child);
        parent.addState(child, newIndex + 1);
    }

    /* (non-Javadoc)
     * @see org.eclipse.gef.commands.Command#undo()
     */
    public void undo() {
        parent.removeState(child);
        parent.addState(child, oldIndex + 1);
    }

}
