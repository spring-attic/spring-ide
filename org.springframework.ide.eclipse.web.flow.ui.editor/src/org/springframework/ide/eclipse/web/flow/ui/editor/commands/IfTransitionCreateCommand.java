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
import org.springframework.ide.eclipse.web.flow.core.internal.model.IfTransition;
import org.springframework.ide.eclipse.web.flow.core.model.IIf;
import org.springframework.ide.eclipse.web.flow.core.model.IIfTransition;
import org.springframework.ide.eclipse.web.flow.core.model.ITransition;
import org.springframework.ide.eclipse.web.flow.core.model.ITransitionableTo;

public class IfTransitionCreateCommand extends Command {

    protected IIf source;

    protected ITransitionableTo target;

    protected IIfTransition transition;

    public boolean canExecute() {
        if (source.equals(target))
            return false;
        if (!(source instanceof IIf)) {
            return false;
        }
        // Check for existence of connection already
        if (source.getElseTransition() != null) {
            return false;
        }
        return true;
    }

    public void execute() {
        transition = new IfTransition(target, source, false);
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

    public void redo() {
        source.setElseTransition(transition);
        target.addInputTransition(transition);
    }

    public void setSource(IIf activity) {
        source = activity;
    }

    public void setTarget(ITransitionableTo activity) {
        target = activity;
    }

    public void setTransition(IfTransition transition) {
        this.transition = transition;
    }

    public void undo() {
        source.removeElseTransition();
        target.removeInputTransition(transition);
    }
}