/*
 * Copyright 2002-2005 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.springframework.ide.eclipse.web.flow.ui.editor.commands;

import org.eclipse.gef.commands.Command;
import org.eclipse.jface.dialogs.Dialog;
import org.springframework.ide.eclipse.web.flow.core.internal.model.IfTransition;
import org.springframework.ide.eclipse.web.flow.core.internal.model.StateTransition;
import org.springframework.ide.eclipse.web.flow.core.model.IIf;
import org.springframework.ide.eclipse.web.flow.core.model.IIfTransition;
import org.springframework.ide.eclipse.web.flow.core.model.IState;
import org.springframework.ide.eclipse.web.flow.core.model.IStateTransition;
import org.springframework.ide.eclipse.web.flow.core.model.ITransitionableFrom;
import org.springframework.ide.eclipse.web.flow.core.model.ITransitionableTo;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowState;
import org.springframework.ide.eclipse.web.flow.ui.editor.dialogs.DialogUtils;

public class SplitIfTransitionCommand extends Command {

    private IState newActivity;

    private IIfTransition newIncomingTransition;

    private IStateTransition newOutgoingTransition;

    private IIf oldSource;

    private ITransitionableTo oldTarget;

    private IWebFlowState parent;

    private IIfTransition transition;

    public void execute() {
        newActivity.setElementParent(parent);
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
                    (ITransitionableFrom) newActivity, "*");
    }

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

    public void setNewActivity(IState activity) {
        newActivity = activity;
        newActivity.setId(activity.getElementName());
    }

    public void setParent(IWebFlowState activity) {
        parent = activity;
    }

    public void setTransition(IIfTransition transition) {
        this.transition = transition;
        oldSource = transition.getFromIf();
        oldTarget = transition.getToState();
    }

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