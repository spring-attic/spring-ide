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

import org.springframework.ide.eclipse.config.graph.parts.ParallelActivityPart;
import org.springframework.ide.eclipse.config.ui.editors.batch.graph.model.DecisionContainerElement;


/**
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
public class DecisionContainerEditPart extends ParallelActivityPart {

	public DecisionContainerEditPart(DecisionContainerElement decision) {
		super(decision);
	}

	@Override
	public DecisionContainerElement getModelElement() {
		return (DecisionContainerElement) getModel();
	}

}
