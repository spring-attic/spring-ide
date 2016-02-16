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
public class StartTag extends Label {

	private final int direction;

	/**
	 * Creates a new StartTag
	 * @param name the text to display in this StartTag
	 */
	public StartTag(String name, int direction) {
		this.direction = direction;
		setText(name);
		if (direction == PositionConstants.EAST) {
			setTextPlacement(PositionConstants.SOUTH);
			setIcon(CommonImages.getImage(ConfigGraphCommonImages.SEQUENCE_BEGIN_HORIZONTAL));
			setBorder(new MarginBorder(0, 2, 2, 9));
		}
		else {
			setIcon(CommonImages.getImage(ConfigGraphCommonImages.SEQUENCE_BEGIN_VERTICAL));
			setBorder(new MarginBorder(2, 0, 2, 9));
		}
	}

	@Override
	protected void paintFigure(Graphics g) {
		super.paintFigure(g);
		Rectangle r = getTextBounds();

		r.resize(-2, 0).expand(1, 1);
		r.x -= 2;
		g.drawLine(r.x, r.y, r.right(), r.y); // Top line
		g.drawLine(r.x, r.bottom(), r.right(), r.bottom()); // Bottom line
		g.drawLine(r.x, r.bottom(), r.x, r.y); // left line

		g.drawLine(r.right() + 7, r.y + r.height / 2, r.right(), r.y);
		g.drawLine(r.right() + 7, r.y + r.height / 2, r.right(), r.bottom());
	}

}
