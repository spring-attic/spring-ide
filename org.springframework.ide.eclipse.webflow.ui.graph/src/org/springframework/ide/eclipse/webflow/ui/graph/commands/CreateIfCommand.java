/*
 * Copyright 2002-2007 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ide.eclipse.webflow.ui.graph.commands;

import org.eclipse.gef.commands.Command;
import org.eclipse.jface.dialogs.Dialog;
import org.springframework.ide.eclipse.webflow.core.model.IDecisionState;
import org.springframework.ide.eclipse.webflow.core.model.IIf;
import org.springframework.ide.eclipse.webflow.ui.graph.dialogs.DialogUtils;

/**
 * 
 */
public class CreateIfCommand extends Command {

    /**
     * 
     */
    private IIf child;

    /**
     * 
     */
    private int index = -1;

    /**
     * 
     */
    private boolean isMove = false;

    /**
     * 
     */
    private IDecisionState parent;

    /* (non-Javadoc)
     * @see org.eclipse.gef.commands.Command#execute()
     */
    public void execute() {
        if (!isMove) {
            if (DialogUtils.openPropertiesDialog(parent, child, true) != Dialog.OK) {
                return;
            }
        }
        if (index > 0)
            parent.addIf(child, index);
        else
            parent.addIf(child);
    }

    /**
     * 
     * 
     * @param action 
     */
    public void setChild(IIf action) {
        child = action;
    }

    /**
     * 
     * 
     * @param i 
     */
    public void setIndex(int i) {
        index = i;
    }

    /**
     * 
     * 
     * @param isMove 
     */
    public void setMove(boolean isMove) {
        this.isMove = isMove;
    }

    /**
     * 
     * 
     * @param sa 
     */
    public void setParent(IDecisionState sa) {
        parent = sa;
    }

    /* (non-Javadoc)
     * @see org.eclipse.gef.commands.Command#undo()
     */
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