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
import org.springframework.ide.eclipse.web.flow.core.model.IDecisionState;
import org.springframework.ide.eclipse.web.flow.core.model.IIf;

public class AddIfCommand extends Command {

    private IIf child;

    private IDecisionState parent;

    public void execute() {
        parent.addIf(child);
    }

    public IDecisionState getParent() {
        return parent;
    }

    public void setChild(IIf newChild) {
        child = newChild;
    }

    public void setParent(IDecisionState newParent) {
        parent = newParent;
    }

    public void undo() {
        if (child.getThenTransition() != null) {
            child.getThenTransition().getToState().removeInputTransition(
                    child.getThenTransition());
        }

        if (child.getElseTransition() != null) {
            child.getElseTransition().getToState().removeInputTransition(
                    child.getElseTransition());
        }
        parent.removeIf(child);
    }

}