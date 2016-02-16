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

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.mylyn.commons.ui.CommonImages;
import org.springframework.ide.eclipse.config.graph.ConfigGraphCommonImages;


/**
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
public class EndTag extends Label {

	private final int direction;

	/**
	 * Creates a new StartTag
	 * @param name the text to display in this StartTag
	 */
	public EndTag(String name, int direction) {
		this.direction = direction;
		setText(name);
		if (direction == PositionConstants.EAST) {
			setTextPlacement(PositionConstants.SOUTH);
			setIcon(CommonImages.getImage(ConfigGraphCommonImages.SEQUENCE_END_HORIZONTAL));
			setBorder(new MarginBorder(0, 2, 2, 9));
		}
		else {
			setIconTextGap(8);
			setIcon(CommonImages.getImage(ConfigGraphCommonImages.SEQUENCE_END_VERTICAL));
			setBorder(new MarginBorder(2, 0, 2, 9));
		}
	}

	@Override
	protected void paintFigure(Graphics g) {
		super.paintFigure(g);
		Rectangle r = getTextBounds();

		if (direction == PositionConstants.EAST) {
			r.resize(-1, 0);
			r.x += 2;
		}
		else {
			r.resize(0, 0).expand(1, 1);
		}
		g.drawLine(r.x, r.y, r.right(), r.y); // Top line
		g.drawLine(r.x, r.bottom(), r.right(), r.bottom()); // Bottom line
		g.drawLine(r.right(), r.bottom(), r.right(), r.y); // Right line

		g.drawLine(r.x - 7, r.y + r.height / 2, r.x, r.y);
		g.drawLine(r.x - 7, r.y + r.height / 2, r.x, r.bottom());
	}
}
