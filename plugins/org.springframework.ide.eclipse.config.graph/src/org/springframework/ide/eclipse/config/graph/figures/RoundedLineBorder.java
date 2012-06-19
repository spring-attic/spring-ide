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
package org.springframework.ide.eclipse.config.graph.figures;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.FigureUtilities;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Color;

/**
 * @author Leo Dos Santos
 */
public class RoundedLineBorder extends LineBorder {

	@Override
	public Insets getInsets(IFigure figure) {
		return new Insets(5, 5, 5, 5);
	}

	/**
	 * Copied from
	 * {@link FigureUtilities#paintEtchedBorder(Graphics, Rectangle)} and
	 * modified to dispose the generated colors.
	 */
	private void paintEtchedBorder(Graphics g, Rectangle r) {
		Color rgb = ColorConstants.gray, shadow = FigureUtilities.darker(rgb), highlight = FigureUtilities.lighter(rgb);
		paintEtchedBorder(g, r, shadow, highlight);
		shadow.dispose();
		highlight.dispose();
	}

	/**
	 * Copied from
	 * {@link FigureUtilities#paintEtchedBorder(Graphics, Rectangle, Color, Color)}
	 * and modified to draw round rectangles.
	 */
	private void paintEtchedBorder(Graphics g, Rectangle r, Color shadow, Color highlight) {
		int x = r.x, y = r.y, w = r.width, h = r.height, arc = 10;

		g.setLineStyle(Graphics.LINE_SOLID);
		g.setLineWidth(1);
		g.setXORMode(false);

		w -= 2;
		h -= 2;

		g.setForegroundColor(shadow);
		g.drawRoundRectangle(new Rectangle(x, y, w, h), arc, arc);

		x++;
		y++;
		g.setForegroundColor(highlight);
		g.drawRoundRectangle(new Rectangle(x, y, w, h), arc, arc);
	}

	/**
	 * Copied from {@link LineBorder#paint(IFigure, Graphics, Insets)} and
	 * modified to use a different
	 * {@link #paintEtchedBorder(Graphics, Rectangle)} method.
	 */
	@Override
	public void paint(IFigure figure, Graphics g, Insets insets) {
		tempRect.setBounds(getPaintRectangle(figure, insets));
		if (getWidth() % 2 == 1) {
			tempRect.width--;
			tempRect.height--;
		}
		tempRect.shrink(getWidth() / 2, getWidth() / 2);
		if (getColor() != null) {
			g.setForegroundColor(getColor());
		}
		paintEtchedBorder(g, tempRect);
	}

}
