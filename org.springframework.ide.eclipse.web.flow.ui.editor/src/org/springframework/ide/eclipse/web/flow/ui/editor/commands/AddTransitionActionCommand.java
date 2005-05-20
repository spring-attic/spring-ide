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
import org.springframework.ide.eclipse.web.flow.core.model.IAction;
import org.springframework.ide.eclipse.web.flow.core.model.IStateTransition;
import org.springframework.ide.eclipse.web.flow.ui.editor.dialogs.DialogUtils;

public class AddTransitionActionCommand extends Command {

    int index = -1;

    private IAction newAction;

    private IStateTransition transition;

    public void execute() {
        newAction.setElementParent(transition);
        if (Dialog.OK != DialogUtils.openPropertiesDialog(transition,
                newAction, true)) {
            return;
        }
        transition.addAction(newAction);
        index = transition.getActions().indexOf(newAction);
    }

    public void redo() {
        transition.addAction(newAction, index);
    }

    public void setNewAction(IAction activity) {
        newAction = activity;
    }

    public void setTransition(IStateTransition transition) {
        this.transition = transition;
    }

    public void undo() {
        transition.removeAction(newAction);
    }

}