/*******************************************************************************
 *  Copyright (c) 2012 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.config.graph.actions;

import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.actions.GEFActionConstants;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.ui.actions.ActionFactory;

/**
 * @author Leo Dos Santos
 */
public class SpringConfigContextMenuProvider extends ContextMenuProvider {

	private final ActionRegistry registry;

	public SpringConfigContextMenuProvider(EditPartViewer viewer, ActionRegistry registry) {
		super(viewer);
		this.registry = registry;
	}

	@Override
	public void buildContextMenu(IMenuManager menu) {
		GEFActionConstants.addStandardActionGroups(menu);

		IAction action = registry.getAction(ActionFactory.UNDO.getId());
		menu.appendToGroup(GEFActionConstants.GROUP_UNDO, action);

		action = registry.getAction(ActionFactory.REDO.getId());
		menu.appendToGroup(GEFActionConstants.GROUP_UNDO, action);

		action = registry.getAction(ActionFactory.DELETE.getId());
		if (action.isEnabled()) {
			menu.appendToGroup(GEFActionConstants.GROUP_EDIT, action);
		}

		action = registry.getAction(ResetManualLayoutAction.RESET_LAYOUT_ID);
		menu.appendToGroup(GEFActionConstants.GROUP_EDIT, action);

		action = registry.getAction(ShowPropertiesAction.SHOW_PROPERTIES_ID);
		if (action.isEnabled()) {
			menu.appendToGroup(GEFActionConstants.GROUP_VIEW, action);
		}

		action = registry.getAction(ShowSourceAction.SHOW_SOURCE_ID);
		if (action.isEnabled()) {
			menu.appendToGroup(GEFActionConstants.GROUP_VIEW, action);
		}
	}

	public ActionRegistry getActionRegistry() {
		return registry;
	}

}
