/*******************************************************************************
 * Copyright (c) 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.webflow.ui.graph.commands;

import org.eclipse.gef.commands.Command;
import org.eclipse.jface.dialogs.Dialog;
import org.springframework.ide.eclipse.webflow.core.model.IDecisionState;
import org.springframework.ide.eclipse.webflow.core.model.IIf;
import org.springframework.ide.eclipse.webflow.ui.graph.dialogs.DialogUtils;

/**
 * @author Christian Dupuis
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
