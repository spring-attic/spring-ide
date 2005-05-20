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
import org.springframework.ide.eclipse.web.flow.core.model.ITransitionableTo;

public class DeleteIfTransitionCommand extends Command {

    private IIf source;

    private ITransitionableTo target;

    private IIfTransition transition;

    public void execute() {

        source.removeElseTransition();
        target.removeInputTransition(transition);
        //transition.setToState(null);
        //transition.setFromIf(null);
    }

    public void setSource(IIf activity) {
        source = activity;
    }

    public void setTarget(ITransitionableTo activity) {
        target = activity;
    }

    public void setTransition(IIfTransition transition) {
        this.transition = transition;
    }

    public void undo() {
        transition.setFromIf(source);
        transition.setToState(target);
        source.setElseTransition(transition);
        //target.addInputTransition(transition);
    }
}