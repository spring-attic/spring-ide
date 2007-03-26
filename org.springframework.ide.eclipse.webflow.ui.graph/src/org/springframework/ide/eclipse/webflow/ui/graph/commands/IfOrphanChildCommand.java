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

import java.util.List;

import org.eclipse.gef.commands.Command;
import org.springframework.ide.eclipse.webflow.core.model.IDecisionState;
import org.springframework.ide.eclipse.webflow.core.model.IIf;

/**
 * 
 */
public class IfOrphanChildCommand extends Command {

    /**
     * 
     */
    private IIf child;

    /**
     * 
     */
    private int index;

    /**
     * 
     */
    private IDecisionState parent;

    /* (non-Javadoc)
     * @see org.eclipse.gef.commands.Command#execute()
     */
    public void execute() {
        List children = parent.getIfs();
        index = children.indexOf(child);
        parent.removeIf(child);
    }

    /**
     * 
     * 
     * @param child 
     */
    public void setChild(IIf child) {
        this.child = child;
    }

    /**
     * 
     * 
     * @param parent 
     */
    public void setParent(IDecisionState parent) {
        this.parent = parent;
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
