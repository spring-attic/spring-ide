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
import org.springframework.ide.eclipse.web.flow.core.model.IAttribute;
import org.springframework.ide.eclipse.web.flow.core.model.IAttributeEnabled;

public class DeleteActionPropertyCommand extends Command {

    private IAttribute child;

    private int index = -1;

    private IAttributeEnabled parent;

    public void execute() {
        primExecute();
    }

    protected void primExecute() {
        index = parent.getProperties().indexOf(child);
        parent.removeProperty(child);
    }

    public void redo() {
        primExecute();
    }

    public void setChild(IAttribute a) {
        child = a;
    }

    public void setParent(IAction sa) {
        parent = (IAttributeEnabled) sa;
    }

    public void undo() {
        parent.addProperty(child, index);
    }
}