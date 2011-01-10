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
import org.springframework.ide.eclipse.webflow.core.model.IStateTransition;
import org.springframework.ide.eclipse.webflow.core.model.ITransitionableFrom;
import org.springframework.ide.eclipse.webflow.core.model.ITransitionableTo;

/**
 * @author Christian Dupuis
 */
public class DeleteStateTransitionCommand extends Command {

    /**
     * 
     */
    private ITransitionableFrom source;

    /**
     * 
     */
    private ITransitionableTo target;

    /**
     * 
     */
    private IStateTransition transition;

    /* (non-Javadoc)
     * @see org.eclipse.gef.commands.Command#execute()
     */
    public void execute() {
        source.removeOutputTransition(transition);
        target.removeInputTransition(transition);
    }

    /**
     * 
     * 
     * @param activity 
     */
    public void setSource(ITransitionableFrom activity) {
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
    public void setTransition(IStateTransition transition) {
        this.transition = transition;
    }

    /* (non-Javadoc)
     * @see org.eclipse.gef.commands.Command#undo()
     */
    public void undo() {
    	source.addOutputTransition(transition);
    	target.addInputTransition(transition);
		transition.setToState(target);
		transition.setFromState(source);
    }
}
