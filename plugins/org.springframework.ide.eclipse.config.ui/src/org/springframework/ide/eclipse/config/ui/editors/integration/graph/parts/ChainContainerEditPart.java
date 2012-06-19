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

import org.springframework.ide.eclipse.config.graph.parts.ParallelActivityPart;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.model.ChainContainerElement;


/**
 * @author Leo Dos Santos
 */
public class ChainContainerEditPart extends ParallelActivityPart {

	public ChainContainerEditPart(ChainContainerElement chain) {
		super(chain);
	}

	@Override
	public ChainContainerElement getModelElement() {
		return (ChainContainerElement) getModel();
	}

}
