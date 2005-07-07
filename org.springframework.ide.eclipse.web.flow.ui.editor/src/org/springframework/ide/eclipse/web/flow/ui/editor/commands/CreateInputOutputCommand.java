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
import org.springframework.ide.eclipse.web.flow.core.model.IAttributeMapper;
import org.springframework.ide.eclipse.web.flow.core.model.IInput;
import org.springframework.ide.eclipse.web.flow.core.model.IOutput;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowModelElement;

public class CreateInputOutputCommand extends Command {

    private Object child;

    private int index = -1;

    private boolean isMove = false;

    private IAttributeMapper parent;

    public void execute() {
        ((IWebFlowModelElement) child).setElementParent(parent);
        if (child instanceof IInput) {
            if (index > 0)
                parent.addInput((IInput) child, index);
            else
                parent.addInput((IInput) child);
        } else if (child instanceof IOutput) {
            if (index > 0)
                parent.addOutput((IOutput) child, index);
            else
                parent.addOutput((IOutput) child);
        }
    }

    public void setChild(Object action) {
        child = action;
    }

    public void setIndex(int i) {
        index = i;
    }

    public void setMove(boolean isMove) {
        this.isMove = isMove;
    }

    public void setParent(IAttributeMapper sa) {
        parent = sa;
    }

    public void undo() {
        if (child instanceof IInput) {
            parent.removeInput((IInput) child);
        } else if (child instanceof IOutput) {
            parent.removeOutput((IOutput) child);
        }
    }
}