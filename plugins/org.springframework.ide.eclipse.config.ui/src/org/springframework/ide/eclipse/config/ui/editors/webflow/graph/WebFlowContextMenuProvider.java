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
package org.springframework.ide.eclipse.config.ui.editors.webflow.graph;

import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.actions.GEFActionConstants;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.springframework.ide.eclipse.config.graph.actions.SpringConfigContextMenuProvider;


/**
 * @author Leo Dos Santos
 */
public class WebFlowContextMenuProvider extends SpringConfigContextMenuProvider {

	public WebFlowContextMenuProvider(EditPartViewer viewer, ActionRegistry registry) {
		super(viewer, registry);
	}

	@Override
	public void buildContextMenu(IMenuManager menu) {
		super.buildContextMenu(menu);
		IAction action = getActionRegistry().getAction(ToggleActionStateAction.ID_ACTION);
		if (action.isEnabled()) {
			menu.appendToGroup(GEFActionConstants.GROUP_REST, action);
		}

		action = getActionRegistry().getAction(ToggleDecisionStateAction.ID_ACTION);
		if (action.isEnabled()) {
			menu.appendToGroup(GEFActionConstants.GROUP_REST, action);
		}

		action = getActionRegistry().getAction(ToggleEndStateAction.ID_ACTION);
		if (action.isEnabled()) {
			menu.appendToGroup(GEFActionConstants.GROUP_REST, action);
		}

		action = getActionRegistry().getAction(ToggleSubflowStateAction.ID_ACTION);
		if (action.isEnabled()) {
			menu.appendToGroup(GEFActionConstants.GROUP_REST, action);
		}

		action = getActionRegistry().getAction(ToggleViewStateAction.ID_ACTION);
		if (action.isEnabled()) {
			menu.appendToGroup(GEFActionConstants.GROUP_REST, action);
		}
	}

}
