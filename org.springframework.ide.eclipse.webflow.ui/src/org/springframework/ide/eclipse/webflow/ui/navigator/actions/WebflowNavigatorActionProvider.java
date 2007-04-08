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
package org.springframework.ide.eclipse.webflow.ui.navigator.actions;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionConstants;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.ICommonMenuConstants;

/**
 * @author Christian Dupuis
 * @since 2.0
 */
public class WebflowNavigatorActionProvider extends CommonActionProvider {

	private OpenConfigFileAction openConfigAction;
	private OpenPropertiesAction openPropertiesAction;
	private OpenWebflowGraphAction openGraphAction;
	private OpenActionWrapperAction openActionWrapperAction;

	public WebflowNavigatorActionProvider() {
	}

	@Override
	public void init(ICommonActionExtensionSite site) {
		openConfigAction = new OpenConfigFileAction(site);
		openPropertiesAction = new OpenPropertiesAction(site);
		openGraphAction = new OpenWebflowGraphAction(site);
		openActionWrapperAction = new OpenActionWrapperAction(site,
				openConfigAction, openGraphAction);
	}

	@Override
	public void fillContextMenu(IMenuManager menu) {
		if (openConfigAction.isEnabled()) {
			menu.appendToGroup(ICommonMenuConstants.GROUP_OPEN,
					openConfigAction);
		}
		if (openGraphAction.isEnabled()) {
			menu.appendToGroup(ICommonMenuConstants.GROUP_OPEN,
					openGraphAction);
		}
		if (openPropertiesAction.isEnabled()) {
			menu.appendToGroup(ICommonMenuConstants.GROUP_PROPERTIES,
					openPropertiesAction);
		}
	}

	@Override
	public void fillActionBars(IActionBars actionBars) {
		if (openActionWrapperAction.isEnabled()) {
			actionBars.setGlobalActionHandler(ICommonActionConstants.OPEN,
					openActionWrapperAction);
		}
		if (openPropertiesAction.isEnabled()) {
			actionBars.setGlobalActionHandler(ActionFactory.PROPERTIES.getId(),
					openPropertiesAction);
		}
	}
}
