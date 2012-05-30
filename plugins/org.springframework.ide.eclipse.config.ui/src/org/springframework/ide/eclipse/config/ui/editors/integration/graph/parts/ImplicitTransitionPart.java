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
package org.springframework.ide.eclipse.config.ui.editors.integration.graph.parts;

import org.eclipse.gef.EditPolicy;
import org.springframework.ide.eclipse.config.graph.model.Transition;
import org.springframework.ide.eclipse.config.graph.parts.TransitionPart;


/**
 * @author Leo Dos Santos
 */
public class ImplicitTransitionPart extends TransitionPart {

	public ImplicitTransitionPart(Transition model) {
		super(model);
	}

	@Override
	protected void createEditPolicies() {
		super.createEditPolicies();
		installEditPolicy(EditPolicy.CONNECTION_ROLE, null);
		installEditPolicy(EditPolicy.CONNECTION_ENDPOINTS_ROLE, null);
	}

}
