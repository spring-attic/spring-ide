/*
 * Copyright 2002-2007 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
 * 
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