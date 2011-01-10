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
import org.springframework.ide.eclipse.webflow.core.model.IState;
import org.springframework.ide.eclipse.webflow.core.model.IStateTransition;
import org.springframework.ide.eclipse.webflow.core.model.ITransitionableFrom;
import org.springframework.ide.eclipse.webflow.core.model.ITransitionableTo;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowState;

/**
 * @author Christian Dupuis
 */
public class SplitIfTransitionCommand extends Command {

    /**
     * 
     */
    private IState newActivity;

    /**
     * 
     */
    private IIfTransition newIncomingTransition;

    /**
     * 
     */
    private IStateTransition newOutgoingTransition;

    /**
     * 
     */
    private IIf oldSource;

    /**
     * 
     */
    private ITransitionableTo oldTarget;

    /**
     * 
     */
    private IWebflowState parent;

    /**
     * 
     */
    private IIfTransition transition;

    /* (non-Javadoc)
     * @see org.eclipse.gef.commands.Command#execute()
     */
    public void execute() {
    	/*newActivity.setElementParent(parent);
        int result = DialogUtils
                .openPropertiesDialog(parent, newActivity, true);
        if (result != Dialog.OK) {
            return;
        }
        oldTarget.removeInputTransition(transition);
        parent.addState(newActivity);
        if (newActivity instanceof ITransitionableTo) {
            newIncomingTransition = new IfTransition(
                    (ITransitionableTo) newActivity, oldSource, transition
                            .isThen());
            if (!transition.isThen()) {
                oldSource.setElseTransition(newIncomingTransition);
            }
            else {
                oldSource.setThenTransition(newIncomingTransition);
            }
        }
        if (newActivity instanceof ITransitionableFrom)
            newOutgoingTransition = new StateTransition(oldTarget,
                    (ITransitionableFrom) newActivity, "*");*/
    }

    /* (non-Javadoc)
     * @see org.eclipse.gef.commands.Command#redo()
     */
    public void redo() {
        if (!transition.isThen()) {
            oldSource.setElseTransition(newIncomingTransition);
        }
        else {
            oldSource.setThenTransition(newIncomingTransition);
        }
        oldTarget.addInputTransition(newOutgoingTransition);
        if (newActivity instanceof ITransitionableTo)
            ((ITransitionableTo) newActivity)
                    .addInputTransition(newIncomingTransition);
        if (newActivity instanceof ITransitionableFrom)
            ((ITransitionableFrom) newActivity)
                    .addOutputTransition(newOutgoingTransition);
        parent.addState(newActivity);
        /*if (!transition.isThen()) {
         oldSource.removeElseTransition();
         }
         else {
         oldSource.removeThenTransition();
         }*/
        oldTarget.removeInputTransition(transition);
    }

    /**
     * 
     * 
     * @param activity 
     */
    public void setNewActivity(IState activity) {
        newActivity = activity;
    }

    /**
     * 
     * 
     * @param activity 
     */
    public void setParent(IWebflowState activity) {
        parent = activity;
    }

    /**
     * 
     * 
     * @param transition 
     */
    public void setTransition(IIfTransition transition) {
        this.transition = transition;
        oldSource = transition.getFromIf();
        oldTarget = transition.getToState();
    }

    /* (non-Javadoc)
     * @see org.eclipse.gef.commands.Command#undo()
     */
    public void undo() {
        /*if (!transition.isThen()) {
         oldSource.removeElseTransition();
         }
         else {
         oldSource.removeThenTransition();
         }*/
        oldTarget.removeInputTransition(newOutgoingTransition);
        if (newActivity instanceof ITransitionableTo)
            ((ITransitionableTo) newActivity)
                    .removeInputTransition(newIncomingTransition);
        if (newActivity instanceof ITransitionableFrom)
            ((ITransitionableFrom) newActivity)
                    .removeOutputTransition(newOutgoingTransition);
        parent.removeState(newActivity);
        if (!transition.isThen()) {
            oldSource.setElseTransition(transition);
        }
        else {
            oldSource.setThenTransition(transition);
        }
        oldTarget.addInputTransition(transition);
    }
}
