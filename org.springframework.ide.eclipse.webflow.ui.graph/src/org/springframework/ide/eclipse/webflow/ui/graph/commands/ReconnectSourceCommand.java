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
import org.springframework.ide.eclipse.webflow.core.model.ITransition;
import org.springframework.ide.eclipse.webflow.core.model.ITransitionableFrom;
import org.springframework.ide.eclipse.webflow.core.model.ITransitionableTo;

/**
 * @author Christian Dupuis
 */
public class ReconnectSourceCommand extends Command {

    /**
     * 
     */
    protected ITransitionableFrom oldSource;

    /**
     * 
     */
    protected ITransitionableFrom source;

    /**
     * 
     */
    protected ITransitionableTo target;

    /**
     * 
     */
    protected IStateTransition transition;

    /* (non-Javadoc)
     * @see org.eclipse.gef.commands.Command#canExecute()
     */
    public boolean canExecute() {
        if (transition.getToState().equals(source))
            return false;
        return true;
    }

    /* (non-Javadoc)
     * @see org.eclipse.gef.commands.Command#execute()
     */
    public void execute() {
        if (source != null) {
            oldSource.removeOutputTransition(transition);
            transition.setFromState(source);
            source.addOutputTransition(transition);
        }
    }

    /**
     * 
     * 
     * @return 
     */
    public ITransitionableFrom getSource() {
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
     * @param trans 
     */
    public void setTransition(IStateTransition trans) {
        transition = trans;
        target = trans.getToState();
        oldSource = trans.getFromState();
    }

    /* (non-Javadoc)
     * @see org.eclipse.gef.commands.Command#undo()
     */
    public void undo() {
        source.removeOutputTransition(transition);
        transition.setFromState(oldSource);
        oldSource.addOutputTransition(transition);
    }
}
