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
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;

/**
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
public class SequentialActivityFigure extends SubgraphFigure {

	/**
	 * @param header
	 * @param footer
	 */
	public SequentialActivityFigure(int direction) {
		super(new StartTag("", direction), new EndTag("", direction), direction); //$NON-NLS-1$ //$NON-NLS-2$
		if (direction == PositionConstants.EAST) {
			setBorder(new MarginBorder(8, 0, 0, 0));
		}
		else {
			setBorder(new MarginBorder(0, 8, 0, 0));
		}
		setOpaque(true);
	}

	@Override
	public Dimension getPreferredSize(int wHint, int hHint) {
		if (direction == PositionConstants.EAST) {
			Dimension dim = new Dimension();
			int hHeader = getHeader().getPreferredSize().height;
			int hFooter = getFooter().getPreferredSize().height;
			dim.height = hHeader >= hFooter ? hHeader : hFooter;
			dim.height += getInsets().getHeight();
			dim.width = 50;
			return dim;
		}
		return super.getPreferredSize(wHint, hHint);
	}

	@Override
	protected void paintFigure(Graphics graphics) {
		super.paintFigure(graphics);
		graphics.setBackgroundColor(ColorConstants.button);
		Rectangle r = getBounds();
		if (direction == PositionConstants.EAST) {
			graphics.fillRectangle(r.x + 10, r.y + 18, r.width - 24, 8);
		}
		else {
			graphics.fillRectangle(r.x + 18, r.y + 10, 8, r.height - 18);
		}
	}

	@Override
	public void setBounds(Rectangle rect) {
		super.setBounds(rect);
		if (direction == PositionConstants.EAST) {
			getClientArea(rect);
			Dimension size = footer.getPreferredSize();
			footer.setLocation(rect.getTopRight().translate(-size.width, 0));
			footer.setSize(size);
		}
	}
}
