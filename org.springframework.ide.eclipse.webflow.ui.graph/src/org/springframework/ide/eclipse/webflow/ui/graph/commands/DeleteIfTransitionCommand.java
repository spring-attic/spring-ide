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
import org.springframework.ide.eclipse.webflow.core.model.IIf;
import org.springframework.ide.eclipse.webflow.core.model.IIfTransition;
import org.springframework.ide.eclipse.webflow.core.model.ITransitionableTo;

/**
 * @author Christian Dupuis
 */
public class DeleteIfTransitionCommand extends Command {

    /**
     * 
     */
    private IIf source;

    /**
     * 
     */
    private ITransitionableTo target;

    /**
     * 
     */
    private IIfTransition transition;

    /* (non-Javadoc)
     * @see org.eclipse.gef.commands.Command#execute()
     */
    public void execute() {

        source.removeElseTransition();
        target.removeInputTransition(transition);
        //transition.setToState(null);
        //transition.setFromIf(null);
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
    public void setTransition(IIfTransition transition) {
        this.transition = transition;
    }

    /* (non-Javadoc)
     * @see org.eclipse.gef.commands.Command#undo()
     */
    public void undo() {
        transition.setFromIf(source);
        transition.setToState(target);
        source.setElseTransition(transition);
        //target.addInputTransition(transition);
    }
}
