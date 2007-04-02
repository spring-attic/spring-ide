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
package org.springframework.ide.eclipse.beans.ui.graph.internal.navigator;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.ICommonMenuConstants;
import org.springframework.ide.eclipse.beans.ui.graph.internal.navigator.actions.ShowBeansGraphAction;

/**
 * @author Torsten Juergeleit
 */
public class SpringExplorerActionProvider extends CommonActionProvider {

	private ShowBeansGraphAction showBeansGraphAction;

	public SpringExplorerActionProvider() {
	}

	@Override
	public void init(ICommonActionExtensionSite site) {
		showBeansGraphAction = new ShowBeansGraphAction(site);
	}

	@Override
	public void fillContextMenu(IMenuManager menu) {
		if (showBeansGraphAction.isEnabled()) {
			menu.appendToGroup(ICommonMenuConstants.GROUP_OPEN,
					showBeansGraphAction);
		}
	}
}
