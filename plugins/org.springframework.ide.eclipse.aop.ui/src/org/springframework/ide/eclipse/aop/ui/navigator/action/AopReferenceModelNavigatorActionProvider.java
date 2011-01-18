/*******************************************************************************
 * Copyright (c) 2006, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.aop.ui.navigator.action;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.navigator.ICommonActionConstants;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.ICommonMenuConstants;

/**
 * {@link CommonNavigator} actions that delegates to
 * {@link OpenConfigFileAction}.
 * @author Christian Dupuis
 * @author Torsten Juergeleit
 * @since 2.0
 */
public class AopReferenceModelNavigatorActionProvider extends
		CommonActionProvider {

	private OpenConfigFileAction openConfigAction;

	public AopReferenceModelNavigatorActionProvider() {
	}

	@Override
	public void init(ICommonActionExtensionSite site) {
		openConfigAction = new OpenConfigFileAction(site);
	}

	@Override
	public void fillContextMenu(IMenuManager menu) {
		menu.appendToGroup(ICommonMenuConstants.GROUP_OPEN, openConfigAction);
	}

	@Override
	public void fillActionBars(IActionBars actionBars) {
		actionBars.setGlobalActionHandler(ICommonActionConstants.OPEN,
				openConfigAction);
	}
}
