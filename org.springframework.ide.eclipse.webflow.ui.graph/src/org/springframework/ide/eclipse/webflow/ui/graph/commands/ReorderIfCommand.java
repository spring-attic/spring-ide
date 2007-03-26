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
import org.springframework.ide.eclipse.webflow.core.model.IDecisionState;
import org.springframework.ide.eclipse.webflow.core.model.IIf;

/**
 * 
 */
public class ReorderIfCommand extends Command {

    /**
     * 
     */
    private IIf child;

    /**
     * 
     */
    private int oldIndex, newIndex;

    /**
     * 
     */
    private IDecisionState parent;

    /**
     * 
     * 
     * @param child 
     * @param oldIndex 
     * @param newIndex 
     * @param parent 
     */
    public ReorderIfCommand(IIf child, IDecisionState parent, int oldIndex,
            int newIndex) {
        super("Reorder if");
        this.child = child;
        this.parent = parent;
        this.oldIndex = oldIndex;
        this.newIndex = newIndex;
    }

    /* (non-Javadoc)
     * @see org.eclipse.gef.commands.Command#execute()
     */
    public void execute() {
        parent.removeIf(child);
        parent.addIf(child, newIndex);
    }

    /* (non-Javadoc)
     * @see org.eclipse.gef.commands.Command#undo()
     */
    public void undo() {
        parent.removeIf(child);
        parent.addIf(child, oldIndex);
    }

}
