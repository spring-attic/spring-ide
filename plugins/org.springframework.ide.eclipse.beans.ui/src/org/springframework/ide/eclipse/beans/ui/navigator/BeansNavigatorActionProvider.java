/*******************************************************************************
 * Copyright (c) 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.navigator;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionConstants;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.ICommonMenuConstants;
import org.springframework.ide.eclipse.beans.ui.navigator.actions.OpenActionWrapperAction;
import org.springframework.ide.eclipse.beans.ui.navigator.actions.OpenConfigFileAction;
import org.springframework.ide.eclipse.beans.ui.navigator.actions.OpenJavaElementAction;
import org.springframework.ide.eclipse.beans.ui.navigator.actions.OpenPropertiesAction;
import org.springframework.ide.eclipse.beans.ui.navigator.actions.OpenReferenceAction;
import org.springframework.ide.eclipse.ui.navigator.actions.ValidationAction;

/**
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 */
public class BeansNavigatorActionProvider extends CommonActionProvider {

	private OpenConfigFileAction openConfigAction;
	private OpenReferenceAction openReferenceAction;
	private OpenJavaElementAction openElementAction;
	private OpenPropertiesAction openPropertiesAction;
	private OpenActionWrapperAction openAction;
	private ValidationAction validationAction;

	public BeansNavigatorActionProvider() {
	}

	@Override
	public void init(ICommonActionExtensionSite site) {
		openConfigAction = new OpenConfigFileAction(site);
		openReferenceAction = new OpenReferenceAction(site);
		openElementAction = new OpenJavaElementAction(site);
		openPropertiesAction = new OpenPropertiesAction(site);
		openAction = new OpenActionWrapperAction(site, openConfigAction,
				openElementAction);
		validationAction = new ValidationAction(site);
	}

	@Override
	public void fillContextMenu(IMenuManager menu) {
		if (openConfigAction.isEnabled()) {
			menu.appendToGroup(ICommonMenuConstants.GROUP_OPEN,
					openConfigAction);
		}
		if (openReferenceAction.isEnabled()) {
			menu.appendToGroup(ICommonMenuConstants.GROUP_OPEN,
					openReferenceAction);
		}
		if (openElementAction.isEnabled()) {
			menu.appendToGroup(ICommonMenuConstants.GROUP_OPEN,
					openElementAction);
		}
		if (validationAction.isEnabled()) {
			menu.appendToGroup(ICommonMenuConstants.GROUP_BUILD,
					validationAction);
		}
		if (openPropertiesAction.isEnabled()) {
			menu.appendToGroup(ICommonMenuConstants.GROUP_PROPERTIES,
					openPropertiesAction);
		}
	}

	@Override
	public void fillActionBars(IActionBars actionBars) {
		if (openAction.isEnabled()) {
			actionBars.setGlobalActionHandler(ICommonActionConstants.OPEN,
					openAction);
		}
		if (openPropertiesAction.isEnabled()) {
			actionBars.setGlobalActionHandler(ActionFactory.PROPERTIES.getId(),
					openPropertiesAction);
		}
	}
}
