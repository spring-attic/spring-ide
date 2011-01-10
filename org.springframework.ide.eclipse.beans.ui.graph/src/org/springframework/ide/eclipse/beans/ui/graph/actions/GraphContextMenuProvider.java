/*******************************************************************************
 * Copyright (c) 2004, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.graph.actions;

import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.actions.GEFActionConstants;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;

/**
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 */
public class GraphContextMenuProvider extends ContextMenuProvider {

	private ActionRegistry actionRegistry;

	public GraphContextMenuProvider(
		EditPartViewer viewer,
		ActionRegistry registry) {
		super(viewer);
		setActionRegistry(registry);
	}

	public void setActionRegistry(ActionRegistry registry) {
		actionRegistry = registry;
	}

	private ActionRegistry getActionRegistry() {
		return actionRegistry;
	}

	@Override
	public void buildContextMenu(IMenuManager menu) {
		GEFActionConstants.addStandardActionGroups(menu);
		IAction action;

		action = getActionRegistry().getAction(GraphActionConstants.OPEN_TYPE);
		if (action.isEnabled()) {
			menu.appendToGroup(GEFActionConstants.GROUP_VIEW, action);
		}

		action = getActionRegistry().getAction(GraphActionConstants.OPEN_FILE);
		if (action.isEnabled()) {
			menu.appendToGroup(GEFActionConstants.GROUP_VIEW, action);
		}
	}
}
