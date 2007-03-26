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
