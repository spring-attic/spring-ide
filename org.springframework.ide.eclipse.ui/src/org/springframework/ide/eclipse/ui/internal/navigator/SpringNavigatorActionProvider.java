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
package org.springframework.ide.eclipse.ui.internal.navigator;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.ide.IDEActionFactory;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.ICommonMenuConstants;
import org.springframework.ide.eclipse.ui.SpringUIPlugin;

/**
 * @author Torsten Juergeleit
 * @since 2.0
 */
public class SpringNavigatorActionProvider extends CommonActionProvider {

	private IWorkbenchAction openPropertiesAction;

	public SpringNavigatorActionProvider() {
	}

	@Override
	public void init(ICommonActionExtensionSite site) {
		openPropertiesAction = IDEActionFactory.OPEN_PROJECT_PROPERTIES
				.create(SpringUIPlugin.getActiveWorkbenchWindow());
	}

	@Override
	public void fillContextMenu(IMenuManager menu) {
		if (openPropertiesAction.isEnabled()) {
			menu.appendToGroup(ICommonMenuConstants.GROUP_PROPERTIES,
					openPropertiesAction);
		}
	}

	@Override
	public void fillActionBars(IActionBars actionBars) {
		if (openPropertiesAction.isEnabled()) {
			actionBars.setGlobalActionHandler(ActionFactory.PROPERTIES.getId(),
					openPropertiesAction);
		}
	}
}
