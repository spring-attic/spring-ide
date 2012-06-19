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
package org.springframework.ide.eclipse.config.ui.editors.integration.graph;

import java.util.List;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.ui.IWorkbenchPart;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.parts.ImplicitChannelGraphicalEditPart;


/**
 * @author Leo Dos Santos
 */
public class CreateExplicitChannelAction extends SelectionAction {

	public static String EXPLICIT_CHANNEL_ID = "ExplicitChannel"; //$NON-NLS-1$

	public static String EXPLICIT_CHANNEL_REQ = EXPLICIT_CHANNEL_ID;

	private final Request request;

	public CreateExplicitChannelAction(IWorkbenchPart part) {
		super(part);
		request = new Request(EXPLICIT_CHANNEL_REQ);
		setId(EXPLICIT_CHANNEL_ID);
		setText(Messages.getString("CreateExplicitChannelAction.CREATE_CHANNEL_ACTION_LABEL")); //$NON-NLS-1$
	}

	@Override
	protected boolean calculateEnabled() {
		List parts = getSelectedObjects();
		if (parts.isEmpty()) {
			return false;
		}
		for (int i = 0; i < parts.size(); i++) {
			Object obj = parts.get(i);
			if (!(obj instanceof ImplicitChannelGraphicalEditPart)) {
				return false;
			}
		}
		return true;
	}

	private Command getCommand() {
		List parts = getSelectedObjects();
		CompoundCommand cc = new CompoundCommand();
		for (int i = 0; i < parts.size(); i++) {
			EditPart part = (EditPart) parts.get(i);
			cc.add(part.getCommand(request));
		}
		return cc;
	}

	@Override
	public void run() {
		execute(getCommand());
	}

}
