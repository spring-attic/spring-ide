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
import org.springframework.ide.eclipse.web.flow.core.model.IAction;
import org.springframework.ide.eclipse.web.flow.core.model.IProperty;
import org.springframework.ide.eclipse.web.flow.core.model.IState;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowModelElement;

public class CreatePropertyCommand extends Command {

    private IProperty child;

    private int index = -1;

    private IWebFlowModelElement parent;

    public void execute() {
        child.setElementParent(parent);
        if (parent instanceof IState) {
            if (index >= 0)
                ((IState) parent).addProperty(child, index);
            else
                ((IState) parent).addProperty(child);
        }
        else if (parent instanceof IAction) {
            if (index >= 0)
                ((IAction) parent).addProperty(child, index);
            else
                ((IAction) parent).addProperty(child);
        }
    }

    public void setChild(IProperty action) {
        child = action;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public void setParent(IWebFlowModelElement sa) {
        parent = sa;
    }

    public void undo() {
        if (parent instanceof IState) {
            ((IState) parent).removeProperty(child);
        }
        else if (parent instanceof IAction) {
            ((IAction) parent).removeProperty(child);
        }
    }
}