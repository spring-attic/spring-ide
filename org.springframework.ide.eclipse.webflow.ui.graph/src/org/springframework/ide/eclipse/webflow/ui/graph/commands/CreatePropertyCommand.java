/*******************************************************************************
 * Copyright (c) 2007 Spring IDE Developers
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
import org.springframework.ide.eclipse.webflow.core.model.IState;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;

/**
 * @author Christian Dupuis
 */
public class CreatePropertyCommand extends Command {

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
    private IWebflowModelElement parent;

    /* (non-Javadoc)
     * @see org.eclipse.gef.commands.Command#execute()
     */
    public void execute() {
        if (parent instanceof IAttributeEnabled) {
            if (index >= 0)
                ((IAttributeEnabled) parent).addAttribute(child, index);
            else
                ((IAttributeEnabled) parent).addAttribute(child);
        }
    }

    /**
     * 
     * 
     * @param action 
     */
    public void setChild(IAttribute action) {
        child = action;
    }

    /**
     * 
     * 
     * @param index 
     */
    public void setIndex(int index) {
        this.index = index;
    }

    /**
     * 
     * 
     * @param sa 
     */
    public void setParent(IWebflowModelElement sa) {
        parent = sa;
    }

    /* (non-Javadoc)
     * @see org.eclipse.gef.commands.Command#undo()
     */
    public void undo() {
        if (parent instanceof IAttributeEnabled) {
            ((IState) parent).removeAttribute(child);
        }
    }
}
