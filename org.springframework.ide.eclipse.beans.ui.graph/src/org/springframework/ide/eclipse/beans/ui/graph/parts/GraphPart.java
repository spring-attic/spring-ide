/*******************************************************************************
 * Copyright (c) 2004, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.graph.parts;

import java.util.List;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.ConnectionLayer;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.ScalableFreeformLayeredPane;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.swt.SWT;
import org.springframework.ide.eclipse.beans.ui.graph.model.Graph;

/**
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 */
public class GraphPart extends AbstractGraphicalEditPart {

	private static final String CONNECTION_LAYER = "Connection Layer";
	
	public static final int MARGIN_SIZE = 10;
	
	@Override
	protected IFigure createFigure() {
		Figure panel = new ScalableFreeformLayeredPane();
		panel.setBackgroundColor(ColorConstants.listBackground);
		panel.setLayoutManager(new XYLayout());
		panel.setBorder(new MarginBorder(MARGIN_SIZE));
		 
		ConnectionLayer cLayer = (ConnectionLayer) getLayer(CONNECTION_LAYER);
        cLayer.setAntialias(SWT.ON);
		
		return panel;
	}

	@Override
	protected void createEditPolicies() {
	}

	@Override
	protected List getModelChildren() {
		return ((Graph) super.getModel()).getNodes();
	}
}
