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
package org.springframework.ide.eclipse.config.graph.actions;

import java.util.List;

import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.ui.IWorkbenchPart;
import org.springframework.ide.eclipse.config.graph.parts.ActivityPart;


/**
 * @author Leo Dos Santos
 */
public class ShowPropertiesAction extends SelectionAction {

	public static String SHOW_PROPERTIES_ID = "ShowProperties"; //$NON-NLS-1$

	public ShowPropertiesAction(IWorkbenchPart part) {
		super(part);
		setId(SHOW_PROPERTIES_ID);
		setText(Messages.ShowPropertiesAction_SHOW_PROPERTIES_ACTION_LABEL);
	}

	@Override
	protected boolean calculateEnabled() {
		List parts = getSelectedObjects();
		if (parts.isEmpty()) {
			return false;
		}
		return (parts.get(0) instanceof ActivityPart);
	}

	@Override
	public void run() {
		ActivityPart part = (ActivityPart) getSelectedObjects().get(0);
		part.showProperties();
	}

}
