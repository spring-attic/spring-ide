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

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.requests.CreateConnectionRequest;
import org.springframework.ide.eclipse.config.graph.model.Activity;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.model.AlternateTransition;


/**
 * @author Leo Dos Santos
 */
public class RouterNodeEditPolicy extends FixedConnectionChannelCreatePolicy {

	@Override
	protected Command getConnectionCreateCommand(CreateConnectionRequest request) {
		if (AlternateTransition.class == request.getNewObjectType()) {
			Activity source = getActivity();
			MappingConnectionCreateCommand cmd = new MappingConnectionCreateCommand(source.getDiagram().getTextEditor());
			cmd.setSource(source);
			request.setStartCommand(cmd);
			return cmd;
		}
		return super.getConnectionCreateCommand(request);
	}

}
