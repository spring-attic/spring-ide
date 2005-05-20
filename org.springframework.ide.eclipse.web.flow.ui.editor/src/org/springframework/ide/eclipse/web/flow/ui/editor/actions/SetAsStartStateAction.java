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

package org.springframework.ide.eclipse.web.flow.ui.editor.actions;

import java.util.List;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.ui.IWorkbenchPart;
import org.springframework.ide.eclipse.web.flow.core.model.IEndState;
import org.springframework.ide.eclipse.web.flow.core.model.IState;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowState;
import org.springframework.ide.eclipse.web.flow.ui.editor.WebFlowImages;
import org.springframework.ide.eclipse.web.flow.ui.editor.parts.WebFlowPart;

public class SetAsStartStateAction extends
        org.eclipse.gef.ui.actions.SelectionAction {

    public static final String STARTSTATE_REQUEST = "Start_state";

    public static final String STARTSTATE = "Start_state";

    Request request;

    public SetAsStartStateAction(IWorkbenchPart part) {
        super(part);
        request = new Request(STARTSTATE_REQUEST);
        setText("Start state");
        setId(STARTSTATE);
        setToolTipText("Set selected state as start state");
        setImageDescriptor(WebFlowImages.DESC_OBJS_START_STATE);
        setHoverImageDescriptor(getImageDescriptor());
    }

    protected boolean calculateEnabled() {
        return canPerformAction();
    }

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
            if (part instanceof WebFlowPart)
                return false;
            if (part.getModel() instanceof IState
                    && !(part.getModel() instanceof IWebFlowState)) {
                IWebFlowState parent = (IWebFlowState) ((IState) part
                        .getModel()).getElementParent();
                if (parent.isStartState((IState) part.getModel())) {
                    return false;
                }
            }
        }
        return true;
    }

    private Command getCommand() {
        List editparts = getSelectedObjects();
        CompoundCommand cc = new CompoundCommand();
        for (int i = 0; i < editparts.size(); i++) {
            EditPart part = (EditPart) editparts.get(i);
            cc.add(part.getCommand(request));
        }
        return cc;
    }

    public void run() {
        execute(getCommand());
    }

}
