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
import org.springframework.ide.eclipse.web.flow.core.internal.model.Transition;
import org.springframework.ide.eclipse.web.flow.core.model.IAction;
import org.springframework.ide.eclipse.web.flow.core.model.IAttributeMapper;
import org.springframework.ide.eclipse.web.flow.core.model.IDecisionState;
import org.springframework.ide.eclipse.web.flow.core.model.IEndState;
import org.springframework.ide.eclipse.web.flow.core.model.ITransition;
import org.springframework.ide.eclipse.web.flow.core.model.ITransitionableFrom;
import org.springframework.ide.eclipse.web.flow.core.model.ITransitionableTo;

public class ConnectionCreateCommand extends Command {

    protected ITransitionableFrom source;

    protected ITransitionableTo target;

    protected ITransition transition;

    public boolean canExecute() {
        if (source.equals(target))
            return false;
        if (source instanceof IEndState || source instanceof IDecisionState
                || source instanceof IAttributeMapper
                || source instanceof IAction) {
            return false;
        }

        return true;
    }

    public void execute() {
        transition = new StateTransition(target, source, "*");
    }

    public ITransitionableFrom getSource() {
        return source;
    }

    public ITransitionableTo getTarget() {
        return target;
    }

    public ITransition getTransition() {
        return transition;
    }

    public void redo() {
        source.addOutputTransition(transition);
        target.addInputTransition(transition);
    }

    public void setSource(ITransitionableFrom activity) {
        source = activity;
    }

    public void setTarget(ITransitionableTo activity) {
        target = activity;
    }

    public void setTransition(Transition transition) {
        this.transition = transition;
    }

    public void undo() {
        source.removeOutputTransition(transition);
        target.removeInputTransition(transition);
    }

}