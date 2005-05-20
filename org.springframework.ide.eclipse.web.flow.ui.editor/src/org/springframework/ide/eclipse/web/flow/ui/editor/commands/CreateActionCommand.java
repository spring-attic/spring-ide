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
import org.springframework.ide.eclipse.web.flow.core.model.IActionState;
import org.springframework.ide.eclipse.web.flow.ui.editor.dialogs.DialogUtils;

public class CreateActionCommand extends Command {

    private IAction child;

    private int index = -1;

    private boolean isMove = false;

    private IActionState parent;

    public void execute() {
        child.setElementParent(parent);
        if (!isMove) {
            if (DialogUtils.openPropertiesDialog(null, child, true) != Dialog.OK) {
                return;
            }
        }
        if (index > 0)
            parent.addAction(child, index);
        else
            parent.addAction(child);
    }

    public void setChild(IAction action) {
        child = action;
    }

    public void setIndex(int i) {
        index = i;
    }

    public void setMove(boolean isMove) {
        this.isMove = isMove;
    }

    public void setParent(IActionState sa) {
        parent = sa;
    }

    public void undo() {
        parent.removeAction(child);
    }
}