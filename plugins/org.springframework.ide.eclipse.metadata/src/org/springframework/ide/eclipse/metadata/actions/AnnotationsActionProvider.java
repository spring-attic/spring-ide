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
package org.springframework.ide.eclipse.metadata.actions;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.ICommonMenuConstants;

/**
 * @author Leo Dos Santos
 */
public class AnnotationsActionProvider extends CommonActionProvider {

	private ShowRequestMappingsAction requestMappingsAction;

	@Override
	public void init(ICommonActionExtensionSite aSite) {
		requestMappingsAction = new ShowRequestMappingsAction(aSite);
	}

	@Override
	public void fillContextMenu(IMenuManager menu) {
		if (requestMappingsAction.isEnabled()) {
			menu.appendToGroup(ICommonMenuConstants.GROUP_PROPERTIES,
					requestMappingsAction);
		}
	}

}
