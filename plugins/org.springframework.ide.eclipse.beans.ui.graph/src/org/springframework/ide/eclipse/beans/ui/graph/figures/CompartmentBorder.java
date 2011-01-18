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
package org.springframework.ide.eclipse.beans.ui.graph.figures;

import org.eclipse.draw2d.AbstractBorder;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Insets;

/**
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 */
public class CompartmentBorder extends AbstractBorder {

	public Insets getInsets(IFigure figure) {
		return new Insets(1, 0, 0, 0);
	}

	public void paint(IFigure figure, Graphics graphics, Insets insets) {
		graphics.drawLine(getPaintRectangle(figure, insets).getTopLeft(),
						  tempRect.getTopRight());
	}
}
