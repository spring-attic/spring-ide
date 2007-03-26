/*******************************************************************************
 * Copyright (c) 2005, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.webflow.ui.graph.actions;

import java.util.List;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.ui.IWorkbenchPart;
import org.springframework.ide.eclipse.webflow.core.model.IEndState;
import org.springframework.ide.eclipse.webflow.core.model.IState;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowState;
import org.springframework.ide.eclipse.webflow.ui.editor.namespaces.webflow.WebflowUIImages;
import org.springframework.ide.eclipse.webflow.ui.graph.parts.FlowPart;

/**
 * 
 */
public class SetAsStartStateAction extends
        org.eclipse.gef.ui.actions.SelectionAction {

    /**
     * 
     */
    public static final String STARTSTATE_REQUEST = "Start_state";

    /**
     * 
     */
    public static final String STARTSTATE = "Start_state";

    /**
     * 
     */
    Request request;

    /**
     * 
     * 
     * @param part 
     */
    public SetAsStartStateAction(IWorkbenchPart part) {
        super(part);
        request = new Request(STARTSTATE_REQUEST);
        setText("Start state");
        setId(STARTSTATE);
        setToolTipText("Set selected state as start state");
        setImageDescriptor(WebflowUIImages.DESC_OBJS_START_STATE);
        setHoverImageDescriptor(getImageDescriptor());
    }

    /* (non-Javadoc)
     * @see org.eclipse.gef.ui.actions.WorkbenchPartAction#calculateEnabled()
     */
    protected boolean calculateEnabled() {
        return canPerformAction();
    }

    /**
     * 
     * 
     * @return 
     */
    private boolean canPerformAction() {
        if (getSelectedObjects().isEmpty())
            return false;
        List parts = getSelectedObjects();
        for (int i = 0; i < parts.size(); i++) {
            Object o = parts.get(i);
            if (!(o instanceof EditPart))
                return false;
            EditPart part = (EditPart) o;
            if (!(part.getModel() instanceof IState)
                    || part.getModel() instanceof IEndState)
                return false;
            if (part instanceof FlowPart)
                return false;
            if (part.getModel() instanceof IState
                    && !(part.getModel() instanceof IWebflowState)) {
                IWebflowState parent = (IWebflowState) ((IState) part
                        .getModel()).getElementParent();
                if (parent.isStartState((IState) part.getModel())) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 
     * 
     * @return 
     */
    private Command getCommand() {
        List editparts = getSelectedObjects();
        CompoundCommand cc = new CompoundCommand();
        for (int i = 0; i < editparts.size(); i++) {
            EditPart part = (EditPart) editparts.get(i);
            cc.add(part.getCommand(request));
        }
        return cc;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.Action#run()
     */
    public void run() {
        execute(getCommand());
    }

}
