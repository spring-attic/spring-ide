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

package org.springframework.ide.eclipse.webflow.ui.graph.actions;

import java.util.List;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.ui.IWorkbenchPart;
import org.springframework.ide.eclipse.webflow.core.model.IActionElement;
import org.springframework.ide.eclipse.webflow.core.model.IIf;
import org.springframework.ide.eclipse.webflow.core.model.IState;
import org.springframework.ide.eclipse.webflow.core.model.IStateTransition;
import org.springframework.ide.eclipse.webflow.ui.editor.namespaces.webflow.WebflowUIImages;

/**
 * 
 */
public class EditPropertiesAction extends
        org.eclipse.gef.ui.actions.SelectionAction {

    /**
     * 
     */
    public static final String EDITPROPERTIES_REQUEST = "Edit_propeties";

    /**
     * 
     */
    public static final String EDITPROPERTIES = "Edit_propeties";

    /**
     * 
     */
    Request request;

    /**
     * 
     * 
     * @param part 
     */
    public EditPropertiesAction(IWorkbenchPart part) {
        super(part);
        request = new Request(EDITPROPERTIES_REQUEST);
        setText("Properties");
        setId(EDITPROPERTIES);
        setToolTipText("Edit properties of selected state");
        setImageDescriptor(WebflowUIImages.DESC_OBJS_PROPERTIES);
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
            if (!(part.getModel() instanceof IState
                    || part.getModel() instanceof IActionElement
                    || part.getModel() instanceof IStateTransition || part
                    .getModel() instanceof IIf))
                return false;
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
    
    /**
     * 
     * 
     * @param command 
     */
    public void runWithCommand(Command command) {
    	execute(command);
    }

}