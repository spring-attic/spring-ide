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

import org.eclipse.jdt.internal.ui.actions.AbstractToggleLinkingAction;
import org.springframework.ide.eclipse.metadata.ui.RequestMappingView;


/**
 * @author Leo Dos Santos
 */
@SuppressWarnings("restriction")
public class ToggleLinkingAction extends AbstractToggleLinkingAction {

	private RequestMappingView view;

	public ToggleLinkingAction(RequestMappingView view) {
		setText(Messages.ToggleLinkingAction_LABEL);
		setDescription(Messages.ToggleLinkingAction_DESCRIPTION);
		setToolTipText(Messages.ToggleLinkingAction_TOOLTIP);
		setChecked(view.isLinkingEnabled());
		this.view = view;
	}

	@Override
	public void run() {
		view.setLinkingEnabled(isChecked());
	}

}
