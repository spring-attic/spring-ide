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
import org.springframework.ide.eclipse.web.flow.core.internal.model.StateTransition;
import org.springframework.ide.eclipse.web.flow.core.model.IStateTransition;
import org.springframework.ide.eclipse.web.flow.core.model.ITransitionableFrom;
import org.springframework.ide.eclipse.web.flow.core.model.ITransitionableTo;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowState;

public class CreateAndAssignSourceCommand extends Command {

    private ITransitionableTo child;

    private IWebFlowState parent;

    private ITransitionableFrom source;

    private IStateTransition transition;

    public void execute() {
        parent.addState(child);
        transition = new StateTransition(child, source, "*");
    }

    public void redo() {
        source.addOutputTransition(transition);
        child.addInputTransition(transition);
        parent.addState(child);
    }

    public void setChild(ITransitionableTo activity) {
        child = activity;
        child.setId(activity.getElementName());
    }

    public void setParent(IWebFlowState sa) {
        parent = sa;
    }

    public void setSource(ITransitionableFrom activity) {
        source = activity;
    }

    public void undo() {
        source.removeOutputTransition(transition);
        child.removeInputTransition(transition);
        parent.removeState(child);
    }
}