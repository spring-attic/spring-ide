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
import org.springframework.ide.eclipse.webflow.core.model.IAttribute;
import org.springframework.ide.eclipse.webflow.core.model.IAttributeEnabled;

/**
 * 
 */
public class DeleteStatePropertyCommand extends Command {

    /**
     * 
     */
    private IAttribute child;

    /**
     * 
     */
    private int index = -1;

    /**
     * 
     */
    private IAttributeEnabled parent;

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
        index = parent.getAttributes().indexOf(child);
        parent.removeAttribute(child);
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
    public void setChild(IAttribute a) {
        child = a;
    }

    /**
     * 
     * 
     * @param sa 
     */
    public void setParent(IAttributeEnabled sa) {
        parent = sa;
    }

    /* (non-Javadoc)
     * @see org.eclipse.gef.commands.Command#undo()
     */
    public void undo() {
        parent.addAttribute(child, index);
    }
}
