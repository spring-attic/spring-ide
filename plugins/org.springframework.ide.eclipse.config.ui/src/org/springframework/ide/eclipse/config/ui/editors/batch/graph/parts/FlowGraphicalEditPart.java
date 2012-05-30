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
package org.springframework.ide.eclipse.config.ui.editors.batch.graph.parts;

import org.springframework.ide.eclipse.config.graph.parts.SequentialActivityPart;
import org.springframework.ide.eclipse.config.ui.editors.batch.graph.model.FlowModelElement;


/**
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
public class FlowGraphicalEditPart extends SequentialActivityPart {

	public FlowGraphicalEditPart(FlowModelElement flow) {
		super(flow);
	}

	@Override
	public FlowModelElement getModelElement() {
		return (FlowModelElement) getModel();
	}

}
