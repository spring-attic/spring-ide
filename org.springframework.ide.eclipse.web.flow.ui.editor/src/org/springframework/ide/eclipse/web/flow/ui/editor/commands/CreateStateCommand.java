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
import org.springframework.ide.eclipse.web.flow.core.model.IState;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowState;
import org.springframework.ide.eclipse.web.flow.ui.editor.dialogs.DialogUtils;

public class CreateStateCommand extends Command {

    private IState child;

    private int index = -1;

    private IWebFlowState parent;

    private int result;

    public void execute() {
        child.setElementParent(parent);
        result = DialogUtils.openPropertiesDialog(parent, child, true);
        if (result != Dialog.OK) {
            return;
        }
        if (index > 0) {
            parent.addState(child, index);
        }
        else {
            parent.addState(child);
        }
    }

    public void redo() {
        if (result != Dialog.OK) {
            return;
        }
        child.setElementParent(parent);
        if (index > 0)
            parent.addState(child, index);
        else
            parent.addState(child);
    }

    public void setChild(IState activity) {
        child = activity;
        child.setId(child.getElementName());
    }

    public void setIndex(int i) {
        index = i;
    }

    public void setParent(IWebFlowState sa) {
        parent = sa;
    }

    public void undo() {
        parent.removeState(child);
    }
}