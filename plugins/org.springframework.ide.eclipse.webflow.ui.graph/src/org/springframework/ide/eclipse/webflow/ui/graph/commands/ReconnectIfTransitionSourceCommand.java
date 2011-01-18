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
import org.springframework.ide.eclipse.webflow.core.model.ITransition;
import org.springframework.ide.eclipse.webflow.core.model.ITransitionableTo;

/**
 * @author Christian Dupuis
 */
public class ReconnectIfTransitionSourceCommand extends Command {

    /**
     * 
     */
    protected IIf oldSource;

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
        if (transition.getToState().equals(source) || !(source instanceof IIf))
            return false;
        return true;
    }

    /* (non-Javadoc)
     * @see org.eclipse.gef.commands.Command#execute()
     */
    public void execute() {
        if (source != null) {
            if (transition.isThen()) {
                oldSource.removeThenTransition();
            }
            else {
                oldSource.removeElseTransition();
            }
            transition.setFromIf(source);
            if (transition.isThen()) {
                source.setThenTransition(transition);
            }
            else {
                source.setElseTransition(transition);
            }
        }
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
     * @param trans 
     */
    public void setTransition(IIfTransition trans) {
        transition = trans;
        target = trans.getToState();
        oldSource = trans.getFromIf();
    }

    /* (non-Javadoc)
     * @see org.eclipse.gef.commands.Command#undo()
     */
    public void undo() {
        if (source != null) {
            if (transition.isThen()) {
                source.removeThenTransition();
            }
            else {
                source.removeElseTransition();
            }
            transition.setFromIf(oldSource);
            if (transition.isThen()) {
                oldSource.setThenTransition(transition);
            }
            else {
                oldSource.setElseTransition(transition);
            }
        }
    }
}
