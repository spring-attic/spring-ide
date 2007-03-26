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
package org.springframework.ide.eclipse.beans.ui.graph.figures;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;

/**
 * Draws a rectangle border whose corners are rounded in appearance.
 */
public class RoundedLineBorder extends LineBorder {

	/** The width and height radii applied to each corner. */
	protected Dimension corner;

	public RoundedLineBorder() {
		this(new Dimension(8, 8));
	}

	public RoundedLineBorder(Dimension corner) {
		this.corner = corner;
	}

	@Override
	public void paint(IFigure figure, Graphics graphics, Insets insets) {

		// Calculations from super.paint()
		tempRect.setBounds(getPaintRectangle(figure, insets));
		if (getWidth() % 2 == 1) {
			tempRect.width--;
			tempRect.height--;
		}
		tempRect.shrink(getWidth() / 2, getWidth() / 2);
		graphics.setLineWidth(getWidth());
		if (getColor() != null) {
			graphics.setForegroundColor(getColor());
		}
		graphics.drawRoundRectangle(tempRect, corner.width, corner.height);
	}

	/**
	 * Sets the dimensions of each corner. This will form the radii of the arcs
	 * which form the corners.
	 *
	 * @param d the dimensions of the corner
	 */
	public void setCornerDimensions(Dimension d) {
		corner.width = d.width;
		corner.height = d.height;
	}
}
