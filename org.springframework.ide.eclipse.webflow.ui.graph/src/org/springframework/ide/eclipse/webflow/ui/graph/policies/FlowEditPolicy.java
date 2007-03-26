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
package org.springframework.ide.eclipse.webflow.ui.graph.policies;

import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.RootComponentEditPolicy;
import org.springframework.ide.eclipse.webflow.core.model.ICloneableModelElement;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;
import org.springframework.ide.eclipse.webflow.ui.graph.actions.EditPropertiesAction;
import org.springframework.ide.eclipse.webflow.ui.graph.commands.EditPropertiesCommand;

/**
 * 
 */
public class FlowEditPolicy extends RootComponentEditPolicy {

	/* (non-Javadoc)
	 * @see org.eclipse.gef.editpolicies.ComponentEditPolicy#getCommand(org.eclipse.gef.Request)
	 */
	public Command getCommand(Request request) {
		if (EditPropertiesAction.EDITPROPERTIES_REQUEST.equals(request
				.getType())) {
			return getEditPropertiesCommand();
		}
		return super.getCommand(request);
	}

	/**
	 * 
	 * 
	 * @return 
	 */
	@SuppressWarnings("unchecked")
	protected Command getEditPropertiesCommand() {
		EditPropertiesCommand command = new EditPropertiesCommand();
		command
				.setChild((ICloneableModelElement<IWebflowModelElement>) getHost()
						.getModel());
		return command;
	}
}
