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
package org.springframework.ide.eclipse.config.ui.editors.webflow.graph;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.requests.CreateConnectionRequest;
import org.springframework.ide.eclipse.config.graph.model.Activity;
import org.springframework.ide.eclipse.config.graph.policies.ActivityNodeEditPolicy;
import org.springframework.ide.eclipse.config.ui.editors.webflow.graph.model.DecisionStateModelElement;


/**
 * @author Leo Dos Santos
 */
public class StateNodeEditPolicy extends ActivityNodeEditPolicy {

	@Override
	protected Command getConnectionCreateCommand(CreateConnectionRequest request) {
		int style = ((Integer) request.getNewObjectType()).intValue();
		Activity source = getActivity();
		if (!(source instanceof DecisionStateModelElement)) {
			StateConnectionCreateCommand cmd = new StateConnectionCreateCommand(source.getDiagram().getTextEditor(),
					style);
			cmd.setSource(source);
			request.setStartCommand(cmd);
			return cmd;
		}
		return super.getConnectionCreateCommand(request);
	}

}
