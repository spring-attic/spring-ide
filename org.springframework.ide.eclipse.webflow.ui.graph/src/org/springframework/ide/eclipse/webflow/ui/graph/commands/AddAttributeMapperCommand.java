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
import org.springframework.ide.eclipse.webflow.core.model.IAttributeMapper;
import org.springframework.ide.eclipse.webflow.core.model.ISubflowState;

/**
 * 
 */
public class AddAttributeMapperCommand extends Command {

    /**
     * 
     */
    private IAttributeMapper child;

    /**
     * 
     */
    private ISubflowState parent;

    /* (non-Javadoc)
     * @see org.eclipse.gef.commands.Command#execute()
     */
    public void execute() {
        parent.setAttributeMapper(child);
    }

    /**
     * 
     * 
     * @return 
     */
    public ISubflowState getParent() {
        return parent;
    }

    /**
     * 
     * 
     * @param newChild 
     */
    public void setChild(IAttributeMapper newChild) {
        child = newChild;
    }

    /**
     * 
     * 
     * @param newParent 
     */
    public void setParent(ISubflowState newParent) {
        parent = newParent;
    }

    /* (non-Javadoc)
     * @see org.eclipse.gef.commands.Command#undo()
     */
    public void undo() {
        parent.removeAttributeMapper();
    }

}
