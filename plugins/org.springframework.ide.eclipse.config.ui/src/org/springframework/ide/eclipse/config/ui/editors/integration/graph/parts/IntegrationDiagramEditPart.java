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

import org.eclipse.draw2d.PositionConstants;
import org.springframework.ide.eclipse.config.graph.parts.ActivityDiagramPart;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.model.IntegrationDiagram;


/**
 * @author Leo Dos Santos
 */
public class IntegrationDiagramEditPart extends ActivityDiagramPart {

	public IntegrationDiagramEditPart(IntegrationDiagram diagram) {
		super(diagram, PositionConstants.EAST);
	}

	@Override
	public IntegrationDiagram getModelElement() {
		return (IntegrationDiagram) getModel();
	}

}
