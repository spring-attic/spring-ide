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
import org.springframework.ide.eclipse.webflow.core.model.IStateTransition;
import org.springframework.ide.eclipse.webflow.core.model.ITransitionableFrom;
import org.springframework.ide.eclipse.webflow.core.model.ITransitionableTo;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowState;

/**
 * 
 */
public class CreateAndAssignSourceCommand extends Command {

    /**
     * 
     */
    private ITransitionableTo child;

    /**
     * 
     */
    private IWebflowState parent;

    /**
     * 
     */
    private ITransitionableFrom source;

    /**
     * 
     */
    private IStateTransition transition;

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
     * @param activity 
     */
    public void setChild(ITransitionableTo activity) {
        child = activity;
        //child.setId(activity.getElementName());
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
