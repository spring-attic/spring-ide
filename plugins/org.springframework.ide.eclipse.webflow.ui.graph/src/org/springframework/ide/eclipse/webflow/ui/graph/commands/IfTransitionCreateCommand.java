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
import org.springframework.ide.eclipse.webflow.core.internal.model.IfTransition;
import org.springframework.ide.eclipse.webflow.core.model.IIf;
import org.springframework.ide.eclipse.webflow.core.model.IIfTransition;
import org.springframework.ide.eclipse.webflow.core.model.ITransition;
import org.springframework.ide.eclipse.webflow.core.model.ITransitionableTo;

/**
 * @author Christian Dupuis
 */
public class IfTransitionCreateCommand extends Command {

    /**
     * 
     */
    protected IIf source;

    /**
     * 
     */
    protected ITransitionableTo target;

    /**
     * 
     */
    protected IIfTransition transition;

    /* (non-Javadoc)
     * @see org.eclipse.gef.commands.Command#canExecute()
     */
    public boolean canExecute() {
        if (source.equals(target))
            return false;
        if (!(source instanceof IIf)) {
            return false;
        }
        // Check for existence of connection already
        if (source.getElseTransition() != null) {
            return false;
        }
        return true;
    }

    /* (non-Javadoc)
     * @see org.eclipse.gef.commands.Command#execute()
     */
    public void execute() {
        //transition = new IfTransition(target, source, false);
    }

    /**
     * 
     * 
     * @return 
     */
    public IIf getSource() {
        return source;
    }

    /**
     * 
     * 
     * @return 
     */
    public ITransitionableTo getTarget() {
        return target;
    }

    /**
     * 
     * 
     * @return 
     */
    public ITransition getTransition() {
        return transition;
    }

    /* (non-Javadoc)
     * @see org.eclipse.gef.commands.Command#redo()
     */
    public void redo() {
        source.setElseTransition(transition);
        target.addInputTransition(transition);
    }

    /**
     * 
     * 
     * @param activity 
     */
    public void setSource(IIf activity) {
        source = activity;
    }

    /**
     * 
     * 
     * @param activity 
     */
    public void setTarget(ITransitionableTo activity) {
        target = activity;
    }

    /**
     * 
     * 
     * @param transition 
     */
    public void setTransition(IfTransition transition) {
        this.transition = transition;
    }

    /* (non-Javadoc)
     * @see org.eclipse.gef.commands.Command#undo()
     */
    public void undo() {
        source.removeElseTransition();
        target.removeInputTransition(transition);
    }
}
