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

import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.actions.GEFActionConstants;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.ui.actions.ActionFactory;

/**
 * 
 */
public class WebflowContextMenuProvider extends ContextMenuProvider {

    /**
     * 
     */
    private ActionRegistry actionRegistry;

    /**
     * Creates a new FlowContextMenuProvider assoicated with the given viewer
     * and action registry.
     * 
     * @param viewer the viewer
     * @param registry the action registry
     */
    public WebflowContextMenuProvider(EditPartViewer viewer,
            ActionRegistry registry) {
        super(viewer);
        setActionRegistry(registry);
    }

    /**
     * 
     * 
     * @param menu 
     * 
     * @see ContextMenuProvider#buildContextMenu(org.eclipse.jface.action.IMenuManager)
     */
    public void buildContextMenu(IMenuManager menu) {
        GEFActionConstants.addStandardActionGroups(menu);

        IAction action;
        action = getActionRegistry().getAction(ActionFactory.UNDO.getId());
        menu.appendToGroup(GEFActionConstants.GROUP_UNDO, action);

        action = getActionRegistry().getAction(ActionFactory.REDO.getId());
        menu.appendToGroup(GEFActionConstants.GROUP_UNDO, action);

        action = getActionRegistry().getAction(ExportAction.ID);
        menu.appendToGroup(GEFActionConstants.GROUP_UNDO, action);

        action = getActionRegistry().getAction(OpenConfigFileAction.OPEN_FILE);
        if (action.isEnabled())
            menu.appendToGroup(GEFActionConstants.GROUP_UNDO, action);

        action = getActionRegistry().getAction(OpenBeansGraphAction.OPEN_FILE);
        if (action.isEnabled())
            menu.appendToGroup(GEFActionConstants.GROUP_UNDO, action);

        action = getActionRegistry().getAction(OpenBeansConfigAction.OPEN_FILE);
        if (action.isEnabled())
        	menu.appendToGroup(GEFActionConstants.GROUP_UNDO, action);
        
        action = getActionRegistry().getAction(ActionFactory.DELETE.getId());
        if (action.isEnabled())
            menu.appendToGroup(GEFActionConstants.GROUP_EDIT, action);

        action = getActionRegistry()
                .getAction(SetAsStartStateAction.STARTSTATE);
        if (action.isEnabled())
            menu.appendToGroup(GEFActionConstants.GROUP_EDIT, action);

        action = getActionRegistry().getAction(
                EditPropertiesAction.EDITPROPERTIES);
        if (action.isEnabled())
            menu.appendToGroup(GEFActionConstants.GROUP_EDIT, action);

    }

    /**
     * 
     * 
     * @return 
     */
    private ActionRegistry getActionRegistry() {
        return actionRegistry;
    }

    /**
     * Sets the action registry.
     * 
     * @param registry the action registry
     */
    public void setActionRegistry(ActionRegistry registry) {
        actionRegistry = registry;
    }

}