/*
 * Copyright 2002-2004 the original author or authors.
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

package org.springframework.ide.eclipse.beans.ui.graph.actions;

import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.actions.GEFActionConstants;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;

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

		action = getActionRegistry().getAction(
											 GraphActionConstants.SHOW_IN_VIEW);
		if (action.isEnabled()) {
			menu.appendToGroup(GEFActionConstants.GROUP_VIEW, action);
		}
	}
}
