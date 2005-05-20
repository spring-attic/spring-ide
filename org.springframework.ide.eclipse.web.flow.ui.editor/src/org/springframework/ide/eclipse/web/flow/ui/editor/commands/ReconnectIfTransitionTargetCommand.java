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
import org.springframework.ide.eclipse.web.flow.core.model.IIf;
import org.springframework.ide.eclipse.web.flow.core.model.IIfTransition;
import org.springframework.ide.eclipse.web.flow.core.model.IState;
import org.springframework.ide.eclipse.web.flow.core.model.ITransition;
import org.springframework.ide.eclipse.web.flow.core.model.ITransitionableTo;

public class ReconnectIfTransitionTargetCommand extends Command {

    protected ITransitionableTo oldTarget;

    protected IIf source;

    protected ITransitionableTo target;

    protected IIfTransition transition;

    public boolean canExecute() {
        if (transition.getToState().equals(target)
                || !(target instanceof IState))
            return false;
        return true;
    }

    public void execute() {
        if (target != null) {
            //oldTarget.removeInputTransition(transition);
            transition.setToState(target);
            //target.addInputTransition(transition);
        }
    }

    public IIf getSource() {
        return source;
    }

    public ITransitionableTo getTarget() {
        return target;
    }

    public ITransition getTransition() {
        return transition;
    }

    public void setSource(IIf activity) {
        source = activity;
    }

    public void setTarget(ITransitionableTo activity) {
        target = activity;
    }

    public void setTransition(IIfTransition trans) {
        transition = trans;
        source = trans.getFromIf();
        oldTarget = trans.getToState();
    }

    public void undo() {
        //target.removeInputTransition(transition);
        transition.setToState(oldTarget);
        //oldTarget.addInputTransition(transition);
    }
}